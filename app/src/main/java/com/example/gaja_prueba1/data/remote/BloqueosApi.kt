package com.example.gaja_prueba1.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Un horario bloqueado por el administrador. [date] viene como "yyyy-MM-dd" y [timeSlot] como "08:00 - 10:00". */
data class BloqueoDto(
    val id: Long,
    val spaceId: String,
    val date: String,
    val timeSlot: String,
    val reason: String? = null
)

/** Cuerpo para bloquear un horario. Si [reason] va null, no se envía. */
data class BloqueoRequestDto(val date: String, val timeSlot: String, val reason: String? = null)

/** Bloqueos del microservicio de espacios. Sin parámetro `fecha` devuelve todos. */
interface BloqueosApi {
    @GET("api/bloqueos")
    suspend fun getBloqueos(): List<BloqueoDto>

    @POST("api/espacios/{id}/bloqueos")
    suspend fun crear(@Path("id") spaceId: String, @Body body: BloqueoRequestDto): BloqueoDto

    @DELETE("api/bloqueos/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}
