package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.model.Booking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CalendarViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val spaces = listOf(
        space("tenis-1", "Tenis 1", "Tenis", available = false),
        space("futbol-a", "Cancha Fútbol A"),
        space("futbol-b", "Cancha Fútbol B")
    )

    private fun vm(
        spacesRepo: FakeSpacesRepository = FakeSpacesRepository(spaces),
        blocks: List<BloqueoDto> = emptyList(),
        bookings: List<Booking> = emptyList()
    ) = CalendarViewModel(spacesRepo, FakeBlocksRepository(blocks), FakeBookingsRepository(bookings))

    @Test
    fun refreshCargaEspaciosBloqueosYReservas() {
        val block = BloqueoDto(1, "futbol-a", "2026-12-01", "08:00 - 10:00", "Mantencion")
        val booking = Booking("Cancha Fútbol A", "2026-12-01", "10:00 - 12:00", "Entrenamiento")
        val vm = vm(blocks = listOf(block), bookings = listOf(booking))

        vm.refresh()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(3, state.spaces.size)
        assertEquals(listOf(block), state.blocks)
        assertEquals(listOf(booking), state.reservations)
    }

    @Test
    fun seleccionaElPrimerEspacioHabilitado() {
        val vm = vm()
        vm.refresh()

        assertEquals("futbol-a", vm.uiState.value.selectedSpaceId)
    }

    @Test
    fun losEspaciosDeshabilitadosNoSePuedenReservar() {
        val vm = vm()
        vm.refresh()

        assertEquals(listOf("futbol-a", "futbol-b"), vm.uiState.value.bookable.map { it.id })
    }

    @Test
    fun siElServicioFallaMuestraError() {
        val vm = vm(spacesRepo = FakeSpacesRepository(spaces, failing = true))
        vm.refresh()

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun siElEspacioElegidoSeDeshabilitaSeElijeOtro() {
        val fake = FakeSpacesRepository(spaces)
        val vm = vm(spacesRepo = fake)
        vm.refresh()
        vm.onSelectSpace("futbol-b")

        fake.spaces = spaces.map { if (it.id == "futbol-b") it.copy(available = false) else it }
        vm.refresh()

        assertEquals("futbol-a", vm.uiState.value.selectedSpaceId)
    }

    @Test
    fun laBusquedaFiltraLosEspacios() {
        val vm = vm()
        vm.refresh()
        vm.onQueryChange("Fútbol B")

        assertEquals(listOf("futbol-b"), vm.uiState.value.results.map { it.id })
    }

    @Test
    fun cambiarDeEspacioLimpiaElDiaSeleccionado() {
        val vm = vm()
        vm.refresh()
        vm.onSelectDay("2026-12-01")
        vm.onSelectSpace("futbol-b")

        assertNull(vm.uiState.value.selectedDay)
        assertEquals("futbol-b", vm.uiState.value.selectedSpaceId)
    }

    @Test
    fun anteriorYSiguienteMuevenLaSemanaOElMesSegunElModo() {
        val vm = vm()

        vm.onNext(); vm.onNext(); vm.onPrevious()
        assertEquals(1, vm.uiState.value.weekOffset)
        assertEquals(0, vm.uiState.value.monthOffset)

        vm.onModeChange(CalendarMode.Mes)
        vm.onSelectDay("2026-12-01")
        vm.onNext()
        assertEquals(1, vm.uiState.value.monthOffset)
        assertEquals(1, vm.uiState.value.weekOffset)
        assertNull(vm.uiState.value.selectedDay)
    }
}
