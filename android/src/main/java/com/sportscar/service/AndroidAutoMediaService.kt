package com.sportscar.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.utils.MediaConstants
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.sportscar.models.*
import com.sportscar.utils.ImageCache
import com.sportscar.utils.MediaLibraryParser
import kotlinx.coroutines.*
import org.json.JSONObject

class AndroidAutoMediaService : MediaBrowserServiceCompat() {

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "AndroidAutoMediaService"
        private const val NOTIFICATION_ID = 1001
        private const val ROOT_ID = "root_id"
        
        // SharedPreferences keys
        private const val PREFS_NAME = "AndroidAutoMediaService"
        private const val KEY_MEDIA_LIBRARY = "media_library"
        private const val KEY_LAYOUT_TYPE = "layout_type"
        private const val KEY_LAST_PLAYED_MEDIA_ID = "last_played_media_id"
        private const val KEY_LAST_POSITION_MS = "last_position_ms"
        private const val KEY_APP_STATE = "app_state"
        
        // Broadcast actions
        const val ACTION_UPDATE_MEDIA_LIBRARY = "com.margelo.nitro.sportscar.UPDATE_MEDIA_LIBRARY"
        const val ACTION_SET_LAYOUT_TYPE = "com.margelo.nitro.sportscar.SET_LAYOUT_TYPE"
        const val ACTION_PLAY_MEDIA = "com.margelo.nitro.sportscar.PLAY_MEDIA"
        const val ACTION_PAUSE = "com.margelo.nitro.sportscar.PAUSE"
        const val ACTION_RESUME = "com.margelo.nitro.sportscar.RESUME"
        const val ACTION_STOP = "com.margelo.nitro.sportscar.STOP"
        const val ACTION_SEEK_TO = "com.margelo.nitro.sportscar.SEEK_TO"
        const val ACTION_SET_PLAYBACK_SPEED = "com.margelo.nitro.sportscar.SET_PLAYBACK_SPEED"
        const val ACTION_SKIP_TO_NEXT = "com.margelo.nitro.sportscar.SKIP_TO_NEXT"
        const val ACTION_SKIP_TO_PREVIOUS = "com.margelo.nitro.sportscar.SKIP_TO_PREVIOUS"
        const val ACTION_APP_STATE_CHANGED = "com.margelo.nitro.sportscar.APP_STATE_CHANGED"
        
        // Intent extras
        const val EXTRA_MEDIA_LIBRARY_JSON = "media_library_json"
        const val EXTRA_LAYOUT_TYPE = "layout_type"
        const val EXTRA_MEDIA_ID = "media_id"
        const val EXTRA_POSITION_MS = "position_ms"
        const val EXTRA_PLAYBACK_SPEED = "playback_speed"
        const val EXTRA_APP_STATE = "app_state"
        
        // Instance for background access
        @Volatile
        private var instance: AndroidAutoMediaService? = null
        
        fun getInstance(): AndroidAutoMediaService? = instance
        
