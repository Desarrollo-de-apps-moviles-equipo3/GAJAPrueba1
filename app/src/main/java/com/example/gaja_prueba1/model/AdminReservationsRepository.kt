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
    val rejectionReason: String? = null,
    val createdAt: String? = null,
    val hiddenForUser: Boolean = false
)

data class ReservationRules(
    val maxDaysAhead: Int,
    val cancellationLimitHours: Int,
    val openingHour: Int = 8,
    val closingHour: Int = 23
)

interface AdminReservationsRepository {
    suspend fun getByStatus(status: EstadoReserva): List<AdminReservation>
    suspend fun getRules(): ReservationRules = ReservationRules(7, 2)
    suspend fun updateRules(rules: ReservationRules): ReservationRules = rules
    suspend fun getAll(): List<AdminReservation> =
        getByStatus(EstadoReserva.PENDIENTE) +
                getByStatus(EstadoReserva.APROBADA) +
                getByStatus(EstadoReserva.RECHAZADA) +
                getByStatus(EstadoReserva.CANCELADA)
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

    override suspend fun getAll(): List<AdminReservation> = withContext(Dispatchers.IO) {
        api.listar().map { it.toModel() }
    }

    override suspend fun getRules(): ReservationRules = withContext(Dispatchers.IO) {
        api.obtenerReglas().let {
            ReservationRules(
                maxDaysAhead = it.maxDiasAdelante,
                cancellationLimitHours = it.horasLimiteCancelacion,
                openingHour = it.horaApertura,
                closingHour = it.horaCierre
            )
        }
    }

    override suspend fun updateRules(rules: ReservationRules): ReservationRules = withContext(Dispatchers.IO) {
        api.actualizarReglas(
            com.example.gaja_prueba1.data.remote.ActualizarReglasReservaDto(
                rules.maxDaysAhead, rules.cancellationLimitHours
            )
        ).let {
            ReservationRules(it.maxDiasAdelante, it.horasLimiteCancelacion,
                it.horaApertura, it.horaCierre)
        }
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
    rejectionReason = motivoRechazo,
    createdAt = creadaEn,
    hiddenForUser = ocultaParaUsuario
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

/** Otra solicitud pendiente que pide la misma cancha, fecha y horario. */
fun AdminReservation.conflictWith(others: List<AdminReservation>): AdminReservation? =
    others.firstOrNull {
        it.id != id && it.status == EstadoReserva.PENDIENTE &&
                it.space == space && it.date == date && it.slot == slot
    }
