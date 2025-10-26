
package com.margelo.nitro.sportscar

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.facebook.proguard.annotations.DoNotStrip
import com.margelo.nitro.core.Promise
import com.margelo.nitro.NitroModules
import com.sportscar.service.AndroidAutoMediaService
import com.sportscar.models.*
import com.sportscar.utils.MediaLibraryParser
import kotlinx.coroutines.*

@DoNotStrip
class Sportscar : HybridSportscarSpec() {
  
  private var mediaService: AndroidAutoMediaService? = null
  private var serviceBound = false
  private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

  private val serviceConnection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
      val binder = service as AndroidAutoMediaService.LocalBinder
      mediaService = binder.getService()
      serviceBound = true
      println("🔗 Sportscar: Connected to media service")
    }

    override fun onServiceDisconnected(name: ComponentName?) {
      mediaService = null
      serviceBound = false
      println("❌ Sportscar: Disconnected from media service")
    }
  }

  init {
    bindToMediaService()
  }

  private fun bindToMediaService() {
    try {
      val context = NitroModules.applicationContext ?: throw Error("Cannot get Android Context - No Context available!")
      val intent = Intent(context, AndroidAutoMediaService::class.java)
      context.startService(intent)
      context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
      println("🚀 Sportscar: Binding to media service")
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to bind to service - ${e.message}")
    }
  }

  private fun unbindFromMediaService() {
    try {
      if (serviceBound) {
        val context = NitroModules.applicationContext
        context?.unbindService(serviceConnection)
        serviceBound = false
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Error unbinding service - ${e.message}")
    }
  }

  override fun initializeMediaLibrary(mediaLibraryJson: String): Promise<Boolean> {
    val promise = Promise<Boolean>()
    serviceScope.launch {
      try {
        val mediaLibrary = MediaLibraryParser.parseFromJson(mediaLibraryJson)
        if (mediaLibrary != null) {
          mediaService?.initializeMediaLibrary(mediaLibrary)
          promise.resolve(true)
          println("✅ Sportscar: Media library initialized")
        } else {
          promise.reject(Exception("Failed to parse media library JSON"))
        }
      } catch (e: Exception) {
        println("❌ Sportscar: Failed to initialize media library - ${e.message}")
        promise.reject(e)
      }
    }
    return promise
  }

  override fun updateMediaLibrary(mediaLibraryJson: String): Promise<Boolean> {
    val promise = Promise<Boolean>()
    serviceScope.launch {
      try {
        val mediaLibrary = MediaLibraryParser.parseFromJson(mediaLibraryJson)
        if (mediaLibrary != null) {
          mediaService?.updateMediaLibrary(mediaLibrary)
          promise.resolve(true)
          println("✅ Sportscar: Media library updated")
        } else {
          promise.reject(Exception("Failed to parse media library JSON"))
        }
      } catch (e: Exception) {
        println("❌ Sportscar: Failed to update media library - ${e.message}")
        promise.reject(e)
      }
    }
    return promise
  }

  override fun getMediaLibrary(): Promise<String> {
    val promise = Promise<String>()
    try {
      val mediaLibrary = mediaService?.getMediaLibrary()
      if (mediaLibrary != null) {
        val jsonString = MediaLibraryParser.toJson(mediaLibrary)
        promise.resolve(jsonString)
        println("📚 Sportscar: Media library retrieved")
      } else {
        promise.reject(Exception("No media library available"))
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to get media library - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun setLayoutType(layoutType: String): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      mediaService?.setLayoutType(layoutType)
      promise.resolve(true)
      println("✅ Sportscar: Layout type set to $layoutType")
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to set layout type - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun refreshAndroidAutoUI(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      mediaService?.refreshAndroidAutoUI()
      promise.resolve(true)
      println("✅ Sportscar: Android Auto UI refreshed")
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to refresh UI - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun playMedia(mediaId: String): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.playMedia(mediaId) ?: false
      promise.resolve(success)
      if (success) {
        println("▶️ Sportscar: Playing media $mediaId")
        // Note: In Nitro modules, we'll need to use callbacks instead of events
        // This will be handled by the JavaScript side
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to play media - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun pause(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.pause() ?: false
      promise.resolve(success)
      if (success) {
        println("⏸️ Sportscar: Playback paused")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to pause - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun resume(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.resume() ?: false
      promise.resolve(success)
      if (success) {
        println("▶️ Sportscar: Playback resumed")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to resume - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun stop(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.stop() ?: false
      promise.resolve(success)
      if (success) {
        println("⏹️ Sportscar: Playback stopped")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to stop - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun seekTo(positionMs: Double): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.seekTo(positionMs.toLong()) ?: false
      promise.resolve(success)
      if (success) {
        println("⏩ Sportscar: Seeked to ${positionMs}ms")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to seek - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun setPlaybackSpeed(speed: Double): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.setPlaybackSpeed(speed.toFloat()) ?: false
      promise.resolve(success)
      if (success) {
        println("🏃 Sportscar: Playback speed set to ${speed}x")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to set playback speed - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun skipToNext(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.skipToNext() ?: false
      promise.resolve(success)
      if (success) {
        println("⏭️ Sportscar: Skipped to next track")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to skip to next - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun skipToPrevious(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.skipToPrevious() ?: false
      promise.resolve(success)
      if (success) {
        println("⏮️ Sportscar: Skipped to previous track")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to skip to previous - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun getPlaybackState(): Promise<PlaybackInfo> {
    val promise = Promise<PlaybackInfo>()
    try {
      val playbackInfo = mediaService?.getPlaybackInfo()
      if (playbackInfo != null) {
        val info = PlaybackInfo(
          state = when (playbackInfo.state) {
            "playing" -> PlaybackState.PLAYING
            "paused" -> PlaybackState.PAUSED
            "stopped" -> PlaybackState.STOPPED
            "buffering" -> PlaybackState.BUFFERING
            "error" -> PlaybackState.ERROR
            else -> PlaybackState.STOPPED
          },
          currentMediaId = playbackInfo.currentMediaId,
          positionMs = playbackInfo.positionMs.toDouble(),
          durationMs = playbackInfo.durationMs.toDouble(),
          playbackSpeed = playbackInfo.playbackSpeed.toDouble(),
          shuffleEnabled = playbackInfo.shuffleEnabled,
          repeatMode = when (playbackInfo.repeatMode) {
            "none" -> RepeatMode.NONE
            "one" -> RepeatMode.ONE
            "all" -> RepeatMode.ALL
            else -> RepeatMode.NONE
          }
        )
        promise.resolve(info)
      } else {
        promise.reject(Exception("No playback state available"))
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to get playback state - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun isCurrentlyPlaying(): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val isPlaying = mediaService?.isCurrentlyPlaying() ?: false
      promise.resolve(isPlaying)
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to check playing state - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun getLastPlayedMediaInfo(): Promise<LastPlayedMediaInfo?> {
    val promise = Promise<LastPlayedMediaInfo?>()
    try {
      val mediaInfo = mediaService?.getLastPlayedMediaInfo()
      if (mediaInfo != null) {
        val info = LastPlayedMediaInfo(
          mediaId = mediaInfo.first,
          positionMs = mediaInfo.second.toDouble()
        )
        promise.resolve(info)
      } else {
        promise.resolve(null)
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to get last played media info - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun handleAppStateChange(appState: String): Promise<Boolean> {
    val promise = Promise<Boolean>()
    try {
      val success = mediaService?.handleAppStateChange(appState) ?: false
      promise.resolve(success)
      if (success) {
        println("📱 Sportscar: App state changed to $appState")
      }
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to handle app state change - ${e.message}")
      promise.reject(e)
    }
    return promise
  }

  override fun getCurrentAppState(): Promise<AppState> {
    val promise = Promise<AppState>()
    try {
      val appState = mediaService?.getCurrentAppState() ?: "foreground"
      promise.resolve(when (appState) {
        "foreground" -> AppState.FOREGROUND
        "background" -> AppState.BACKGROUND
        "destroyed" -> AppState.DESTROYED
        else -> AppState.FOREGROUND
      })
    } catch (e: Exception) {
      println("❌ Sportscar: Failed to get app state - ${e.message}")
      promise.reject(e)
    }
    return promise
  }
}
