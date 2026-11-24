package com.example.gaja_prueba1.data.remote

import retrofit2.http.GET

/** Un horario bloqueado por el administrador. [date] viene como "yyyy-MM-dd" y [timeSlot] como "08:00 - 10:00". */
data class BloqueoDto(
    val id: Long,
    val spaceId: String,
    val date: String,
    val timeSlot: String,
    val reason: String? = null
)

/** Bloqueos del microservicio de espacios. Sin parámetro `fecha` devuelve todos. */
interface BloqueosApi {
    @GET("api/bloqueos")
    suspend fun getBloqueos(): List<BloqueoDto>
}
