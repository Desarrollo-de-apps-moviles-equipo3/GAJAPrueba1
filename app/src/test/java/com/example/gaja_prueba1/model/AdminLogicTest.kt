package com.example.gaja_prueba1.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminHomeLogicTest {

    private fun reservation(
        id: Long, space: String = "Fútbol A", date: String = "2026-11-24",
        slot: String = "16:00 - 18:00", email: String = "a@gaja.cl"
    ) = AdminReservation(id, email, space, date, slot, "Entrenamiento", EstadoReserva.PENDIENTE)

    @Test
    fun `dos solicitudes para el mismo espacio fecha y horario se marcan en conflicto`() {
        val items = buildHomeItems(
            listOf(reservation(1, email = "a@gaja.cl"), reservation(2, email = "b@gaja.cl"), reservation(3, space = "Tenis C")),
            todayIso = "2026-11-24"
        )
        assertTrue(items.first { it.reservation.id == 1L }.hasConflict)
        assertTrue(items.first { it.reservation.id == 2L }.hasConflict)
        assertFalse(items.first { it.reservation.id == 3L }.hasConflict)
    }

    @Test
    fun `se ordenan por fecha y hora y se muestran solo las primeras 3`() {
        val items = buildHomeItems(
            listOf(
                reservation(1, date = "2026-11-30"),
                reservation(2, date = "2026-11-25"),
                reservation(3, date = "2026-11-24", slot = "18:00 - 20:00"),
                reservation(4, date = "2026-11-24", slot = "08:00 - 10:00")
            ),
            todayIso = "2026-11-24"
        )
        assertEquals(listOf(4L, 3L, 2L), items.map { it.reservation.id })
    }

    @Test
    fun `las solicitudes de dias pasados no aparecen`() {
        val items = buildHomeItems(listOf(reservation(1, date = "2026-11-20")), todayIso = "2026-11-24")
        assertTrue(items.isEmpty())
    }

    @Test
    fun `la etiqueta dice hoy o la fecha`() {
        assertEquals("hoy 16:00", whenLabel(reservation(1), "2026-11-24"))
        assertEquals("30/11 16:00", whenLabel(reservation(1, date = "2026-11-30"), "2026-11-24"))
    }

    @Test
    fun `las iniciales salen del nombre`() {
        assertEquals("LM", initialsOf("Lucas Morales"))
        assertEquals("A", initialsOf("admin"))
        assertEquals("A", initialsOf("   "))
    }
}