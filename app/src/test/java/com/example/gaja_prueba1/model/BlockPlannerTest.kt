package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.data.remote.BloqueoRequestDto
import com.example.gaja_prueba1.data.remote.BloqueosApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockPlannerTest {

    private val cancha = Space(
        id = "cancha-1", name = "Cancha 1", category = "Deportes", description = "", capacity = 10,
        available = true, colorHex = "#2F6B4F"
    )
    private val day = "2026-11-26"
    private val s1 = "08:00 - 10:00"
    private val s2 = "10:00 - 12:00"
    private val s3 = "12:00 - 14:00"

    private fun block(id: Long, spaceId: String, date: String, slot: String) =
        BloqueoDto(id, spaceId, date, slot, null)

    private fun reservation(space: String, date: String, slot: String) =
        Booking(space = space, date = date, timeSlot = slot, reason = "Entrenamiento")

    @Test
    fun el_editor_parte_marcado_con_los_horarios_ya_bloqueados() {
        val blocks = listOf(block(1, "cancha-1", day, s2), block(2, "cancha-1", "2026-11-27", s1), block(3, "cancha-2", day, s1))
        assertEquals(setOf(s2), BlockPlanner.initialChecked(cancha, day, blocks))
    }

    @Test
    fun marcar_un_horario_libre_crea_un_bloqueo() {
        val plan = BlockPlanner.plan(cancha, day, setOf(s1), emptyList(), emptyList())
        assertEquals(listOf(s1), plan.toCreate)
        assertTrue(plan.toDelete.isEmpty())
        assertTrue(plan.hasChanges)
    }

    @Test
    fun desmarcar_un_horario_bloqueado_borra_su_bloqueo() {
        val blocks = listOf(block(7, "cancha-1", day, s1))
        val plan = BlockPlanner.plan(cancha, day, emptySet(), blocks, emptyList())
        assertTrue(plan.toCreate.isEmpty())
        assertEquals(listOf(7L), plan.toDelete.map { it.id })
    }

    @Test
    fun sin_cambios_no_hay_nada_que_guardar() {
        val blocks = listOf(block(7, "cancha-1", day, s1))
        val plan = BlockPlanner.plan(cancha, day, setOf(s1), blocks, emptyList())
        assertFalse(plan.hasChanges)
    }

    @Test
    fun no_toca_los_bloqueos_de_otros_dias_ni_de_otros_espacios() {
        val blocks = listOf(
            block(1, "cancha-1", "2026-11-27", s1),
            block(2, "cancha-2", day, s1)
        )
        val plan = BlockPlanner.plan(cancha, day, emptySet(), blocks, emptyList())
        assertFalse(plan.hasChanges)
    }

    @Test
    fun bloquear_un_horario_con_reserva_la_marca_como_afectada() {
        val reservations = listOf(
            reservation("Cancha 1", day, s1),
            reservation("Cancha 1", day, s2),
            reservation("Cancha 2", day, s1),
            reservation("Cancha 1", "2026-11-27", s1)
        )
        val plan = BlockPlanner.plan(cancha, day, setOf(s1, s3), emptyList(), reservations)
        assertEquals(listOf(s1, s3), plan.toCreate)
        assertEquals(1, plan.affected.size)
        assertEquals(s1, plan.affected.first().timeSlot)
    }

    @Test
    fun cambia_varios_horarios_a_la_vez() {
        val blocks = listOf(block(5, "cancha-1", day, s1))
        val plan = BlockPlanner.plan(cancha, day, setOf(s2, s3), blocks, emptyList())
        assertEquals(listOf(s2, s3), plan.toCreate)
        assertEquals(listOf(5L), plan.toDelete.map { it.id })
    }

    @Test
    fun el_repositorio_manda_el_bloqueo_al_servicio() = runBlocking {
        var sentSpace: String? = null
        var sentBody: BloqueoRequestDto? = null
        var deleted: Long? = null
        val api = object : BloqueosApi {
            override suspend fun getBloqueos() = emptyList<BloqueoDto>()
            override suspend fun crear(spaceId: String, body: BloqueoRequestDto): BloqueoDto {
                sentSpace = spaceId
                sentBody = body
                return BloqueoDto(1, spaceId, body.date, body.timeSlot, body.reason)
            }
            override suspend fun eliminar(id: Long) { deleted = id }
        }
        val repo = RemoteBlocksRepository(api)

        val created = repo.create("cancha-1", day, s1, "Mantención")
        repo.delete(9)

        assertEquals("cancha-1", sentSpace)
        assertEquals(BloqueoRequestDto(day, s1, "Mantención"), sentBody)
        assertEquals(s1, created.timeSlot)
        assertEquals(9L, deleted)
    }
}
