package com.example.gaja_prueba1.model

/** Cantidad de decisiones registradas para una persona o un espacio. */
data class UsageReportRow(
    val label: String,
    val approvedReservations: Int,
    val rejectedReservations: Int
)

/** Agrupa los registros persistidos del microservicio para los informes del panel. */
class AdminReportsRepository(
    private val reservations: AdminReservationsRepository = RemoteAdminReservationsRepository()
) {
    suspend fun reservations(): List<AdminReservation> = reservations.getAll()

    suspend fun rules(): ReservationRules = reservations.getRules()

    suspend fun updateRules(rules: ReservationRules): ReservationRules =
        reservations.updateRules(rules)
}
