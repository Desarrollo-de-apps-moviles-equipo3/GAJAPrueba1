package com.example.gaja_prueba1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Sesión activa. Id fijo = 1: solo existe una sesión a la vez. */
@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val email: String,
    val role: String
)