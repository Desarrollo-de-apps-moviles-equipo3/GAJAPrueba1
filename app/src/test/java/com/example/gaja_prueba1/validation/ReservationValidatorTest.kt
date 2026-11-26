package com.example.gaja_prueba1.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ReservationValidatorTest {

    @Test
    fun fecha_pasada_se_rechaza_y_hoy_se_acepta() {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        val todayStart = ReservationValidator.startOfDay(today.timeInMillis).timeInMillis

        assertEquals(
            "La fecha no puede ser pasada",
            ReservationValidator.validateDate(yesterday.timeInMillis, today.timeInMillis)
        )
        assertNull(ReservationValidator.validateDate(todayStart, today.timeInMillis))
    }

    @Test
    fun fecha_mas_alla_del_limite_se_rechaza() {
        val now = Calendar.getInstance().timeInMillis
        val tooLate = (ReservationValidator.startOfDay(now)).apply {
            add(Calendar.DAY_OF_YEAR, ReservationValidator.MAX_DAYS_AHEAD + 1)
        }.timeInMillis

        assertEquals(
            "Solo puedes reservar hasta ${ReservationValidator.MAX_DAYS_AHEAD} días adelante",
            ReservationValidator.validateDate(tooLate, now)
        )
    }

    @Test
    fun motivo_debe_tener_el_minimo_de_caracteres() {
        assertEquals(
            "El motivo es obligatorio",
            ReservationValidator.validateReason("   ")
        )
        assertEquals(
            "Describe el motivo (mínimo ${ReservationValidator.MIN_REASON_LENGTH} caracteres)",
            ReservationValidator.validateReason("Corto")
        )
        assertNull(ReservationValidator.validateReason("Entrenamiento"))
    }

    @Test
    fun rango_horario_se_formatea_y_detecta_solapamientos() {
        assertEquals("08:00 - 11:00", ReservationValidator.formatHourSlot(8, 3))
        assertTrue(ReservationValidator.overlaps("08:00 - 11:00", "10:00 - 12:00"))
        assertFalse(ReservationValidator.overlaps("08:00 - 10:00", "10:00 - 12:00"))
    }
}
