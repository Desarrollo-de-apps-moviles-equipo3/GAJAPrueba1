package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.AdminReservasApi
import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Un horario que ya está tomado por una reserva activa. Es lo único que necesita el calendario. */
data class Booking(
    val space: String,
    val date: String,
    val timeSlot: String,
    val reason: String
)

interface BookingsRepository {
    suspend fun getActive(): List<Booking>
}

/**
 * Reservas que ocupan un horario, leídas del microservicio de reservas (puerto 8083).
 * Solo cuentan las PENDIENTE y APROBADA: una reserva rechazada o cancelada libera el horario,
 * igual que hace el servicio al calcular los horarios ocupados.
 */
class RemoteBookingsRepository(
    private val api: AdminReservasApi = ApiClient.create(ApiConfig.RESERVAS_URL, AdminReservasApi::class.java)
) : BookingsRepository {

    override suspend fun getActive(): List<Booking> = withContext(Dispatchers.IO) {
        api.listar(null)
            .filter { it.estado.uppercase() in ACTIVE_STATES }
            .map { Booking(space = it.espacio, date = it.fecha, timeSlot = it.horario.trim(), reason = it.motivo) }
    }

    private companion object {
        val ACTIVE_STATES = setOf("PENDIENTE", "APROBADA")
    }
}
