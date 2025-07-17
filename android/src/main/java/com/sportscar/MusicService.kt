

package com.sportscar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.MediaItem
import kotlinx.coroutines.*
import com.bumptech.glide.Glide
import com.sportscar.models.MediaItemData
import com.sportscar.models.MediaLibraryData
import com.sportscar.models.LayoutType
import com.sportscar.utils.JsonParser
import com.sportscar.utils.ImageCache
import com.sportscar.utils.DefaultData

class MusicService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var player: SimpleExoPlayer

    // Dynamic media library data
    private var mediaLibraryData: MediaLibraryData? = null

    override fun onCreate() {
        super.onCreate()

        // Initialize ExoPlayer
        player = SimpleExoPlayer.Builder(this).build()

        // Try to load data from SharedPreferences first, fallback to default
        loadMediaLibraryFromSharedPreferences()

        // MediaSession for Android Auto
        mediaSession = MediaSessionCompat(this, "MyMusicService")

        mediaSession.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                // Optional fallback
            }

            override fun onPause() {
                player.pause()
                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED)
            }

            override fun onPlayFromMediaId(mediaId: String?, extras: Bundle?) {
                mediaId?.let {
                    playMediaById(it)
                }
            }
        })

        // Set initial layout hints
        updateServiceLayoutHints()
        
        mediaSession.isActive = true
        sessionToken = mediaSession.sessionToken
    }

    // Method to update media library from JSON (will be called from JS)
    fun updateMediaLibraryFromJson(jsonString: String) {
        try {
            val parsedData = JsonParser.parseMediaLibraryFromJson(jsonString)
            if (parsedData != null) {
                mediaLibraryData = parsedData
                // Preload images for better performance
                ImageCache.preloadImages(applicationContext, mediaLibraryData)
                println("✅ Successfully parsed JSON data with layout: ${parsedData.layoutType}")
                
                // Notify Android Auto about the layout change
                notifyLayoutChange()
            } else {
                println("❌ Failed to parse JSON data, using fallback")
                // Fallback to default data if JSON parsing fails
                val fallbackData = JsonParser.parseMediaLibraryFromJson(DefaultData.listLayoutJsonData)
                mediaLibraryData = fallbackData
                if (fallbackData != null) {
                    ImageCache.preloadImages(applicationContext, fallbackData)
                    println("✅ Using fallback data with layout: ${fallbackData.layoutType}")
                    notifyLayoutChange()
                }
            }
        } catch (e: Exception) {
            println("❌ Exception while parsing JSON: ${e.message}")
            e.printStackTrace()
            // Fallback to default data if JSON parsing fails
            val fallbackData = JsonParser.parseMediaLibraryFromJson(DefaultData.listLayoutJsonData)
            mediaLibraryData = fallbackData
            if (fallbackData != null) {
                ImageCache.preloadImages(applicationContext, fallbackData)
                println("✅ Using fallback data after exception")
                notifyLayoutChange()
            }
        }
    }
    
    // Method to notify Android Auto about layout changes
    private fun notifyLayoutChange() {
        try {
            // Update service-level layout hints
            updateServiceLayoutHints()
            
            // Force refresh by notifying all connected clients
            notifyChildrenChanged("root_id")
            
            // Also try to force a media session update
            mediaSession.isActive = false
            mediaSession.isActive = true
            
            println("🔄 Notified Android Auto about layout change to: ${mediaLibraryData?.layoutType}")
            println("🔄 Media session reactivated to force layout refresh")
        } catch (e: Exception) {
            println("❌ Failed to notify layout change: ${e.message}")
        }
    }
    
    // Method to load media library data from SharedPreferences
    private fun loadMediaLibraryFromSharedPreferences() {
        try {
            val sharedPrefs = getSharedPreferences("MediaLibraryPrefs", Context.MODE_PRIVATE)
            val jsonString = sharedPrefs.getString("media_library_data", null)
            
            if (jsonString != null) {
                println("📖 Loading media library data from SharedPreferences")
                updateMediaLibraryFromJson(jsonString)
            } else {
                println("ℹ️ No data in SharedPreferences, using default data")
                updateMediaLibraryFromJson(DefaultData.mixedIconsJsonData)
            }
        } catch (e: Exception) {
            println("❌ Failed to load from SharedPreferences: ${e.message}")
            println("🔄 Falling back to default data")
            updateMediaLibraryFromJson(DefaultData.mixedIconsJsonData)
        }
    }
    
    // Method to update service-level layout hints
    private fun updateServiceLayoutHints() {
        try {
            val layoutHint = when (mediaLibraryData?.layoutType) {
                LayoutType.LIST -> 1 // List hint
                else -> 2 // Grid hint (default)
            }
            
            // Set service-level layout hints
            val serviceExtras = Bundle().apply {
                putInt("android.media.browse.CONTENT_STYLE_SUPPORTED", 1)
                putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", layoutHint)
                putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", layoutHint)
            }
            
            // Update the media session with new hints
            mediaSession.setExtras(serviceExtras)
            
            println("🎛️ Updated service-level layout hints to: ${mediaLibraryData?.layoutType} (hint: $layoutHint)")
        } catch (e: Exception) {
            println("❌ Failed to update service layout hints: ${e.message}")
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        // Add content style hints based on layout type
        val extras = Bundle().apply {
            putInt("android.media.browse.CONTENT_STYLE_SUPPORTED", 1)
            // Set layout hints based on the current layout type
            val layoutHint = when (mediaLibraryData?.layoutType) {
                LayoutType.LIST -> 1 // List hint
                else -> 2 // Grid hint (default)
            }
            putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", layoutHint)
            putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", layoutHint)
            println("🏠 Root hints set - Layout: ${mediaLibraryData?.layoutType}, Hint: $layoutHint")
        }
        return BrowserRoot("root_id", extras)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            "root_id" -> {
                val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                mediaLibraryData?.rootItems?.forEach { item ->
                    mediaItems.add(createMediaItemFromData(item))
                }
                result.sendResult(mediaItems)
            }
            else -> {
                // Find the item by ID in the media library data
                val item = JsonParser.findMediaItemById(parentId, mediaLibraryData?.rootItems)
                if (item != null && item.children != null) {
                    val mediaItems = mutableListOf<MediaBrowserCompat.MediaItem>()
                    item.children.forEach { child ->
                        mediaItems.add(createMediaItemFromData(child))
                    }
                    result.sendResult(mediaItems)
                } else {
                    result.sendResult(mutableListOf())
                }
            }
        }
    }



    private fun createMediaItemFromData(data: MediaItemData): MediaBrowserCompat.MediaItem {
        val builder = MediaDescriptionCompat.Builder()
            .setMediaId(data.id)
            .setTitle(data.title)
            .setSubtitle(data.subtitle)
        
        // Get cached bitmap or load it (only if iconUrl is provided and not empty)
        data.iconUrl?.let { url ->
            if (url.isNotBlank()) {
                ImageCache.getCachedBitmap(url)?.let { bitmap ->
                    builder.setIconBitmap(bitmap)
                    println("🖼️ Set icon for ${data.title}")
                } ?: println("⚠️ No cached bitmap for ${data.title}")
            } else {
                println("ℹ️ No icon URL for ${data.title}")
            }
        } ?: println("ℹ️ No icon URL provided for ${data.title}")
        
        // Add extras to hint for layout type
        val extras = Bundle().apply {
            putInt("android.media.browse.CONTENT_STYLE_SUPPORTED", 1)
            // Set layout hints based on the layout type
            val layoutHint = when (mediaLibraryData?.layoutType) {
                LayoutType.LIST -> {
                    println("📋 Setting LIST layout hints for ${data.title}")
                    1 // List hint
                }
                else -> {
                    println("🔲 Setting GRID layout hints for ${data.title}")
                    2 // Grid hint
                }
            }
            
            // Set the appropriate hint based on whether the item is playable or browsable
            if (data.isPlayable) {
                putInt("android.media.browse.CONTENT_STYLE_PLAYABLE_HINT", layoutHint)
            } else {
                putInt("android.media.browse.CONTENT_STYLE_BROWSABLE_HINT", layoutHint)
            }
        }
        builder.setExtras(extras)
        
        val flags = if (data.isPlayable) {
            MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
        } else {
            MediaBrowserCompat.MediaItem.FLAG_BROWSABLE
        }
        
        return MediaBrowserCompat.MediaItem(builder.build(), flags)
    }

    private fun playMediaById(mediaId: String) {
        // Find the media item and get its URL
        val mediaItemData = JsonParser.findMediaItemById(mediaId, mediaLibraryData?.rootItems)
        val mediaUrl = mediaItemData?.mediaUrl
        
        mediaUrl?.let {
            val exoMediaItem = MediaItem.fromUri(it)
            player.setMediaItem(exoMediaItem)
            player.prepare()
            player.play()

            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING)

            val metadata = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, mediaItemData?.title ?: mediaId)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mediaItemData?.subtitle ?: "Demo Artist")
                .build()

            mediaSession.setMetadata(metadata)
        }
    }

    private fun updatePlaybackState(state: Int) {
        val playbackState = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID
            )
            .setState(state, player.currentPosition, 1.0f)
            .build()

        mediaSession.setPlaybackState(playbackState)
    }
}
