package com.example.gaja_prueba1.validation

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object ReservationValidator {
    const val MAX_DAYS_AHEAD = 7
    const val MIN_REASON_LENGTH = 10
    const val OPENING_HOUR = 8
    const val CLOSING_HOUR = 23

    /** Bloques horarios de una hora usados por el administrador para bloquear la agenda. */
    val SLOTS: List<String> = (OPENING_HOUR until CLOSING_HOUR).map { formatHourSlot(it) }

    fun startHours(openingHour: Int = OPENING_HOUR, closingHour: Int = CLOSING_HOUR): List<Int> =
        (openingHour until closingHour).toList()

    fun formatHourSlot(startHour: Int, durationHours: Int = 1): String =
        "%02d:00 - %02d:00".format(Locale.US, startHour, startHour + durationHours)

    fun durationHours(slot: String): Int? = runCatching {
        val parts = slot.split("-")
        if (parts.size != 2) return null
        val start = parts[0].trim().substringBefore(":").toInt()
        val end = parts[1].trim().substringBefore(":").toInt()
        (end - start).takeIf { it > 0 }
    }.getOrNull()

    fun overlaps(first: String, second: String): Boolean = runCatching {
        val a = first.split("-").map { it.trim().substringBefore(":").toInt() }
        val b = second.split("-").map { it.trim().substringBefore(":").toInt() }
        a.size == 2 && b.size == 2 && a[0] < b[1] && b[0] < a[1]
    }.getOrDefault(false)

    fun validateDate(
        selectedMillis: Long?,
        nowMillis: Long = System.currentTimeMillis(),
        maxDaysAhead: Int = MAX_DAYS_AHEAD
    ): String? {
        if (selectedMillis == null) return "Selecciona una fecha"

        val today = startOfDay(nowMillis)
        val selected = startOfDay(selectedMillis)

        val lastAllowed = (today.clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, maxDaysAhead)
        }

        return when {
            selected.before(today) -> "La fecha no puede ser pasada"
            selected.after(lastAllowed) ->
                "Solo puedes reservar hasta $maxDaysAhead días adelante"
            else -> null
        }
    }

    fun validateReason(text: String): String? = when {
        text.isBlank() -> "El motivo es obligatorio"
        text.trim().length < MIN_REASON_LENGTH ->
            "Describe el motivo (mínimo $MIN_REASON_LENGTH caracteres)"
        else -> null
    }

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