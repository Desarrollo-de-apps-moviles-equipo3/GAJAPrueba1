package com.example.gaja_prueba1.model

data class Space(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val capacity: Int,
    val available: Boolean,
    val colorHex: String,
    val tags: List<String> = emptyList()
)