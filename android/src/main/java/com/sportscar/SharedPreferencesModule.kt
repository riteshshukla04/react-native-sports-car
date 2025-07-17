package com.sportscar

import android.content.Context
import com.facebook.react.bridge.*
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.modules.core.DeviceEventManagerModule
import org.json.JSONObject
import org.json.JSONException

@ReactModule(name = SharedPreferencesModule.NAME)
class SharedPreferencesModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    companion object {
        const val NAME = "SharedPreferencesModule"
        private const val PREF_NAME = "MediaLibraryPrefs"
        private const val KEY_MEDIA_LIBRARY_DATA = "media_library_data"
        private const val KEY_LAYOUT_TYPE = "layout_type"
    }

    override fun getName(): String {
        return NAME
    }

    /**
     * Write JSON data to SharedPreferences
     * @param jsonString JSON string to store
     * @param promise Promise to resolve/reject
     */
    @ReactMethod
    fun writeMediaLibraryData(jsonString: String, promise: Promise) {
        try {
            val sharedPrefs = reactApplicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            
            // Store the JSON string
            editor.putString(KEY_MEDIA_LIBRARY_DATA, jsonString)
            
            // Also extract and store layout type separately for quick access
            try {
                val jsonObject = JSONObject(jsonString)
                val layoutType = if (jsonObject.has("layoutType")) {
                    jsonObject.getString("layoutType")
                } else {
                    "GRID" // Default layout type
                }
                editor.putString(KEY_LAYOUT_TYPE, layoutType)
            } catch (e: JSONException) {
                editor.putString(KEY_LAYOUT_TYPE, "GRID")
            }
            
            editor.apply()
            
            println("💾 SharedPreferences: Successfully wrote media library data")
            promise.resolve(true)
            
        } catch (e: Exception) {
            println("❌ SharedPreferences: Failed to write data - ${e.message}")
            promise.reject("WRITE_ERROR", "Failed to write to SharedPreferences", e)
        }
    }

    /**
     * Read JSON data from SharedPreferences
     * @param promise Promise to resolve with the JSON string or reject
     */
    @ReactMethod
    fun readMediaLibraryData(promise: Promise) {
        try {
            val sharedPrefs = reactApplicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonString = sharedPrefs.getString(KEY_MEDIA_LIBRARY_DATA, null)
            
            if (jsonString != null) {
                println("📖 SharedPreferences: Successfully read media library data")
                promise.resolve(jsonString)
            } else {
                println("ℹ️ SharedPreferences: No data found, returning null")
                promise.resolve(null)
            }
            
        } catch (e: Exception) {
            println("❌ SharedPreferences: Failed to read data - ${e.message}")
            promise.reject("READ_ERROR", "Failed to read from SharedPreferences", e)
        }
    }

    /**
     * Read only the layout type from SharedPreferences
     * @param promise Promise to resolve with the layout type string
     */
    @ReactMethod
    fun readLayoutType(promise: Promise) {
        try {
            val sharedPrefs = reactApplicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val layoutType = sharedPrefs.getString(KEY_LAYOUT_TYPE, "GRID")
            
            println("📖 SharedPreferences: Layout type is $layoutType")
            promise.resolve(layoutType)
            
        } catch (e: Exception) {
            println("❌ SharedPreferences: Failed to read layout type - ${e.message}")
            promise.reject("READ_ERROR", "Failed to read layout type", e)
        }
    }

    /**
     * Clear all stored data from SharedPreferences
     * @param promise Promise to resolve/reject
     */
    @ReactMethod
    fun clearMediaLibraryData(promise: Promise) {
        try {
            val sharedPrefs = reactApplicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val editor = sharedPrefs.edit()
            editor.clear()
            editor.apply()
            
            println("🗑️ SharedPreferences: Successfully cleared all data")
            promise.resolve(true)
            
        } catch (e: Exception) {
            println("❌ SharedPreferences: Failed to clear data - ${e.message}")
            promise.reject("CLEAR_ERROR", "Failed to clear SharedPreferences", e)
        }
    }

    /**
     * Check if media library data exists in SharedPreferences
     * @param promise Promise to resolve with boolean indicating if data exists
     */
    @ReactMethod
    fun hasMediaLibraryData(promise: Promise) {
        try {
            val sharedPrefs = reactApplicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val hasData = sharedPrefs.contains(KEY_MEDIA_LIBRARY_DATA)
            
            println("🔍 SharedPreferences: Has data = $hasData")
            promise.resolve(hasData)
            
        } catch (e: Exception) {
            println("❌ SharedPreferences: Failed to check data existence - ${e.message}")
            promise.reject("CHECK_ERROR", "Failed to check SharedPreferences", e)
        }
    }

    /**
     * Get the size of stored JSON data in bytes
     * @param promise Promise to resolve with the size in bytes
     */
    @ReactMethod
    fun getDataSize(promise: Promise) {
        try {
            val sharedPrefs = reactApplicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val jsonString = sharedPrefs.getString(KEY_MEDIA_LIBRARY_DATA, "")
            val size = jsonString?.length ?: 0
            
            println("📊 SharedPreferences: Data size = $size bytes")
            promise.resolve(size)
            
        } catch (e: Exception) {
            println("❌ SharedPreferences: Failed to get data size - ${e.message}")
            promise.reject("SIZE_ERROR", "Failed to get data size", e)
        }
    }
} 