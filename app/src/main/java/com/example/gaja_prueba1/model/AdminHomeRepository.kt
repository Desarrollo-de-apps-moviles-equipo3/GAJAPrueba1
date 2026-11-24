package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.IncidenciasApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Una fila de "Pendientes": la solicitud, si choca con otra y cuándo es. */
data class AdminHomeItem(
    val reservation: AdminReservation,
    val hasConflict: Boolean,
    val whenLabel: String
)

/** Lo que muestra el Inicio del administrador. Un contador en null significa "sin dato todavía". */
data class AdminHomeSummary(
    val pendingReservations: Int,
    val openIncidents: Int?,
    val items: List<AdminHomeItem>
)

interface AdminHomeRepository {
    suspend fun load(todayIso: String): AdminHomeSummary
}

class RemoteAdminHomeRepository(
    private val reservations: AdminReservationsRepository = RemoteAdminReservationsRepository(),
    private val incidents: IncidenciasApi =
        ApiClient.create(ApiConfig.RESERVAS_URL, IncidenciasApi::class.java)
) : AdminHomeRepository {

    override suspend fun load(todayIso: String): AdminHomeSummary {
        val pending = reservations.getByStatus(EstadoReserva.PENDIENTE)
        // Si el contador de incidencias falla, el Inicio igual se muestra (con "—").
        val openIncidents = withContext(Dispatchers.IO) {
            runCatching { incidents.contarPendientes().cantidad.toInt() }.getOrNull()
        }
        return AdminHomeSummary(
            pendingReservations = pending.size,
            openIncidents = openIncidents,
            items = buildHomeItems(pending, todayIso)
        )
    }
}

/**
 * Ordena las solicitudes pendientes por fecha y hora, marca las que chocan con otra
 * (mismo espacio, fecha y horario) y deja las [limit] primeras. Las que ya pasaron no se muestran.
 */
fun buildHomeItems(
    pending: List<AdminReservation>,
    todayIso: String,
    limit: Int = 3
): List<AdminHomeItem> {
    val conflicts = pending
        .groupBy { Triple(it.space, it.date, it.slot) }
        .filterValues { it.size > 1 }
        .keys

    return pending
        .filter { it.date >= todayIso }
        .sortedWith(compareBy({ it.date }, { it.slot }))
        .take(limit)
        .map { reservation ->
            AdminHomeItem(
                reservation = reservation,
                hasConflict = Triple(reservation.space, reservation.date, reservation.slot) in conflicts,
                whenLabel = whenLabel(reservation, todayIso)
            )
        }
}

/** "hoy 16:00" si es hoy; si no, "30/11 16:00". */
internal fun whenLabel(reservation: AdminReservation, todayIso: String): String {
    val start = reservation.slot.take(5)
    if (reservation.date == todayIso) return "hoy $start"
    val parts = reservation.date.split("-")
    val day = if (parts.size == 3) "${parts[2]}/${parts[1]}" else reservation.date
    return "$day $start"
}

/** Iniciales del avatar: "Lucas Morales" -> "LM", "admin" -> "A". */
fun initialsOf(name: String): String =
    name.trim().split(" ").filter { it.isNotBlank() }.take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "A" }