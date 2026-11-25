package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import com.example.gaja_prueba1.data.remote.CantidadDto
import com.example.gaja_prueba1.data.remote.IncidenciasApi
import com.example.gaja_prueba1.model.AdminHomeRepository
import com.example.gaja_prueba1.model.AdminHomeSummary
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.AdminReservationsRepository
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.model.RemoteAdminHomeRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminHomeViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val summary = AdminHomeSummary(pendingReservations = 2, openIncidents = 1, items = emptyList())

    private fun viewModel(repository: AdminHomeRepository) =
        AdminHomeViewModel(mockk<Application>(relaxed = true), repository)

    @Test
    fun `al iniciar carga el resumen y la fecha`() {
        val repository = mockk<AdminHomeRepository> {
            coEvery { load(any()) } returns summary
        }
        val state = viewModel(repository).uiState.value

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(summary, state.summary)
        assertTrue(state.dateLabel.isNotBlank())
        assertTrue(state.dateLabel.first().isUpperCase())
    }

    @Test
    fun `pide el resumen con la fecha de hoy en formato ISO`() {
        val repository = mockk<AdminHomeRepository> {
            coEvery { load(any()) } returns summary
        }
        viewModel(repository)

        coVerify(exactly = 1) {
            repository.load(match { it.matches(Regex("\\d{4}-\\d{2}-\\d{2}")) })
        }
    }

    @Test
    fun `si el servicio falla muestra un mensaje de error`() {
        val repository = mockk<AdminHomeRepository> {
            coEvery { load(any()) } throws RuntimeException("sin conexión")
        }
        val state = viewModel(repository).uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertNull(state.summary)
    }

    @Test
    fun `volver a cargar despues de un error recupera el resumen`() {
        val repository = mockk<AdminHomeRepository>()
        coEvery { repository.load(any()) } throws RuntimeException("sin conexión")
        val vm = viewModel(repository)
        assertNotNull(vm.uiState.value.errorMessage)

        coEvery { repository.load(any()) } returns summary
        vm.load()

        val state = vm.uiState.value
        assertNull(state.errorMessage)
        assertEquals(summary, state.summary)
    }

    // ---------- RemoteAdminHomeRepository ----------

    private fun reservation(id: Long, date: String = "2099-01-10", slot: String = "10:00 - 12:00") =
        AdminReservation(
            id = id, userEmail = "cadete@gaja.cl", space = "Cancha 1", date = date, slot = slot,
            reason = "Entrenamiento", status = EstadoReserva.PENDIENTE
        )

    @Test
    fun `el repositorio cuenta las pendientes y las incidencias abiertas`() = runBlocking {
        val reservations = mockk<AdminReservationsRepository> {
            coEvery { getByStatus(EstadoReserva.PENDIENTE) } returns listOf(reservation(1), reservation(2, slot = "14:00 - 16:00"))
        }
        val incidents = mockk<IncidenciasApi> {
            coEvery { contarPendientes() } returns CantidadDto(4)
        }

        val result = RemoteAdminHomeRepository(reservations, incidents).load("2026-11-25")

        assertEquals(2, result.pendingReservations)
        assertEquals(4, result.openIncidents)
        assertEquals(2, result.items.size)
    }

    @Test
    fun `si falla el contador de incidencias el resumen igual se entrega`() = runBlocking {
        val reservations = mockk<AdminReservationsRepository> {
            coEvery { getByStatus(EstadoReserva.PENDIENTE) } returns listOf(reservation(1))
        }
        val incidents = mockk<IncidenciasApi> {
            coEvery { contarPendientes() } throws RuntimeException("sin conexión")
        }

        val result = RemoteAdminHomeRepository(reservations, incidents).load("2026-11-25")

        assertEquals(1, result.pendingReservations)
        assertNull(result.openIncidents)
    }
}