        // App state constants
        const val APP_STATE_FOREGROUND = "foreground"
        const val APP_STATE_BACKGROUND = "background"
        const val APP_STATE_DESTROYED = "destroyed"
    }

    // Service binding
    private val binder = LocalBinder()
    
    inner class LocalBinder : Binder() {
        fun getService(): AndroidAutoMediaService = this@AndroidAutoMediaService
    }

    // Media components
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var exoPlayer: ExoPlayer
    private var notificationManager: PlayerNotificationManager? = null
    
    // Media library
    private var mediaLibrary: MediaLibrary? = null
    private var currentMediaItem: AndroidAutoMediaItem? = null
    
    // Data persistence
    private lateinit var sharedPreferences: SharedPreferences
    
    // Broadcast receiver for background updates
    private val backgroundUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { handleBackgroundUpdate(it) }
        }
    }
    
    // Coroutine scope for async operations
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        
        instance = this
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        createNotificationChannel()
        initializeMediaSession()
        initializeExoPlayer()
        initializeNotificationManager()
        registerBackgroundUpdateReceiver()
        
        // Load persisted data
        loadPersistedData()
        
        // Clean up expired failed URLs periodically
        serviceScope.launch {
            while (isActive) {
                delay(10 * 60 * 1000) // Every 10 minutes
                ImageCache.clearExpiredFailedUrls()
                println("🧹 AndroidAutoMediaService: Cleaned up expired failed URLs")
            }
        }
        
        println("🚀 AndroidAutoMediaService: Service created")
    }

    override fun onDestroy() {
        super.onDestroy()
        
        instance = null
        unregisterBackgroundUpdateReceiver()
        serviceScope.cancel()
        exoPlayer.release()
        mediaSession.release()
        notificationManager?.setPlayer(null)
        
        println("🛑 AndroidAutoMediaService: Service destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return if (intent?.action == "android.media.browse.MediaBrowserService") {
            super.onBind(intent)
        } else {
            binder
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Android Auto Media Player",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Media playback controls"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "AndroidAutoMediaService").apply {
            // Set flags to handle media buttons and transport controls
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            
            // Set callback to handle physical button events from car steering wheel
            setCallback(MediaSessionCallback())
            
            // Activate the session to receive events
            isActive = true
            
            println("🎮 AndroidAutoMediaService: Media session initialized with button handling")
        }
        sessionToken = mediaSession.sessionToken
    }

    private fun initializeExoPlayer() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE) // Changed to support both audio and video
            .build()

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()

        exoPlayer.addListener(PlayerEventListener())
    }

    private fun initializeNotificationManager() {
        notificationManager = PlayerNotificationManager.Builder(
            this,
            NOTIFICATION_ID,
            NOTIFICATION_CHANNEL_ID
        )
            .setMediaDescriptionAdapter(MediaDescriptionAdapter())
            .setNotificationListener(NotificationListener())
            .build()
            .apply {
                setPlayer(exoPlayer)
                setMediaSessionToken(sessionToken!!)
                setUseRewindAction(false)
                setUseFastForwardAction(false)
                // Enable next/previous buttons in notification and Android Auto
                setUseNextAction(true)
                setUsePreviousAction(true)
            }
    }
    
    private fun registerBackgroundUpdateReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_UPDATE_MEDIA_LIBRARY)
            addAction(ACTION_SET_LAYOUT_TYPE)
            addAction(ACTION_PLAY_MEDIA)
            addAction(ACTION_PAUSE)
            addAction(ACTION_RESUME)
            addAction(ACTION_STOP)
            addAction(ACTION_SEEK_TO)
            addAction(ACTION_SET_PLAYBACK_SPEED)
            addAction(ACTION_SKIP_TO_NEXT)
            addAction(ACTION_SKIP_TO_PREVIOUS)
            addAction(ACTION_APP_STATE_CHANGED)
        }
        
        // For Android 14+ (API 34+), we need to specify the receiver flags
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            registerReceiver(backgroundUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(backgroundUpdateReceiver, filter)
        }
    }
    
    private fun unregisterBackgroundUpdateReceiver() {
        try {
            unregisterReceiver(backgroundUpdateReceiver)
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Error unregistering receiver - ${e.message}")
        }
    }
    
    private fun loadPersistedData() {
        serviceScope.launch {
            try {
                // Load media library
                val mediaLibraryJson = sharedPreferences.getString(KEY_MEDIA_LIBRARY, null)
                if (mediaLibraryJson != null) {
                    val library = MediaLibraryParser.parseFromJson(mediaLibraryJson)
                    if (library != null) {
                        mediaLibrary = library
                        ImageCache.preloadImages(applicationContext, library)
                        notifyChildrenChanged(ROOT_ID)
                        println("📱 AndroidAutoMediaService: Loaded persisted media library")
                    }
                }
                
                // Load layout type
                val layoutType = sharedPreferences.getString(KEY_LAYOUT_TYPE, "GRID")
                mediaLibrary?.let { library ->
                    val newLayoutType = when (layoutType?.uppercase()) {
                        "LIST" -> LayoutType.LIST
                        else -> LayoutType.GRID
                    }
                    mediaLibrary = library.copy(layoutType = newLayoutType)
                    updateServiceLayoutHints()
                    println("📱 AndroidAutoMediaService: Loaded persisted layout type: $newLayoutType")
                }

                // Load last played media and position
                val lastPlayedMediaId = sharedPreferences.getString(KEY_LAST_PLAYED_MEDIA_ID, null)
                val lastPositionMs = sharedPreferences.getLong(KEY_LAST_POSITION_MS, 0L)

                if (lastPlayedMediaId != null && lastPositionMs > 0) {
                    val mediaItem = findMediaItemById(lastPlayedMediaId)
                    if (mediaItem != null) {
                        playMedia(lastPlayedMediaId)
                        seekTo(lastPositionMs)
                        println("📱 AndroidAutoMediaService: Loaded last played media and position")
                    }
                }

                // Load app state
                val appState = sharedPreferences.getString(KEY_APP_STATE, APP_STATE_FOREGROUND)
                if (appState == APP_STATE_BACKGROUND) {
                    // If app was in background, ensure player is paused
                    pause()
                    println("📱 AndroidAutoMediaService: App was in background, paused player")
                } else if (appState == APP_STATE_DESTROYED) {
                    // If app was destroyed, stop player and clear state
                    stop()
                    println("📱 AndroidAutoMediaService: App was destroyed, stopped player and cleared state")
                }

            } catch (e: Exception) {
                println("❌ AndroidAutoMediaService: Error loading persisted data - ${e.message}")
            }
        }
    }
    
    private fun persistData() {
        try {
            // Persist media library
            mediaLibrary?.let { library ->
                val json = MediaLibraryParser.toJson(library)
                sharedPreferences.edit().putString(KEY_MEDIA_LIBRARY, json).apply()
            }
            
            // Persist layout type
            val layoutType = when (mediaLibrary?.layoutType) {
                LayoutType.LIST -> "LIST"
                else -> "GRID"
            }
            sharedPreferences.edit().putString(KEY_LAYOUT_TYPE, layoutType).apply()

            // Persist last played media and position
            currentMediaItem?.let { mediaItem ->
                sharedPreferences.edit().putString(KEY_LAST_PLAYED_MEDIA_ID, mediaItem.id).apply()
                sharedPreferences.edit().putLong(KEY_LAST_POSITION_MS, exoPlayer.currentPosition).apply()
            }

            // Persist app state - default to foreground since we can't determine actual state here
            val appState = APP_STATE_FOREGROUND
            sharedPreferences.edit().putString(KEY_APP_STATE, appState).apply()
            
            println("💾 AndroidAutoMediaService: Data persisted")
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Error persisting data - ${e.message}")
        }
    }
    
    private fun handleBackgroundUpdate(intent: Intent) {
        when (intent.action) {
            ACTION_UPDATE_MEDIA_LIBRARY -> {
                val mediaLibraryJson = intent.getStringExtra(EXTRA_MEDIA_LIBRARY_JSON)
                if (mediaLibraryJson != null) {
                    serviceScope.launch {
                        try {
                            val library = MediaLibraryParser.parseFromJson(mediaLibraryJson)
                            if (library != null) {
                                updateMediaLibrary(library)
                                println("🔄 AndroidAutoMediaService: Background media library update")
                            }
                        } catch (e: Exception) {
                            println("❌ AndroidAutoMediaService: Background update failed - ${e.message}")
                        }
                    }
                }
            }
            ACTION_SET_LAYOUT_TYPE -> {
                val layoutType = intent.getStringExtra(EXTRA_LAYOUT_TYPE)
                if (layoutType != null) {
                    setLayoutType(layoutType)
                    println("📱 AndroidAutoMediaService: Background layout type update")
                }
            }
            ACTION_PLAY_MEDIA -> {
                val mediaId = intent.getStringExtra(EXTRA_MEDIA_ID)
                if (mediaId != null) {
                    playMedia(mediaId)
                    println("▶️ AndroidAutoMediaService: Background play media")
                }
            }
            ACTION_PAUSE -> {
                pause()
                println("⏸️ AndroidAutoMediaService: Background pause")
            }
            ACTION_RESUME -> {
                resume()
                println("▶️ AndroidAutoMediaService: Background resume")
            }
            ACTION_STOP -> {
                stop()
                println("⏹️ AndroidAutoMediaService: Background stop")
            }
            ACTION_SEEK_TO -> {
                val positionMs = intent.getLongExtra(EXTRA_POSITION_MS, 0L)
                seekTo(positionMs)
                println("⏩ AndroidAutoMediaService: Background seek")
            }
            ACTION_SET_PLAYBACK_SPEED -> {
                val speed = intent.getFloatExtra(EXTRA_PLAYBACK_SPEED, 1.0f)
                setPlaybackSpeed(speed)
                println("🏃 AndroidAutoMediaService: Background speed change")
            }
            ACTION_SKIP_TO_NEXT -> {
                skipToNext()
                println("⏭️ AndroidAutoMediaService: Background skip to next")
            }
            ACTION_SKIP_TO_PREVIOUS -> {
                skipToPrevious()
                println("⏮️ AndroidAutoMediaService: Background skip to previous")
            }
            ACTION_APP_STATE_CHANGED -> {
                val appState = intent.getStringExtra(EXTRA_APP_STATE)
                if (appState == APP_STATE_FOREGROUND) {
                    // If app is foreground, ensure player is playing
                    resume()
                    println("📱 AndroidAutoMediaService: App is foreground, resumed player")
                } else if (appState == APP_STATE_BACKGROUND) {
                    // If app is background, ensure player is paused
                    pause()
                    println("📱 AndroidAutoMediaService: App is background, paused player")
                }
            }
        }
    }

    // Public API methods
    fun initializeMediaLibrary(library: MediaLibrary) {
        mediaLibrary = library
        serviceScope.launch {
            ImageCache.preloadImages(applicationContext, library)
            notifyChildrenChanged(ROOT_ID)
            persistData()
            println("✅ AndroidAutoMediaService: Media library initialized with ${library.rootItems.size} items")
            println("📊 ${getImageCacheStats()}")
        }
    }

    fun updateMediaLibrary(library: MediaLibrary) {
        mediaLibrary = library
        serviceScope.launch {
            ImageCache.preloadImages(applicationContext, library)
            notifyChildrenChanged(ROOT_ID)
            updateServiceLayoutHints()
            persistData()
            println("🔄 AndroidAutoMediaService: Media library updated")
            println("📊 ${getImageCacheStats()}")
        }
    }

    fun getMediaLibrary(): MediaLibrary? {
        return mediaLibrary
    }
    
    /**
     * Get image cache statistics for debugging
     */
    fun getImageCacheStats(): String {
        return "ImageCache: ${ImageCache.getCacheSize()} cached, ${ImageCache.getFailedUrlsCount()} failed"
    }

    fun setLayoutType(layoutType: String) {
        mediaLibrary?.let { library ->
            val newLayoutType = when (layoutType.lowercase()) {
                "list" -> LayoutType.LIST
                else -> LayoutType.GRID
            }
            mediaLibrary = library.copy(layoutType = newLayoutType)
            updateServiceLayoutHints()
            notifyChildrenChanged(ROOT_ID)
            persistData()
            println("📱 AndroidAutoMediaService: Layout type set to $newLayoutType")
        }
    }

    fun refreshAndroidAutoUI() {
        serviceScope.launch {
            updateServiceLayoutHints()
            notifyChildrenChanged(ROOT_ID)
            println("🔄 AndroidAutoMediaService: UI manually refreshed")
        }
    }

    fun playMedia(mediaId: String): Boolean {
        return try {
            val mediaItem = findMediaItemById(mediaId)
            if (mediaItem?.isPlayable == true && mediaItem.mediaUrl != null) {
                // Ensure we're on the main thread for ExoPlayer operations
                serviceScope.launch(Dispatchers.Main) {
                    try {
                        val exoMediaItem = com.google.android.exoplayer2.MediaItem.Builder()
                            .setUri(mediaItem.mediaUrl)
                            .setMediaId(mediaItem.id)
                            .build()
                        
                        // Configure ExoPlayer based on media type
                        if (mediaItem.mediaType == MediaType.VIDEO) {
                            // For video content, we need to handle it differently
                            // Note: Android Auto has limited video support, mainly for parked scenarios
                            println("🎬 AndroidAutoMediaService: Configuring for video playback")
                        }
                        
                        exoPlayer.setMediaItem(exoMediaItem)
                        exoPlayer.prepare()
                        exoPlayer.play()
                        
                        currentMediaItem = mediaItem
                        updateMediaSessionMetadata(mediaItem)
                        updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                        persistData() // Persist last played media and position
                        
                        println("▶️ AndroidAutoMediaService: Playing ${mediaItem.title} (${mediaItem.mediaType})")
                    } catch (e: Exception) {
                        println("❌ AndroidAutoMediaService: Failed to play media on main thread - ${e.message}")
                    }
                }
                true
            } else {
                println("❌ AndroidAutoMediaService: Media item not playable - $mediaId")
                false
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to play media - ${e.message}")
            false
        }
    }

    fun pause(): Boolean {
        return try {
            // Ensure we're on the main thread for ExoPlayer operations
            serviceScope.launch(Dispatchers.Main) {
                try {
                    exoPlayer.pause()
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
                    println("⏸️ AndroidAutoMediaService: Playback paused")
                } catch (e: Exception) {
                    println("❌ AndroidAutoMediaService: Failed to pause on main thread - ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to pause - ${e.message}")
            false
        }
    }

    fun resume(): Boolean {
        return try {
            // Ensure we're on the main thread for ExoPlayer operations
            serviceScope.launch(Dispatchers.Main) {
                try {
                    exoPlayer.play()
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    println("▶️ AndroidAutoMediaService: Playback resumed")
                } catch (e: Exception) {
                    println("❌ AndroidAutoMediaService: Failed to resume on main thread - ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to resume - ${e.message}")
            false
        }
    }

    fun stop(): Boolean {
        return try {
            // Ensure we're on the main thread for ExoPlayer operations
            serviceScope.launch(Dispatchers.Main) {
                try {
                    exoPlayer.stop()
                    currentMediaItem = null
                    updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)
                    println("⏹️ AndroidAutoMediaService: Playback stopped")
                } catch (e: Exception) {
                    println("❌ AndroidAutoMediaService: Failed to stop on main thread - ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to stop - ${e.message}")
            false
        }
    }

    fun seekTo(positionMs: Long): Boolean {
        return try {
            // Ensure we're on the main thread for ExoPlayer operations
            serviceScope.launch(Dispatchers.Main) {
                try {
                    exoPlayer.seekTo(positionMs)
                    println("⏩ AndroidAutoMediaService: Seeked to ${positionMs}ms")
                } catch (e: Exception) {
                    println("❌ AndroidAutoMediaService: Failed to seek on main thread - ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to seek - ${e.message}")
            false
        }
    }

    fun setPlaybackSpeed(speed: Float): Boolean {
        return try {
            // Ensure we're on the main thread for ExoPlayer operations
            serviceScope.launch(Dispatchers.Main) {
                try {
                    exoPlayer.setPlaybackSpeed(speed)
                    println("🏃 AndroidAutoMediaService: Playback speed set to ${speed}x")
                } catch (e: Exception) {
                    println("❌ AndroidAutoMediaService: Failed to set playback speed on main thread - ${e.message}")
                }
            }
            true
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to set playback speed - ${e.message}")
            false
        }
    }

    fun skipToNext(): Boolean {
        return try {
            val nextMediaId = getNextMediaId()
            if (nextMediaId != null) {
                playMedia(nextMediaId)
                println("⏭️ AndroidAutoMediaService: Skipped to next track: $nextMediaId")
                true
            } else {
                println("⏭️ AndroidAutoMediaService: No next track available")
                false
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to skip to next - ${e.message}")
            false
        }
    }

    fun skipToPrevious(): Boolean {
        return try {
            val previousMediaId = getPreviousMediaId()
            if (previousMediaId != null) {
                playMedia(previousMediaId)
                println("⏮️ AndroidAutoMediaService: Skipped to previous track: $previousMediaId")
                true
            } else {
                println("⏮️ AndroidAutoMediaService: No previous track available")
                false
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to skip to previous - ${e.message}")
            false
        }
    }

    /**
     * Handle app state changes (foreground/background/destroyed)
     * This method can be called from the React Native app to notify the service
     * about app lifecycle changes
     */
    fun handleAppStateChange(appState: String): Boolean {
        return try {
            when (appState) {
                APP_STATE_FOREGROUND -> {
                    // App is in foreground - resume playback if it was paused due to background
                    if (exoPlayer.playbackState == Player.STATE_READY && !exoPlayer.playWhenReady) {
                        resume()
                        println("📱 AndroidAutoMediaService: App foreground - resuming playback")
                    }
                }
                APP_STATE_BACKGROUND -> {
                    // App is in background - pause playback to save resources
                    if (exoPlayer.playbackState == Player.STATE_READY && exoPlayer.playWhenReady) {
                        pause()
                        println("📱 AndroidAutoMediaService: App background - pausing playback")
                    }
                }
                APP_STATE_DESTROYED -> {
                    // App is destroyed - stop playback and clear state
                    stop()
                    println("📱 AndroidAutoMediaService: App destroyed - stopping playback")
                }
            }
            
            // Persist the app state
            sharedPreferences.edit().putString(KEY_APP_STATE, appState).apply()
            true
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to handle app state change - ${e.message}")
            false
        }
    }

    /**
     * Get the current app state from SharedPreferences
     */
    fun getCurrentAppState(): String {
        return sharedPreferences.getString(KEY_APP_STATE, APP_STATE_FOREGROUND) ?: APP_STATE_FOREGROUND
    }

    /**
     * Check if the service is currently playing media
     */
    fun isCurrentlyPlaying(): Boolean {
        return exoPlayer.playbackState == Player.STATE_READY && exoPlayer.playWhenReady
    }

    /**
     * Get the last played media information
     */
    fun getLastPlayedMediaInfo(): Pair<String?, Long> {
        val mediaId = sharedPreferences.getString(KEY_LAST_PLAYED_MEDIA_ID, null)
        val position = sharedPreferences.getLong(KEY_LAST_POSITION_MS, 0L)
        return Pair(mediaId, position)
    }

    fun getPlaybackInfo(): PlaybackInfo {
        return try {
            PlaybackInfo(
                state = when (exoPlayer.playbackState) {
                    Player.STATE_IDLE -> "stopped"
                    Player.STATE_BUFFERING -> "buffering"
                    Player.STATE_READY -> if (exoPlayer.playWhenReady) "playing" else "paused"
                    Player.STATE_ENDED -> "stopped"
                    else -> "stopped"
                },
                currentMediaId = currentMediaItem?.id,
                positionMs = exoPlayer.currentPosition,
                durationMs = exoPlayer.duration.takeIf { it != C.TIME_UNSET } ?: 0,
                playbackSpeed = exoPlayer.playbackParameters.speed,
                shuffleEnabled = exoPlayer.shuffleModeEnabled,
                repeatMode = when (exoPlayer.repeatMode) {
                    Player.REPEAT_MODE_OFF -> "none"
                    Player.REPEAT_MODE_ONE -> "one"
                    Player.REPEAT_MODE_ALL -> "all"
                    else -> "none"
                }
            )
        } catch (e: Exception) {
            println("❌ AndroidAutoMediaService: Failed to get playback info - ${e.message}")
            PlaybackInfo(
                state = "stopped",
                currentMediaId = null,
                positionMs = 0,
                durationMs = 0,
                playbackSpeed = 1.0f,
                shuffleEnabled = false,
                repeatMode = "none"
            )
        }
    }

    // MediaBrowserService implementation
    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot {
        val extras = Bundle().apply {
            val layoutHint = when (mediaLibrary?.layoutType) {
                LayoutType.LIST -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
                else -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM
            }
            putInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, layoutHint)
            putInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE, layoutHint)
        }
        return BrowserRoot(ROOT_ID, extras)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        when (parentId) {
            ROOT_ID -> {
                val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                mediaLibrary?.rootItems?.forEach { item ->
                    mediaItems.add(createMediaBrowserItem(item))
                }
                result.sendResult(mediaItems)
            }
            else -> {
                val item = findMediaItemById(parentId)
                if (item?.children != null) {
                    val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                    item.children.forEach { child ->
                        mediaItems.add(createMediaBrowserItem(child))
                    }
                    result.sendResult(mediaItems)
                } else {
                    result.sendResult(mutableListOf())
                }
            }
        }
    }

    // Helper methods
    private fun findMediaItemById(id: String): AndroidAutoMediaItem? {
        return mediaLibrary?.let { library ->
            findMediaItemInList(id, library.rootItems)
        }
    }

    private fun findMediaItemInList(id: String, items: List<AndroidAutoMediaItem>): AndroidAutoMediaItem? {
        items.forEach { item ->
            if (item.id == id) return item
            item.children?.let { children ->
                findMediaItemInList(id, children)?.let { found -> return found }
            }
        }
        return null
    }

    private fun createMediaBrowserItem(item: AndroidAutoMediaItem): MediaBrowserCompat.MediaItem {
        val builder = MediaDescriptionCompat.Builder()
            .setMediaId(item.id)
            .setTitle(item.title)
        
        // Set subtitle based on media type
        val subtitle = when (item.mediaType) {
            MediaType.VIDEO -> "Video • ${item.subtitle ?: "Unknown"}"
            MediaType.AUDIO -> item.subtitle ?: "Unknown Artist"
            else -> item.subtitle
        }
        builder.setSubtitle(subtitle)

        // Set icon if available
        item.iconUrl?.let { url ->
            ImageCache.getCachedBitmap(url)?.let { bitmap ->
                builder.setIconBitmap(bitmap)
            }
        }

        // Set layout hints and media type information
        val extras = Bundle().apply {
            // Use item-specific layout type if available, otherwise fall back to library default
            val effectiveLayoutType = item.layoutType ?: mediaLibrary?.layoutType
            val layoutHint = when (effectiveLayoutType) {
                LayoutType.LIST -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
                else -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM
            }
            println("🎨 AndroidAutoMediaService: Setting layout for '${item.title}' - type: $effectiveLayoutType, hint: $layoutHint")
            
            // WORKAROUND: Set both browsable and playable hints to ensure consistency
            // This helps prevent layout inheritance issues in Android Auto
            putInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, layoutHint)
            putInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE, layoutHint)
            
            // Add unique identifiers to help Android Auto distinguish between items
            putString("android.media.browse.FOLDER_TYPE", effectiveLayoutType?.name ?: "GRID")
            putString("android.media.browse.LAYOUT_HINT_ID", "${item.id}_${effectiveLayoutType?.name ?: "GRID"}")
            
            if (item.isPlayable) {
                // Add media type information
                putString("android.media.metadata.MEDIA_TYPE", item.mediaType.name)
                if (item.mediaType == MediaType.VIDEO) {
                    putString("android.media.metadata.CONTENT_TYPE", "video/*")
                    putString("android.media.browse.CONTENT_TYPE", "video")
                    putBoolean("android.media.browse.IS_VIDEO", true)
                    // Add video-specific flags
                    putInt("android.media.browse.VIDEO_WIDTH", 1920)
                    putInt("android.media.browse.VIDEO_HEIGHT", 1080)
                } else {
                    putString("android.media.metadata.CONTENT_TYPE", "audio/*")
                    putString("android.media.browse.CONTENT_TYPE", "audio")
                    putBoolean("android.media.browse.IS_VIDEO", false)
                }
                // Add duration if available
                item.durationMs?.let { duration ->
                    putLong("android.media.metadata.DURATION", duration)
                }
            }
            // Note: Both browsable and playable hints are already set above
        }
        builder.setExtras(extras)

        val flags = if (item.isPlayable) {
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        } else {
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        }

        return MediaBrowserCompat.MediaItem(builder.build(), flags)
    }

    private fun updateServiceLayoutHints() {
        val serviceExtras = Bundle().apply {
            val layoutHint = when (mediaLibrary?.layoutType) {
                LayoutType.LIST -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_LIST_ITEM
                else -> MediaConstants.DESCRIPTION_EXTRAS_VALUE_CONTENT_STYLE_GRID_ITEM
            }
            putInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_BROWSABLE, layoutHint)
            putInt(MediaConstants.DESCRIPTION_EXTRAS_KEY_CONTENT_STYLE_PLAYABLE, layoutHint)
        }
        mediaSession.setExtras(serviceExtras)
    }

    private fun updateMediaSessionMetadata(mediaItem: AndroidAutoMediaItem) {
        val metadata = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaItem.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mediaItem.subtitle ?: "Unknown Artist")
            .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, mediaItem.id)
        
        // Set duration - use provided duration or try to get from ExoPlayer
        val duration = when {
            mediaItem.durationMs != null && mediaItem.durationMs > 0 -> mediaItem.durationMs
            exoPlayer.duration != C.TIME_UNSET && exoPlayer.duration > 0 -> exoPlayer.duration
            else -> 0L
        }
        metadata.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
        
        // Set media type specific metadata
        when (mediaItem.mediaType) {
            MediaType.VIDEO -> {
                metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, mediaItem.title)
                metadata.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Video • ${mediaItem.subtitle ?: "Unknown"}")
                metadata.putString(MediaMetadataCompat.METADATA_KEY_GENRE, "Video")
                // Add video-specific metadata
                metadata.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1)
                metadata.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1)
                // Mark as video content
                metadata.putString("android.media.metadata.CONTENT_TYPE", "video")
                metadata.putString("android.media.metadata.MEDIA_TYPE", "VIDEO")
            }
            MediaType.AUDIO -> {
                metadata.putString(MediaMetadataCompat.METADATA_KEY_ALBUM, mediaItem.metadata?.get("album")?.toString() ?: "Unknown Album")
                metadata.putString(MediaMetadataCompat.METADATA_KEY_GENRE, mediaItem.metadata?.get("genre")?.toString() ?: "Unknown Genre")
                // Mark as audio content
                metadata.putString("android.media.metadata.CONTENT_TYPE", "audio")
                metadata.putString("android.media.metadata.MEDIA_TYPE", "AUDIO")
            }
            else -> {
                // Default handling
            }
        }

        // Set artwork if available (with fallback to placeholder)
        mediaItem.iconUrl?.let { url ->
            val bitmap = ImageCache.getCachedBitmap(url, mediaItem.title)
            if (bitmap != null) {
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                metadata.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, bitmap)
                println("🖼️ AndroidAutoMediaService: Set artwork for ${mediaItem.title}")
            } else {
                println("⚠️ AndroidAutoMediaService: No artwork available for ${mediaItem.title}")
            }
        }

        mediaSession.setMetadata(metadata.build())
        println("🎵 AndroidAutoMediaService: Updated metadata for ${mediaItem.title} (${mediaItem.mediaType}) - Duration: ${duration}ms")
    }

    /**
     * Get the next playable media item ID in the current context
     * This method finds the next track in the same folder/playlist as the current track
     */
    private fun getNextMediaId(): String? {
        val currentId = currentMediaItem?.id ?: return null
        val playableItems = getAllPlayableItems()
        val currentIndex = playableItems.indexOfFirst { it.id == currentId }
        
        return if (currentIndex >= 0 && currentIndex < playableItems.size - 1) {
            playableItems[currentIndex + 1].id
        } else {
            // If at the end, optionally loop back to the beginning (depending on repeat mode)
            if (exoPlayer.repeatMode == Player.REPEAT_MODE_ALL && playableItems.isNotEmpty()) {
                playableItems[0].id
            } else {
                null
            }
        }
    }

    /**
     * Get the previous playable media item ID in the current context
     * This method finds the previous track in the same folder/playlist as the current track
     */
    private fun getPreviousMediaId(): String? {
        val currentId = currentMediaItem?.id ?: return null
        val playableItems = getAllPlayableItems()
        val currentIndex = playableItems.indexOfFirst { it.id == currentId }
        
        return if (currentIndex > 0) {
            playableItems[currentIndex - 1].id
        } else {
            // If at the beginning, optionally loop to the end (depending on repeat mode)
            if (exoPlayer.repeatMode == Player.REPEAT_MODE_ALL && playableItems.isNotEmpty()) {
                playableItems[playableItems.size - 1].id
            } else {
                null
            }
        }
    }

    /**
     * Get all playable items from the media library in a flat list
     * This creates a queue-like structure for next/previous navigation
     */
    private fun getAllPlayableItems(): List<AndroidAutoMediaItem> {
        val playableItems = mutableListOf<AndroidAutoMediaItem>()
        
        fun collectPlayableItems(items: List<AndroidAutoMediaItem>) {
            items.forEach { item ->
                if (item.isPlayable && item.mediaUrl != null) {
                    playableItems.add(item)
                }
                item.children?.let { children ->
                    collectPlayableItems(children)
                }
            }
        }
        
        mediaLibrary?.rootItems?.let { rootItems ->
            collectPlayableItems(rootItems)
        }
        
        return playableItems
    }

    private fun updatePlaybackState(state: Int) {
        // Build available actions based on queue position
        var actions = PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_SEEK_TO or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_PAUSE
        
        // Add skip actions if next/previous tracks are available
        val hasNext = getNextMediaId() != null
        val hasPrevious = getPreviousMediaId() != null
        
        if (hasNext) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        }
        
        if (hasPrevious) {
            actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
        }
        
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(actions)
            .setState(state, exoPlayer.currentPosition, exoPlayer.playbackParameters.speed)
            .build()

        mediaSession.setPlaybackState(playbackState)
        
        println("🎮 AndroidAutoMediaService: Playback state updated - State: $state, HasNext: $hasNext, HasPrevious: $hasPrevious")
    }

    // Media session callback
    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            println("🎮 MediaSessionCallback: onPlay() called (physical button or screen)")
            serviceScope.launch(Dispatchers.Main) {
                resume()
            }
        }

        override fun onPause() {
            println("🎮 MediaSessionCallback: onPause() called (physical button or screen)")
            serviceScope.launch(Dispatchers.Main) {
                pause()
            }
        }

        override fun onStop() {
            println("🎮 MediaSessionCallback: onStop() called (physical button or screen)")
            serviceScope.launch(Dispatchers.Main) {
                stop()
            }
        }

        override fun onSeekTo(pos: Long) {
            println("🎮 MediaSessionCallback: onSeekTo($pos) called")
            serviceScope.launch(Dispatchers.Main) {
                seekTo(pos)
            }
        }

        override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
            println("🎮 MediaSessionCallback: onPlayFromMediaId($mediaId) called")
            serviceScope.launch(Dispatchers.Main) {
                mediaId?.let { playMedia(it) }
            }
        }

        override fun onSkipToNext() {
            println("🎮🚗 MediaSessionCallback: onSkipToNext() called - STEERING WHEEL NEXT BUTTON PRESSED!")
            serviceScope.launch(Dispatchers.Main) {
                skipToNext()
            }
        }

        override fun onSkipToPrevious() {
            println("🎮🚗 MediaSessionCallback: onSkipToPrevious() called - STEERING WHEEL PREVIOUS BUTTON PRESSED!")
            serviceScope.launch(Dispatchers.Main) {
                skipToPrevious()
            }
        }
    }

    // ExoPlayer event listener
    private inner class PlayerEventListener : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            val state = when (playbackState) {
                Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                Player.STATE_READY -> {
                    // Update metadata with actual duration when ready
                    currentMediaItem?.let { mediaItem ->
                        updateMediaSessionMetadata(mediaItem)
                    }
                    if (exoPlayer.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                }
                Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
                else -> PlaybackStateCompat.STATE_NONE
            }
            updatePlaybackState(state)
            
            println("🎵 AndroidAutoMediaService: Playback state changed to $state (ExoPlayer state: $playbackState)")
        }

        override fun onPlayerError(error: PlaybackException) {
            println("❌ AndroidAutoMediaService: Player error - ${error.message}")
            updatePlaybackState(PlaybackStateCompat.STATE_ERROR)
        }
        
        override fun onMediaItemTransition(mediaItem: com.google.android.exoplayer2.MediaItem?, reason: Int) {
            println("🎵 AndroidAutoMediaService: Media item transition - ${mediaItem?.mediaId}")
            // Update metadata when media item changes
            currentMediaItem?.let { currentItem ->
                updateMediaSessionMetadata(currentItem)
            }
        }
    }

    // Notification components
    private inner class MediaDescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): CharSequence {
            return currentMediaItem?.title ?: "Unknown Title"
        }

        override fun createCurrentContentIntent(player: Player): android.app.PendingIntent? {
            return null
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return currentMediaItem?.subtitle
        }

        override fun getCurrentLargeIcon(player: Player, callback: PlayerNotificationManager.BitmapCallback): Bitmap? {
            return currentMediaItem?.iconUrl?.let { url ->
                ImageCache.getCachedBitmap(url)
            }
        }
    }

    private inner class NotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            stopForeground(true)
        }

        override fun onNotificationPosted(notificationId: Int, notification: Notification, ongoing: Boolean) {
            if (ongoing) {
                startForeground(notificationId, notification)
            }
        }
    }
}
