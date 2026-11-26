package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.data.remote.BloqueoRequestDto
import com.example.gaja_prueba1.data.remote.BloqueosApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Calendar

class SpaceAvailabilityTest {

    private val cancha = Space(
        id = "cancha-1", name = "Cancha 1", category = "Deportes", description = "", capacity = 10,
        available = true, colorHex = "#2F6B4F"
    )
    private val otra = cancha.copy(id = "cancha-2", name = "Cancha 2")

    private val today = "2026-11-25"
    private val slot = "08:00 - 10:00"

    private fun block(spaceId: String, date: String, slot: String, reason: String? = null) =
        BloqueoDto(1, spaceId, date, slot, reason)

    private fun reservation(space: String, date: String, slot: String) =
        Booking(space = space, date = date, timeSlot = slot, reason = "Entrenamiento")

    private fun millis(year: Int, month0: Int, day: Int, hour: Int = 12): Long =
        Calendar.getInstance().apply { clear(); set(year, month0, day, hour, 0) }.timeInMillis

    @Test
    fun sin_bloqueos_ni_reservas_el_horario_esta_libre() {
        assertEquals(SlotStatus.Libre, SpaceAvailability.slotStatus(cancha, "2026-11-26", slot, today, emptyList(), emptyList()))
    }

    @Test
    fun un_bloqueo_del_admin_deja_el_horario_bloqueado() {
        val blocks = listOf(block("cancha-1", "2026-11-26", slot))
        assertEquals(SlotStatus.Bloqueado, SpaceAvailability.slotStatus(cancha, "2026-11-26", slot, today, blocks, emptyList()))
    }

    @Test
    fun el_bloqueo_de_un_espacio_no_afecta_a_otro() {
        val blocks = listOf(block("cancha-1", "2026-11-26", slot))
        assertEquals(SlotStatus.Libre, SpaceAvailability.slotStatus(otra, "2026-11-26", slot, today, blocks, emptyList()))
    }

    @Test
    fun el_bloqueo_de_otro_horario_u_otro_dia_no_afecta() {
        val blocks = listOf(block("cancha-1", "2026-11-26", "10:00 - 12:00"))
        assertEquals(SlotStatus.Libre, SpaceAvailability.slotStatus(cancha, "2026-11-26", slot, today, blocks, emptyList()))
        assertEquals(SlotStatus.Libre, SpaceAvailability.slotStatus(cancha, "2026-11-27", "10:00 - 12:00", today, blocks, emptyList()))
    }

    @Test
    fun una_reserva_deja_el_horario_reservado() {
        val reservations = listOf(reservation("Cancha 1", "2026-11-26", slot))
        assertEquals(SlotStatus.Reservado, SpaceAvailability.slotStatus(cancha, "2026-11-26", slot, today, emptyList(), reservations))
        assertEquals(SlotStatus.Libre, SpaceAvailability.slotStatus(otra, "2026-11-26", slot, today, emptyList(), reservations))
    }

    @Test
    fun si_hay_bloqueo_y_reserva_gana_el_bloqueo() {
        val blocks = listOf(block("cancha-1", "2026-11-26", slot))
        val reservations = listOf(reservation("Cancha 1", "2026-11-26", slot))
        assertEquals(SlotStatus.Bloqueado, SpaceAvailability.slotStatus(cancha, "2026-11-26", slot, today, blocks, reservations))
    }

    @Test
    fun los_dias_anteriores_a_hoy_quedan_como_pasado() {
        val blocks = listOf(block("cancha-1", "2026-11-24", slot))
        assertEquals(SlotStatus.Pasado, SpaceAvailability.slotStatus(cancha, "2026-11-24", slot, today, blocks, emptyList()))
        assertEquals(
            SlotStatus.Libre,
            SpaceAvailability.slotStatus(
                cancha, today, slot, today, emptyList(), emptyList(),
                nowMillis = millis(2026, Calendar.NOVEMBER, 25, 7)
            )
        )
    }

