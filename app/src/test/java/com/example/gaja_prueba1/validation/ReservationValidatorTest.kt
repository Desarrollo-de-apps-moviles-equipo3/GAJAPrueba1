package com.example.gaja_prueba1.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class ReservationValidatorTest {

    // "Hoy" fijo para que los tests no dependan del reloj real: 10 de noviembre de 2026, 15:00
    private val hoy: Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 10, 15, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    // Devuelve la fecha de hoy movida N días (positivo = futuro, negativo = pasado)
    private fun diasDesdeHoy(dias: Int): Long =
        Calendar.getInstance().apply {
            timeInMillis = hoy
            add(Calendar.DAY_OF_YEAR, dias)
        }.timeInMillis

    // ---------- Fecha ----------

    @Test
    fun fecha_nula_pide_seleccionar_una_fecha() {
        assertEquals("Selecciona una fecha", ReservationValidator.validateDate(null, hoy))
    }

    @Test
    fun fecha_pasada_da_error() {
        assertEquals(
            "La fecha no puede ser pasada",
            ReservationValidator.validateDate(diasDesdeHoy(-1), hoy)
        )
    }

    @Test
    fun hoy_es_valido() {
        assertNull(ReservationValidator.validateDate(hoy, hoy))
    }

    @Test
    fun dia_7_es_valido() {
        assertNull(ReservationValidator.validateDate(diasDesdeHoy(7), hoy))
    }

    @Test
    fun dia_8_da_error_de_anticipacion() {
        assertEquals(
            "Solo puedes reservar hasta 7 días adelante",
            ReservationValidator.validateDate(diasDesdeHoy(8), hoy)
        )
    }

    // ---------- Motivo ----------

    @Test
    fun motivo_vacio_es_obligatorio() {
        assertEquals("El motivo es obligatorio", ReservationValidator.validateReason(""))
    }

    @Test
    fun motivo_solo_con_espacios_es_obligatorio() {
        assertEquals("El motivo es obligatorio", ReservationValidator.validateReason("     "))
    }

    @Test
    fun motivo_de_9_caracteres_es_muy_corto() {
        assertEquals(
            "Describe el motivo (mínimo 10 caracteres)",
            ReservationValidator.validateReason("123456789")
        )
    }

    @Test
    fun motivo_de_10_caracteres_es_valido() {
        assertNull(ReservationValidator.validateReason("1234567890"))
    }
}