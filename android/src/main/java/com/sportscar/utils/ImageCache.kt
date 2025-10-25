package com.sportscar.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.sportscar.models.AndroidAutoMediaItem
import com.sportscar.models.MediaLibrary
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object ImageCache {
    
    private val cachedBitmaps = ConcurrentHashMap<String, Bitmap>()
    private val failedUrls = ConcurrentHashMap<String, Long>() // URL -> timestamp
    private val PLACEHOLDER_CACHE = ConcurrentHashMap<String, Bitmap>()
    
    // Cache failed URLs for 5 minutes to avoid repeated failed requests
    private const val FAILED_URL_CACHE_DURATION = 5 * 60 * 1000L // 5 minutes
    
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
                    loadImageWithFallback(context, url, item.title)
                }
            }
            
            item.children?.let { children ->
                preloadImagesRecursive(context, children)
            }
        }
    }
    
    /**
     * Load image with proper error handling and fallback
     * @param context Application context
     * @param url Image URL
     * @param title Item title for placeholder
     */
    private suspend fun loadImageWithFallback(context: Context, url: String, title: String) {
        // Check if this URL recently failed
        val failedTimestamp = failedUrls[url]
        if (failedTimestamp != null && System.currentTimeMillis() - failedTimestamp < FAILED_URL_CACHE_DURATION) {
            println("🖼️ ImageCache: Skipping recently failed URL: $url")
            return
        }
        
        try {
            val bitmap = withContext(Dispatchers.IO) {
                Glide.with(context)
                    .asBitmap()
                    .load(url)
                    .timeout(10000) // 10 second timeout
                    .listener(object : RequestListener<Bitmap> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Bitmap>,
                            isFirstResource: Boolean
                        ): Boolean {
                            println("❌ ImageCache: Failed to load image: $url - ${e?.message}")
                            failedUrls[url] = System.currentTimeMillis()
                            return false
                        }
                        
                        override fun onResourceReady(
                            resource: Bitmap,
                            model: Any,
                            target: Target<Bitmap>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            println("✅ ImageCache: Successfully loaded image: $url")
                            return false
                        }
                    })
                    .submit()
                    .get()
            }
            
            if (bitmap != null) {
                cachedBitmaps[url] = bitmap
                // Remove from failed URLs if it was previously failed
                failedUrls.remove(url)
            }
        } catch (e: Exception) {
            println("❌ ImageCache: Exception loading image: $url - ${e.message}")
            failedUrls[url] = System.currentTimeMillis()
            
            // Create a placeholder bitmap for failed loads
            val placeholder = createPlaceholderBitmap(title)
            if (placeholder != null) {
                cachedBitmaps[url] = placeholder
            }
        }
    }
    
    /**
     * Create a placeholder bitmap for failed image loads
     * @param title Item title to display on placeholder
     * @return Placeholder bitmap or null if creation fails
     */
    private fun createPlaceholderBitmap(title: String): Bitmap? {
        return try {
            val size = 200 // Standard size for Android Auto
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Background color
            canvas.drawColor(Color.parseColor("#424242"))
            
            // Text paint
            val paint = Paint().apply {
                color = Color.WHITE
                textSize = 24f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            
            // Draw title (truncated if too long)
            val displayTitle = if (title.length > 15) {
                title.substring(0, 12) + "..."
            } else {
                title
            }
            
            val textBounds = Rect()
            paint.getTextBounds(displayTitle, 0, displayTitle.length, textBounds)
            val x = size / 2f
            val y = size / 2f + textBounds.height() / 2f
            
            canvas.drawText(displayTitle, x, y, paint)
            
            bitmap
        } catch (e: Exception) {
            println("❌ ImageCache: Failed to create placeholder bitmap: ${e.message}")
            null
        }
    }
    
    /**
     * Get cached bitmap for a given URL with fallback to placeholder
     * @param url Image URL
     * @param title Item title for placeholder fallback
     * @return Cached bitmap, placeholder, or null if not found
     */
    fun getCachedBitmap(url: String, title: String? = null): Bitmap? {
        return cachedBitmaps[url] ?: run {
            // If no cached bitmap and we have a title, create a placeholder
            title?.let { 
                val placeholder = createPlaceholderBitmap(it)
                if (placeholder != null) {
                    cachedBitmaps[url] = placeholder
                }
                placeholder
            }
        }
    }
    
    /**
     * Get cached bitmap for a given URL (legacy method)
     * @param url Image URL
     * @return Cached bitmap or null if not found
     */
    fun getCachedBitmap(url: String): Bitmap? {
        return cachedBitmaps[url]
    }
    
    /**
     * Clear all cached bitmaps and failed URLs
     */
    fun clearCache() {
        cachedBitmaps.clear()
        failedUrls.clear()
        PLACEHOLDER_CACHE.clear()
    }
    
    /**
     * Get cache size
     * @return Number of cached images
     */
    fun getCacheSize(): Int {
        return cachedBitmaps.size
    }
    
    /**
     * Get failed URLs count
     * @return Number of failed URLs
     */
    fun getFailedUrlsCount(): Int {
        return failedUrls.size
    }
    
    /**
     * Clear expired failed URLs
     */
    fun clearExpiredFailedUrls() {
        val currentTime = System.currentTimeMillis()
        failedUrls.entries.removeAll { (_, timestamp) ->
            currentTime - timestamp > FAILED_URL_CACHE_DURATION
        }
    }
} 