    @Test
    fun freeCount_descuenta_bloqueos_y_reservas() {
        val blocks = listOf(block("cancha-1", "2026-11-26", "08:00 - 10:00"))
        val reservations = listOf(reservation("Cancha 1", "2026-11-26", "10:00 - 12:00"))
        assertEquals(11, SpaceAvailability.freeCount(cancha, "2026-11-26", today, blocks, reservations))
    }

    @Test
    fun blockFor_entrega_el_motivo() {
        val blocks = listOf(block("cancha-1", "2026-11-26", slot, "Mantención"))
        assertEquals("Mantención", SpaceAvailability.blockFor(cancha, "2026-11-26", slot, blocks)?.reason)
        assertNull(SpaceAvailability.blockFor(otra, "2026-11-26", slot, blocks))
    }

    @Test
    fun la_semana_va_de_lunes_a_domingo() {
        // Miércoles 25 de noviembre de 2026
        val days = SpaceAvailability.weekDays(millis(2026, Calendar.NOVEMBER, 25), 0)
        assertEquals(
            listOf("2026-11-23", "2026-11-24", "2026-11-25", "2026-11-26", "2026-11-27", "2026-11-28", "2026-11-29"),
            days
        )
    }

    @Test
    fun el_domingo_pertenece_a_la_semana_que_termina_ese_dia() {
        val days = SpaceAvailability.weekDays(millis(2026, Calendar.NOVEMBER, 29), 0)
        assertEquals("2026-11-23", days.first())
        assertEquals("2026-11-29", days.last())
    }

    @Test
    fun la_semana_se_puede_correr_y_cruza_de_mes() {
        val now = millis(2026, Calendar.NOVEMBER, 25)
        assertEquals("2026-11-30", SpaceAvailability.weekDays(now, 1).first())
        assertEquals("2026-12-06", SpaceAvailability.weekDays(now, 1).last())
        assertEquals("2026-11-16", SpaceAvailability.weekDays(now, -1).first())
    }

    @Test
    fun el_mes_empieza_en_el_dia_de_la_semana_correcto() {
        // 1 de noviembre de 2026 es domingo: 6 huecos antes
        val grid = SpaceAvailability.monthGrid(millis(2026, Calendar.NOVEMBER, 25), 0)
        assertEquals(2026, grid.year)
        assertEquals(Calendar.NOVEMBER, grid.month)
        assertEquals(List(6) { null }, grid.cells.take(6))
        assertEquals("2026-11-01", grid.cells[6])
        assertEquals("2026-11-30", grid.cells.filterNotNull().last())
        assertEquals(30, grid.cells.filterNotNull().size)
        assertEquals(0, grid.cells.size % 7)
    }

    @Test
    fun el_mes_siguiente_cruza_de_anio() {
        val grid = SpaceAvailability.monthGrid(millis(2026, Calendar.NOVEMBER, 25), 2)
        assertEquals(2027, grid.year)
        assertEquals(Calendar.JANUARY, grid.month)
        assertEquals("2027-01-01", grid.cells.filterNotNull().first())
        assertEquals(31, grid.cells.filterNotNull().size)
    }

    @Test
    fun el_nombre_del_mes_va_en_espanol() {
        val grid = SpaceAvailability.monthGrid(millis(2026, Calendar.NOVEMBER, 25), 0)
        assertEquals("Noviembre 2026", SpaceAvailability.monthLabel(grid))
    }

    @Test
    fun los_bloqueos_llegan_desde_el_servicio() = runBlocking {
        val api = object : BloqueosApi {
            override suspend fun getBloqueos() = listOf(block("cancha-1", "2026-11-26", slot, "Mantención"))
            override suspend fun crear(spaceId: String, body: BloqueoRequestDto) = block(spaceId, body.date, body.timeSlot, body.reason)
            override suspend fun eliminar(id: Long) {}
        }
        val result = RemoteBlocksRepository(api).getAll()
        assertEquals(1, result.size)
        assertEquals("cancha-1", result.first().spaceId)
    }
}
