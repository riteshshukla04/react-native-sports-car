package com.sportscar.utils

import com.sportscar.models.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

object MediaLibraryParser {
    
    /**
     * Parse JSON string into MediaLibrary
     * @param jsonString JSON string containing media library data
     * @return MediaLibrary object or null if parsing fails
     */
    fun parseFromJson(jsonString: String): MediaLibrary? {
        return try {
            println("🔍 MediaLibraryParser: Parsing JSON (${jsonString.length} chars)")
            val jsonObject = JSONObject(jsonString)
            val result = parseMediaLibrary(jsonObject)
            println("✅ MediaLibraryParser: Successfully parsed media library")
            result
        } catch (e: JSONException) {
            println("❌ MediaLibraryParser: JSON parsing failed - ${e.message}")
            e.printStackTrace()
            null
        } catch (e: Exception) {
            println("❌ MediaLibraryParser: Unexpected error - ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Parse JSONObject into MediaLibrary
     */
    private fun parseMediaLibrary(jsonObject: JSONObject): MediaLibrary {
        // Parse layout type
        val layoutType = if (jsonObject.has("layoutType")) {
            val layoutString = jsonObject.getString("layoutType").uppercase()
            when (layoutString) {
                "LIST" -> LayoutType.LIST
                else -> LayoutType.GRID
            }
        } else {
            LayoutType.GRID
        }
        
        // Parse root items
        val rootItemsArray = jsonObject.getJSONArray("rootItems")
        val rootItems = mutableListOf<AndroidAutoMediaItem>()
        
        for (i in 0 until rootItemsArray.length()) {
            val itemJson = rootItemsArray.getJSONObject(i)
            rootItems.add(parseMediaItem(itemJson))
        }
        
        // Parse optional fields
        val appName = if (jsonObject.has("appName")) jsonObject.getString("appName") else null
        val appIconUrl = if (jsonObject.has("appIconUrl")) jsonObject.getString("appIconUrl") else null
        
        println("📱 MediaLibraryParser: Layout=$layoutType, Items=${rootItems.size}, App=$appName")
        
        return MediaLibrary(
            layoutType = layoutType,
            rootItems = rootItems,
            appName = appName,
            appIconUrl = appIconUrl
        )
    }
    
    /**
     * Parse JSONObject into MediaItem
     */
    private fun parseMediaItem(jsonObject: JSONObject): AndroidAutoMediaItem {
        val id = jsonObject.getString("id")
        val title = jsonObject.getString("title")
        val subtitle = if (jsonObject.has("subtitle") && !jsonObject.isNull("subtitle")) {
            jsonObject.getString("subtitle")
        } else null
        
        val iconUrl = if (jsonObject.has("iconUrl") && !jsonObject.isNull("iconUrl")) {
            jsonObject.getString("iconUrl")
        } else null
        
        val isPlayable = if (jsonObject.has("isPlayable")) {
            jsonObject.getBoolean("isPlayable")
        } else false
        
        val mediaUrl = if (jsonObject.has("mediaUrl") && !jsonObject.isNull("mediaUrl")) {
            jsonObject.getString("mediaUrl")
        } else null
        
        val mediaType = if (jsonObject.has("mediaType")) {
            val typeString = jsonObject.getString("mediaType").uppercase()
            when (typeString) {
                "VIDEO" -> MediaType.VIDEO
                "FOLDER" -> MediaType.FOLDER
                else -> MediaType.AUDIO
            }
        } else MediaType.AUDIO
        
        val durationMs = if (jsonObject.has("durationMs")) {
            jsonObject.getLong("durationMs")
        } else null
        
        // Parse children
        val children = if (jsonObject.has("children")) {
            val childrenArray = jsonObject.getJSONArray("children")
            val childrenList = mutableListOf<AndroidAutoMediaItem>()
            for (i in 0 until childrenArray.length()) {
                childrenList.add(parseMediaItem(childrenArray.getJSONObject(i)))
            }
            childrenList
        } else null
        
        // Parse metadata
        val metadata = if (jsonObject.has("metadata")) {
            val metadataJson = jsonObject.getJSONObject("metadata")
            parseMetadata(metadataJson)
        } else null
        
        return AndroidAutoMediaItem(
            id = id,
            title = title,
            subtitle = subtitle,
            iconUrl = iconUrl,
            isPlayable = isPlayable,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            durationMs = durationMs,
            children = children,
            metadata = metadata
        )
    }
    
    /**
     * Parse metadata JSONObject into Map
     */
    private fun parseMetadata(jsonObject: JSONObject): Map<String, Any> {
        val metadata = mutableMapOf<String, Any>()
        
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            
            when (value) {
                is String -> metadata[key] = value
                is Int -> metadata[key] = value
                is Long -> metadata[key] = value
                is Double -> metadata[key] = value
                is Boolean -> metadata[key] = value
                is JSONArray -> metadata[key] = parseJsonArray(value)
                is JSONObject -> metadata[key] = parseMetadata(value)
                else -> metadata[key] = value.toString()
            }
        }
        
        return metadata
    }
    
    /**
     * Parse JSONArray into List
     */
    private fun parseJsonArray(jsonArray: JSONArray): List<Any> {
        val list = mutableListOf<Any>()
        
        for (i in 0 until jsonArray.length()) {
            val value = jsonArray.get(i)
            
            when (value) {
                is String -> list.add(value)
                is Int -> list.add(value)
                is Long -> list.add(value)
                is Double -> list.add(value)
                is Boolean -> list.add(value)
                is JSONArray -> list.add(parseJsonArray(value))
                is JSONObject -> list.add(parseMetadata(value))
                else -> list.add(value.toString())
            }
        }
        
        return list
    }
    
    /**
     * Find MediaItem by ID recursively
     */
    fun findMediaItemById(id: String, items: List<AndroidAutoMediaItem>): AndroidAutoMediaItem? {
        items.forEach { item ->
            if (item.id == id) return item
            item.children?.let { children ->
                findMediaItemById(id, children)?.let { found -> return found }
            }
        }
        return null
    }
    
    /**
     * Convert MediaLibrary to JSON string
     */
    fun toJson(mediaLibrary: MediaLibrary): String {
        return try {
            val jsonObject = JSONObject().apply {
                put("layoutType", mediaLibrary.layoutType.name)
                put("rootItems", mediaItemsToJsonArray(mediaLibrary.rootItems))
                mediaLibrary.appName?.let { put("appName", it) }
                mediaLibrary.appIconUrl?.let { put("appIconUrl", it) }
            }
            jsonObject.toString()
        } catch (e: Exception) {
            println("❌ MediaLibraryParser: Failed to convert to JSON - ${e.message}")
            "{}"
        }
    }
    
    /**
     * Convert list of MediaItems to JSONArray
     */
    private fun mediaItemsToJsonArray(items: List<AndroidAutoMediaItem>): JSONArray {
        val jsonArray = JSONArray()
        items.forEach { item ->
            jsonArray.put(mediaItemToJsonObject(item))
        }
        return jsonArray
    }
    
    /**
     * Convert MediaItem to JSONObject
     */
    private fun mediaItemToJsonObject(item: AndroidAutoMediaItem): JSONObject {
        return JSONObject().apply {
            put("id", item.id)
            put("title", item.title)
            item.subtitle?.let { put("subtitle", it) }
            item.iconUrl?.let { put("iconUrl", it) }
            put("isPlayable", item.isPlayable)
            item.mediaUrl?.let { put("mediaUrl", it) }
            put("mediaType", item.mediaType.name)
            item.durationMs?.let { put("durationMs", it) }
            item.children?.let { put("children", mediaItemsToJsonArray(it)) }
            item.metadata?.let { put("metadata", metadataToJsonObject(it)) }
        }
    }
    
    /**
     * Convert metadata Map to JSONObject
     */
    private fun metadataToJsonObject(metadata: Map<String, Any>): JSONObject {
        val jsonObject = JSONObject()
        metadata.forEach { (key, value) ->
            jsonObject.put(key, value)
        }
        return jsonObject
    }
}
