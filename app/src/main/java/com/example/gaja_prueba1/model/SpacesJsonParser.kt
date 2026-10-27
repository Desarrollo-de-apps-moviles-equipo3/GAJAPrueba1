package com.example.gaja_prueba1.model

import org.json.JSONArray

object SpacesJsonParser {
    fun parse(json: String): List<Space> {
        val array = JSONArray(json)
        return List(array.length()) { index ->
            val item = array.getJSONObject(index)
            val tagsArray = item.optJSONArray("tags")
            Space(
                id = item.getString("id"),
                name = item.getString("name"),
                category = item.getString("category"),
                description = item.optString("description", ""),
                capacity = item.optInt("capacity", 0),
                available = item.optBoolean("available", true),
                colorHex = item.optString("colorHex", "#3B6989"),
                tags = if (tagsArray == null) emptyList()
                else List(tagsArray.length()) { i -> tagsArray.getString(i) }
            )
        }
    }
}