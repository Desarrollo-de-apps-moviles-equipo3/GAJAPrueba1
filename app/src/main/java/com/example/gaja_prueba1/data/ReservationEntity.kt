package com.example.gaja_prueba1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Reserva guardada. [date] va como "yyyy-MM-dd" para comparar y contar por día en SQL. */
@Entity(tableName = "reservation")
data class ReservationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val space: String,
    val date: String,
    val timeSlot: String,
    val reason: String,
    val photoUri: String? = null
)