package com.example.gaja_prueba1.validation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReservationValidator {
    const val MAX_DAYS_AHEAD = 7
    const val MAX_PER_DAY = 3
    const val MIN_REASON_LENGTH = 10

    val SLOTS = listOf(
        "08:00 - 10:00", "10:00 - 12:00", "12:00 - 14:00",
        "14:00 - 16:00", "16:00 - 18:00", "18:00 - 20:00"
    )

    fun validateDate(
        selectedMillis: Long?,
        nowMillis: Long = System.currentTimeMillis()
    ): String? {
        if (selectedMillis == null) return "Selecciona una fecha"

        val today = startOfDay(nowMillis)
        val selected = startOfDay(selectedMillis)

        val lastAllowed = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, MAX_DAYS_AHEAD)
        }

        return when {
            selected.before(today) -> "La fecha no puede ser pasada"
            selected.after(lastAllowed) ->
                "Solo puedes reservar hasta $MAX_DAYS_AHEAD días adelante"
            else -> null
        }
    }

    fun validateReason(text: String): String? = when {
        text.isBlank() -> "El motivo es obligatorio"
        text.trim().length < MIN_REASON_LENGTH ->
            "Describe el motivo (mínimo $MIN_REASON_LENGTH caracteres)"
        else -> null
    }

    // Regla del límite diario. Recibe cuántas reservas ya existen ese día.
    fun validateDailyLimit(reservationsThatDay: Int): String? =
        if (reservationsThatDay >= MAX_PER_DAY) "Ese día ya alcanzó el máximo de reservas" else null

    // Formato para guardar y ordenar la fecha en Room.
    fun toIsoDate(millis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(millis)

    // Formato para mostrar la fecha en español en la pantalla.
    fun formatDate(
        millis: Long,
        pattern: String = "EEE, d 'de' MMMM"
    ): String =
        SimpleDateFormat(pattern, Locale("es", "CL")).format(millis)

        fun startOfDay(millis: Long): Calendar =
        Calendar.getInstance().apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
}