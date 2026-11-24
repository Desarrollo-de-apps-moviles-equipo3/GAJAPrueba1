package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ActualizarEstadoDto
import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.ReservaDto
import com.example.gaja_prueba1.data.remote.AdminReservasApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Una solicitud de reserva tal como la ve el administrador. */
data class AdminReservation(
    val id: Long,
    val userEmail: String,
    val space: String,
    val date: String,
    val slot: String,
    val reason: String,
    val status: EstadoReserva,
    val rejectionReason: String? = null
)

interface AdminReservationsRepository {
    suspend fun getByStatus(status: EstadoReserva): List<AdminReservation>
    suspend fun approve(id: Long): AdminReservation
    suspend fun reject(id: Long, reason: String): AdminReservation
}

class RemoteAdminReservationsRepository(
    private val api: AdminReservasApi = ApiClient.create(ApiConfig.RESERVAS_URL, AdminReservasApi::class.java)
) : AdminReservationsRepository {

    override suspend fun getByStatus(status: EstadoReserva): List<AdminReservation> =
        withContext(Dispatchers.IO) {
            api.listar(status.name).map { it.toModel() }
        }

    override suspend fun approve(id: Long): AdminReservation = withContext(Dispatchers.IO) {
        api.actualizarEstado(id, ActualizarEstadoDto(EstadoReserva.APROBADA.name)).toModel()
    }

    override suspend fun reject(id: Long, reason: String): AdminReservation =
        withContext(Dispatchers.IO) {
            api.actualizarEstado(
                id,
                ActualizarEstadoDto(EstadoReserva.RECHAZADA.name, reason.trim())
            ).toModel()
        }
}

internal fun ReservaDto.toModel() = AdminReservation(
    id = id,
    userEmail = usuarioEmail,
    space = espacio,
    date = fecha,
    slot = horario,
    reason = motivo,
    status = EstadoReserva.entries.firstOrNull { it.name == estado } ?: EstadoReserva.PENDIENTE,
    rejectionReason = motivoRechazo
)

/** Reglas del motivo de rechazo (el servicio lo guarda y el usuario lo verá). */
object RejectionValidator {
    const val MIN = 5
    const val MAX = 200

    fun validate(reason: String): String? {
        val text = reason.trim()
        return when {
            text.isEmpty() -> "Escribe el motivo del rechazo"
            text.length < MIN -> "El motivo debe tener al menos $MIN caracteres"
            text.length > MAX -> "El motivo no puede superar los $MAX caracteres"
            else -> null
        }
    }
}