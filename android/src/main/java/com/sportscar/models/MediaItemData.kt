package com.sportscar.models

data class MediaItemData(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val iconUrl: String? = null,
    val isPlayable: Boolean = false,
    val mediaUrl: String? = null,
    val children: List<MediaItemData>? = null
)

data class MediaLibraryData(
    val rootItems: List<MediaItemData>,
    val layoutType: LayoutType = LayoutType.GRID
)

enum class LayoutType {
    GRID,
    LIST
} 