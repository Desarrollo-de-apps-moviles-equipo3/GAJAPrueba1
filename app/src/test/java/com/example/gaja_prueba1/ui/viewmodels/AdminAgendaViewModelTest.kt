package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.model.Booking
import com.example.gaja_prueba1.validation.ReservationValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminAgendaViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val cancha = space("futbol-a", "Cancha Fútbol A")
    private val dayMillis = 24L * 60 * 60 * 1000
    private val tomorrow = ReservationValidator.toIsoDate(System.currentTimeMillis() + dayMillis)
    private val yesterday = ReservationValidator.toIsoDate(System.currentTimeMillis() - dayMillis)
    private val slot1 = "08:00 - 09:00"
    private val slot2 = "10:00 - 11:00"

    private fun vm(blocks: FakeBlocksRepository, bookings: List<Booking> = emptyList()) =
        AdminAgendaViewModel(FakeSpacesRepository(listOf(cancha)), blocks, FakeBookingsRepository(bookings))
            .also { it.refresh() }

    @Test
    fun noSePuedeEditarUnDiaPasado() {
        val vm = vm(FakeBlocksRepository())
        vm.onOpenEditor(yesterday)

        assertNull(vm.uiState.value.editor)
        assertEquals("No se puede editar un día pasado", vm.uiState.value.message)
    }

    @Test
    fun alAbrirElEditorSeMarcanLosHorariosYaBloqueados() {
        val vm = vm(FakeBlocksRepository(listOf(BloqueoDto(1, "futbol-a", tomorrow, slot1))))
        vm.onOpenEditor(tomorrow)

        assertEquals(setOf(slot1), vm.uiState.value.editor?.checked)
    }

    @Test
    fun marcarYDesmarcarUnHorarioActualizaElEditor() {
        val vm = vm(FakeBlocksRepository())
        vm.onOpenEditor(tomorrow)

        vm.onToggleSlot(slot2)
        assertTrue(slot2 in vm.uiState.value.editor!!.checked)
        vm.onToggleSlot(slot2)
        assertTrue(vm.uiState.value.editor!!.checked.isEmpty())
    }

    @Test
    fun elMotivoSeCortaEn80Caracteres() {
        val vm = vm(FakeBlocksRepository())
        vm.onOpenEditor(tomorrow)
        vm.onReasonChange("x".repeat(200))

        assertEquals(80, vm.uiState.value.editor!!.reason.length)
    }

    @Test
    fun guardarCreaElBloqueoEnElServicio() {
        val fake = FakeBlocksRepository()
        val vm = vm(fake)
        vm.onOpenEditor(tomorrow)
        vm.onToggleSlot(slot1)
        vm.onReasonChange("Mantencion")
        vm.onEditorSave()

        val state = vm.uiState.value
        assertNull(state.editor)
        assertEquals("Disponibilidad actualizada", state.message)
        assertEquals(1, fake.blocks.size)
        assertEquals("Mantencion", fake.blocks.first().reason)
        assertEquals(fake.blocks, state.blocks)
    }

    @Test
    fun desmarcarUnHorarioLoDesbloquea() {
        val fake = FakeBlocksRepository(listOf(BloqueoDto(1, "futbol-a", tomorrow, slot1)))
        val vm = vm(fake)
        vm.onOpenEditor(tomorrow)
        vm.onToggleSlot(slot1)
        vm.onEditorSave()

        assertTrue(fake.blocks.isEmpty())
        assertTrue(vm.uiState.value.blocks.isEmpty())
    }

    @Test
    fun guardarSinCambiosSoloCierraElEditor() {
        val fake = FakeBlocksRepository()
        val vm = vm(fake)
        vm.onOpenEditor(tomorrow)
        vm.onEditorSave()

        assertNull(vm.uiState.value.editor)
        assertNull(vm.uiState.value.message)
        assertTrue(fake.blocks.isEmpty())
    }

    @Test
    fun siGuardarFallaElEditorQuedaAbiertoConUnMensaje() {
        val fake = FakeBlocksRepository().apply { failOnCreate = true }
        val vm = vm(fake)
        vm.onOpenEditor(tomorrow)
        vm.onToggleSlot(slot1)
        vm.onEditorSave()

        val state = vm.uiState.value
        assertNotNull(state.editor)
        assertEquals(false, state.editor!!.isSaving)
        assertEquals("No pudimos guardar los cambios", state.message)
    }
}
