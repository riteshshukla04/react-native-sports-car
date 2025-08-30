package com.sportscar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.turbomodule.core.interfaces.TurboModule
import com.sportscar.service.AndroidAutoMediaService
import com.sportscar.models.MediaLibrary
import com.sportscar.utils.MediaLibraryParser
import kotlinx.coroutines.*

/**
 * Turbo Module implementation for Android Auto
 * This implements the new architecture while maintaining the same functionality
 */
@ReactModule(name = AndroidAutoTurboModule.NAME)
class AndroidAutoTurboModule(reactContext: ReactApplicationContext) : 
    NativeSportscarSpecSpec(reactContext) {

    companion object {
        const val NAME = "AndroidAutoModule"
    }

    private var mediaService: AndroidAutoMediaService? = null
    private var serviceBound = false
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AndroidAutoMediaService.LocalBinder
            mediaService = binder.getService()
            serviceBound = true
            println("🔗 AndroidAutoTurboModule: Connected to media service")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            mediaService = null
            serviceBound = false
            println("❌ AndroidAutoTurboModule: Disconnected from media service")
        }
    }

    override fun getName(): String = NAME

    override fun initialize() {
        super.initialize()
        bindToMediaService()
    }

    override fun invalidate() {
        super.invalidate()
        unbindFromMediaService()
        serviceScope.cancel()
    }

    private fun bindToMediaService() {
        try {
            val intent = Intent(reactApplicationContext, AndroidAutoMediaService::class.java)
            reactApplicationContext.startService(intent)
            reactApplicationContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
            println("🚀 AndroidAutoTurboModule: Binding to media service")
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to bind to service - ${e.message}")
        }
    }

    private fun unbindFromMediaService() {
        try {
            if (serviceBound) {
                reactApplicationContext.unbindService(serviceConnection)
                serviceBound = false
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Error unbinding service - ${e.message}")
        }
    }

    /**
     * Initialize the Android Auto media service with a media library
     */
    override fun initializeMediaLibrary(mediaLibraryJson: String, promise: Promise) {
        serviceScope.launch {
            try {
                val mediaLibrary = MediaLibraryParser.parseFromJson(mediaLibraryJson)
                if (mediaLibrary != null) {
                    mediaService?.initializeMediaLibrary(mediaLibrary)
                    promise.resolve(true)
                    println("✅ AndroidAutoTurboModule: Media library initialized")
                } else {
                    promise.reject("PARSE_ERROR", "Failed to parse media library JSON")
                }
            } catch (e: Exception) {
                println("❌ AndroidAutoTurboModule: Failed to initialize media library - ${e.message}")
                promise.reject("INIT_ERROR", "Failed to initialize media library", e)
            }
        }
    }

    /**
     * Update the media library with new content
     */
    override fun updateMediaLibrary(mediaLibraryJson: String, promise: Promise) {
        serviceScope.launch {
            try {
                val mediaLibrary = MediaLibraryParser.parseFromJson(mediaLibraryJson)
                if (mediaLibrary != null) {
                    mediaService?.updateMediaLibrary(mediaLibrary)
                    promise.resolve(true)
                    println("✅ AndroidAutoTurboModule: Media library updated")
                } else {
                    promise.reject("PARSE_ERROR", "Failed to parse media library JSON")
                }
            } catch (e: Exception) {
                println("❌ AndroidAutoTurboModule: Failed to update media library - ${e.message}")
                promise.reject("UPDATE_ERROR", "Failed to update media library", e)
            }
        }
    }

    /**
     * Set the layout type for the media browser
     */
    override fun setLayoutType(layoutType: String, promise: Promise) {
        try {
            mediaService?.setLayoutType(layoutType)
            promise.resolve(true)
            println("✅ AndroidAutoTurboModule: Layout type set to $layoutType")
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to set layout type - ${e.message}")
            promise.reject("LAYOUT_ERROR", "Failed to set layout type", e)
        }
    }

    /**
     * Force refresh the Android Auto UI
     */
    override fun refreshAndroidAutoUI(promise: Promise) {
        try {
            mediaService?.refreshAndroidAutoUI()
            promise.resolve(true)
            println("✅ AndroidAutoTurboModule: Android Auto UI refreshed")
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to refresh UI - ${e.message}")
            promise.reject("REFRESH_ERROR", "Failed to refresh Android Auto UI", e)
        }
    }

    /**
     * Play media by ID
     */
    override fun playMedia(mediaId: String, promise: Promise) {
        try {
            val success = mediaService?.playMedia(mediaId) ?: false
            promise.resolve(success)
            if (success) {
                println("▶️ AndroidAutoTurboModule: Playing media $mediaId")
                sendEvent("playbackStateChanged", createPlaybackStateMap("playing", mediaId))
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to play media - ${e.message}")
            promise.reject("PLAY_ERROR", "Failed to play media", e)
        }
    }

    /**
     * Pause current playback
     */
    override fun pause(promise: Promise) {
        try {
            val success = mediaService?.pause() ?: false
            promise.resolve(success)
            if (success) {
                println("⏸️ AndroidAutoTurboModule: Playback paused")
                sendEvent("playbackStateChanged", createPlaybackStateMap("paused"))
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to pause - ${e.message}")
            promise.reject("PAUSE_ERROR", "Failed to pause playback", e)
        }
    }

    /**
     * Resume current playback
     */
    override fun resume(promise: Promise) {
        try {
            val success = mediaService?.resume() ?: false
            promise.resolve(success)
            if (success) {
                println("▶️ AndroidAutoTurboModule: Playback resumed")
                sendEvent("playbackStateChanged", createPlaybackStateMap("playing"))
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to resume - ${e.message}")
            promise.reject("RESUME_ERROR", "Failed to resume playback", e)
        }
    }

    /**
     * Stop current playback
     */
    override fun stop(promise: Promise) {
        try {
            val success = mediaService?.stop() ?: false
            promise.resolve(success)
            if (success) {
                println("⏹️ AndroidAutoTurboModule: Playback stopped")
                sendEvent("playbackStateChanged", createPlaybackStateMap("stopped"))
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to stop - ${e.message}")
            promise.reject("STOP_ERROR", "Failed to stop playback", e)
        }
    }

    /**
     * Seek to a specific position
     */
    override fun seekTo(positionMs: Double, promise: Promise) {
        try {
            val success = mediaService?.seekTo(positionMs.toLong()) ?: false
            promise.resolve(success)
            if (success) {
                println("⏩ AndroidAutoTurboModule: Seeked to ${positionMs}ms")
                sendEvent("positionChanged", createPositionMap(positionMs.toLong()))
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to seek - ${e.message}")
            promise.reject("SEEK_ERROR", "Failed to seek", e)
        }
    }

    /**
     * Get current playback state
     */
    override fun getPlaybackState(promise: Promise) {
        try {
            val playbackInfo = mediaService?.getPlaybackInfo()
            if (playbackInfo != null) {
                val map = Arguments.createMap().apply {
                    putString("state", playbackInfo.state)
                    putString("currentMediaId", playbackInfo.currentMediaId)
                    putDouble("positionMs", playbackInfo.positionMs.toDouble())
                    putDouble("durationMs", playbackInfo.durationMs.toDouble())
                    putDouble("playbackSpeed", playbackInfo.playbackSpeed.toDouble())
                    putBoolean("shuffleEnabled", playbackInfo.shuffleEnabled)
                    putString("repeatMode", playbackInfo.repeatMode)
                }
                promise.resolve(map)
            } else {
                promise.resolve(null)
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to get playback state - ${e.message}")
            promise.reject("STATE_ERROR", "Failed to get playback state", e)
        }
    }

    /**
     * Set playback speed
     */
    override fun setPlaybackSpeed(speed: Double, promise: Promise) {
        try {
            val success = mediaService?.setPlaybackSpeed(speed.toFloat()) ?: false
            promise.resolve(success)
            if (success) {
                println("🏃 AndroidAutoTurboModule: Playback speed set to ${speed}x")
            }
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to set playback speed - ${e.message}")
            promise.reject("SPEED_ERROR", "Failed to set playback speed", e)
        }
    }

    /**
     * Send event to React Native
     */
    private fun sendEvent(eventName: String, params: WritableMap?) {
        try {
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        } catch (e: Exception) {
            println("❌ AndroidAutoTurboModule: Failed to send event $eventName - ${e.message}")
        }
    }

    /**
     * Create playback state map for events
     */
    private fun createPlaybackStateMap(state: String, mediaId: String? = null): WritableMap {
        return Arguments.createMap().apply {
            putString("type", "playbackStateChanged")
            putMap("data", Arguments.createMap().apply {
                putString("state", state)
                mediaId?.let { putString("mediaId", it) }
                putDouble("timestamp", System.currentTimeMillis().toDouble())
            })
        }
    }

    /**
     * Create position map for events
     */
    private fun createPositionMap(positionMs: Long): WritableMap {
        return Arguments.createMap().apply {
            putString("type", "positionChanged")
            putMap("data", Arguments.createMap().apply {
                putDouble("positionMs", positionMs.toDouble())
                putDouble("timestamp", System.currentTimeMillis().toDouble())
            })
        }
    }
}
