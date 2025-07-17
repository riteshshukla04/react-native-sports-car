package com.sportscar.utils

import com.sportscar.models.MediaItemData
import com.sportscar.models.MediaLibraryData
import com.sportscar.models.LayoutType
import org.json.JSONObject
import org.json.JSONArray
import org.json.JSONException

object JsonParser {
    
    /**
     * Parse JSON string into MediaLibraryData
     * @param jsonString JSON string containing media library data
     * @return MediaLibraryData object or null if parsing fails
     */
    fun parseMediaLibraryFromJson(jsonString: String): MediaLibraryData? {
        return try {
            println("🔍 Parsing JSON: ${jsonString.take(100)}...")
            val jsonObject = JSONObject(jsonString)
            val result = parseMediaLibraryData(jsonObject)
            println("✅ JSON parsing successful")
            result
        } catch (e: JSONException) {
            println("❌ JSON parsing failed: ${e.message}")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            println("❌ Unexpected error during JSON parsing: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parse JSONObject into MediaLibraryData
     * @param jsonObject JSONObject containing media library data
     * @return MediaLibraryData object
     */
    private fun parseMediaLibraryData(jsonObject: JSONObject): MediaLibraryData {
        val rootItemsArray = jsonObject.getJSONArray("rootItems")
        val rootItems = mutableListOf<MediaItemData>()
        
        for (i in 0 until rootItemsArray.length()) {
            val itemJson = rootItemsArray.getJSONObject(i)
            rootItems.add(parseMediaItemData(itemJson))
        }
        
        // Parse layout type (default to GRID if not specified)
        val layoutType = if (jsonObject.has("layoutType")) {
            val layoutString = jsonObject.getString("layoutType").uppercase()
            println("📱 Layout type from JSON: $layoutString")
            when (layoutString) {
                "LIST" -> LayoutType.LIST
                else -> LayoutType.GRID
            }
        } else {
            println("📱 No layout type specified, defaulting to GRID")
            LayoutType.GRID
        }
        
        println("📱 Final layout type: $layoutType")
        return MediaLibraryData(rootItems, layoutType)
    }
    
    /**
     * Parse JSONObject into MediaItemData
     * @param jsonObject JSONObject containing media item data
     * @return MediaItemData object
     */
    private fun parseMediaItemData(jsonObject: JSONObject): MediaItemData {
        val id = jsonObject.getString("id")
        val title = jsonObject.getString("title")
        val subtitle = if (jsonObject.has("subtitle")) jsonObject.getString("subtitle") else null
        val iconUrl = if (jsonObject.has("iconUrl") && !jsonObject.isNull("iconUrl")) jsonObject.getString("iconUrl") else null
        val isPlayable = if (jsonObject.has("isPlayable")) jsonObject.getBoolean("isPlayable") else false
        val mediaUrl = if (jsonObject.has("mediaUrl") && !jsonObject.isNull("mediaUrl")) jsonObject.getString("mediaUrl") else null
        
        val children = if (jsonObject.has("children")) {
            val childrenArray = jsonObject.getJSONArray("children")
            val childrenList = mutableListOf<MediaItemData>()
            for (i in 0 until childrenArray.length()) {
                childrenList.add(parseMediaItemData(childrenArray.getJSONObject(i)))
            }
            childrenList
        } else null
        
        return MediaItemData(id, title, subtitle, iconUrl, isPlayable, mediaUrl, children)
    }
    
    /**
     * Find MediaItemData by ID recursively in the media library
     * @param id ID to search for
     * @param items List of MediaItemData to search in
     * @return MediaItemData if found, null otherwise
     */
    fun findMediaItemById(id: String, items: List<MediaItemData>?): MediaItemData? {
        items?.forEach { item ->
            if (item.id == id) {
                return item
            }
            item.children?.let { children ->
                val found = findMediaItemById(id, children)
                if (found != null) {
                    return found
                }
            }
        }
        return null
    }
} 