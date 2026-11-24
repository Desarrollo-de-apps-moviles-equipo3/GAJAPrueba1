package com.example.gaja_prueba1.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/** Reserva tal como la devuelve el microservicio de reservas (reservas-service). */
data class ReservaDto(
    val id: Long,
    val usuarioEmail: String,
    val espacio: String,
    val fecha: String,          // "AAAA-MM-DD"
    val horario: String,
    val motivo: String,
    val estado: String,         // PENDIENTE, APROBADA, RECHAZADA o CANCELADA
    val creadaEn: String? = null,
    val motivoRechazo: String? = null
)

/** Cuerpo para aprobar o rechazar. [motivoRechazo] solo se envía al rechazar. */
data class ActualizarEstadoDto(
    val estado: String,
    val motivoRechazo: String? = null
)

/**
 * Rutas del administrador en reservas-service.
 * - listar(estado): requiere GET /api/reservas?estado=... en reservas-service.
 * - actualizarEstado: requiere el campo motivoRechazo en ActualizarEstadoRequest.
 */
interface AdminReservasApi {
    @GET("api/reservas")
    suspend fun listar(@Query("estado") estado: String? = null): List<ReservaDto>

    @PATCH("api/reservas/{id}/estado")
    suspend fun actualizarEstado(
        @Path("id") id: Long,
        @Body body: ActualizarEstadoDto
    ): ReservaDto
}