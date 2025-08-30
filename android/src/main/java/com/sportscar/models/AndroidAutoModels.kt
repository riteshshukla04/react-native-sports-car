package com.sportscar.models

/**
 * Layout type for the Android Auto media browser
 */
enum class LayoutType {
    GRID,
    LIST
}

/**
 * Media type for different kinds of content
 */
enum class MediaType {
    AUDIO,
    VIDEO,
    FOLDER
}

/**
 * Media item that can be displayed in Android Auto
 */
data class AndroidAutoMediaItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val iconUrl: String? = null,
    val isPlayable: Boolean = false,
    val mediaUrl: String? = null,
    val mediaType: MediaType = MediaType.AUDIO,
    val durationMs: Long? = null,
    val children: List<AndroidAutoMediaItem>? = null,
    val layoutType: LayoutType? = null,
    val metadata: Map<String, Any>? = null
)

/**
 * Media library structure for Android Auto
 */
data class MediaLibrary(
    val layoutType: LayoutType = LayoutType.GRID,
    val rootItems: List<AndroidAutoMediaItem>,
    val appName: String? = null,
    val appIconUrl: String? = null
)

/**
 * Current playback information
 */
data class PlaybackInfo(
    val state: String,
    val currentMediaId: String? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val shuffleEnabled: Boolean = false,
    val repeatMode: String = "none"
)

/**
 * Configuration options for Android Auto
 */
data class AndroidAutoConfig(
    val enableDebugLogging: Boolean = false,
    val colorScheme: ColorScheme? = null,
    val maxItemsPerPage: Int = 50,
    val enableImageCaching: Boolean = true,
    val imageCacheSize: Int = 100 // MB
)

/**
 * Color scheme configuration
 */
data class ColorScheme(
    val primary: String? = null,
    val secondary: String? = null,
    val background: String? = null,
    val text: String? = null
)
