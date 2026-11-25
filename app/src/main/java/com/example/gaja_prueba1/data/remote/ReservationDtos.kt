package com.example.gaja_prueba1.data.remote

data class CreateReservationRequest(
    val usuarioEmail: String,
    val espacio: String,
    val fecha: String,
    val horario: String,
    val motivo: String
)

data class ReservationResponse(
    val id: Long,
    val usuarioEmail: String,
    val espacio: String,
    val fecha: String,
    val horario: String,
    val motivo: String,
    val estado: String,
    val creadaEn: String,
    val motivoRechazo: String? = null
)