package com.sportscar.utils

import android.content.Context
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.sportscar.models.AndroidAutoMediaItem
import com.sportscar.models.MediaLibrary
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object ImageCache {
    
    private val cachedBitmaps = ConcurrentHashMap<String, Bitmap>()
    
    /**
     * Preload all images from media library data
     * @param context Application context
     * @param mediaLibrary Media library data containing image URLs
     */
    fun preloadImages(context: Context, mediaLibrary: MediaLibrary?) {
        mediaLibrary?.let { data ->
            GlobalScope.launch(Dispatchers.IO) {
                preloadImagesRecursive(context, data.rootItems)
            }
        }
    }
    
    /**
     * Recursively preload images from media items
     * @param context Application context
     * @param items List of media items to process
     */
    private suspend fun preloadImagesRecursive(context: Context, items: List<AndroidAutoMediaItem>) {
        items.forEach { item ->
            item.iconUrl?.let { url ->
                if (url.isNotBlank() && !cachedBitmaps.containsKey(url)) {
                    try {
                        val bitmap = Glide.with(context)
                            .asBitmap()
                            .load(url)
                            .submit()
                            .get()
                        cachedBitmaps[url] = bitmap
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Add a placeholder or default bitmap for failed loads
                        // This ensures the UI doesn't break when images fail to load
                    }
                }
            }
            
            item.children?.let { children ->
                preloadImagesRecursive(context, children)
            }
        }
    }
    
    /**
     * Get cached bitmap for a given URL
     * @param url Image URL
     * @return Cached bitmap or null if not found
     */
    fun getCachedBitmap(url: String): Bitmap? {
        return cachedBitmaps[url]
    }
    
    /**
     * Clear all cached bitmaps
     */
    fun clearCache() {
        cachedBitmaps.clear()
    }
    
    /**
     * Get cache size
     * @return Number of cached images
     */
    fun getCacheSize(): Int {
        return cachedBitmaps.size
    }
} 