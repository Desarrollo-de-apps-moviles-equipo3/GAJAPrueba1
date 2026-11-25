package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.AdminReservationsRepository
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.model.RejectionValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminReservationsViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun reservation(id: Long, status: EstadoReserva, reason: String? = null) = AdminReservation(
        id = id, userEmail = "cadete@gaja.cl", space = "Cancha 1", date = "2026-11-30",
        slot = "10:00 - 12:00", reason = "Entrenamiento", status = status, rejectionReason = reason
    )

    private val pending = reservation(1, EstadoReserva.PENDIENTE)
    private val approved = reservation(2, EstadoReserva.APROBADA)
    private val rejected = reservation(3, EstadoReserva.RECHAZADA, "Mantención")

    /** Repositorio simulado con MockK: devuelve una lista por estado. */
    private fun repo(): AdminReservationsRepository = mockk {
        coEvery { getByStatus(EstadoReserva.PENDIENTE) } returns listOf(pending)
        coEvery { getByStatus(EstadoReserva.APROBADA) } returns listOf(approved)
        coEvery { getByStatus(EstadoReserva.RECHAZADA) } returns listOf(rejected)
    }

    private fun viewModel(repository: AdminReservationsRepository = repo()) =
        AdminReservationsViewModel(mockk<Application>(relaxed = true), repository)

    @Test
    fun `al iniciar carga las tres listas y cuenta las pendientes`() {
        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertEquals(1, state.pendingCount)
        assertEquals(listOf(pending), state.items[ReservationsTab.Pendientes])
        assertEquals(listOf(approved), state.items[ReservationsTab.Aprobadas])
        assertEquals(listOf(rejected), state.items[ReservationsTab.Rechazadas])
    }

    @Test
    fun `si el servicio falla muestra un mensaje de error`() {
        val failing = mockk<AdminReservationsRepository> {
            coEvery { getByStatus(any()) } throws RuntimeException("sin conexión")
        }
        val state = viewModel(failing).uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun `cambiar de pestana muestra la lista de ese estado`() {
        val vm = viewModel()
        vm.onTabSelected(ReservationsTab.Aprobadas)

        assertEquals(ReservationsTab.Aprobadas, vm.uiState.value.tab)
        assertEquals(listOf(approved), vm.uiState.value.current)
    }

    @Test
    fun `abrir y cerrar el detalle actualiza la seleccion`() {
        val vm = viewModel()
        vm.onOpenDetail(pending)
        assertEquals(pending, vm.uiState.value.selected)

        vm.onCloseDetail()
        assertNull(vm.uiState.value.selected)
    }

    @Test
    fun `aprobar llama al repositorio y muestra el resultado`() {
        val repository = repo()
        val done = pending.copy(status = EstadoReserva.APROBADA)
        coEvery { repository.approve(1) } returns done
        val vm = viewModel(repository)

        vm.onOpenDetail(pending)
        vm.onApprove()

        coVerify(exactly = 1) { repository.approve(1) }
        val state = vm.uiState.value
        assertEquals(DecisionResult(done), state.result)
        assertNull(state.selected)
        assertFalse(state.isApproving)
    }

    @Test
    fun `si aprobar falla muestra mensaje y no hay resultado`() {
        val repository = repo()
        coEvery { repository.approve(any()) } throws RuntimeException("error")
        val vm = viewModel(repository)

        vm.onOpenDetail(pending)
        vm.onApprove()

        val state = vm.uiState.value
        assertEquals("No pudimos aprobar la solicitud", state.message)
        assertNull(state.result)
        assertFalse(state.isApproving)
    }

    @Test
    fun `aprobar sin solicitud seleccionada no hace nada`() {
        val repository = repo()
        val vm = viewModel(repository)

        vm.onApprove()

        coVerify(exactly = 0) { repository.approve(any()) }
    }

    @Test
    fun `iniciar y cancelar el rechazo abre y cierra el formulario`() {
        val vm = viewModel()
        vm.onStartReject()
        assertNotNull(vm.uiState.value.rejectForm)

        vm.onCancelReject()
        assertNull(vm.uiState.value.rejectForm)
    }

    @Test
    fun `el motivo se recorta al maximo permitido`() {
        val vm = viewModel()
        vm.onStartReject()
        vm.onReasonChange("a".repeat(RejectionValidator.MAX + 50))

        assertEquals(RejectionValidator.MAX, vm.uiState.value.rejectForm?.reason?.length)
    }

    @Test
    fun `rechazar sin motivo muestra error y no llama al servicio`() {
        val repository = repo()
        val vm = viewModel(repository)

        vm.onOpenDetail(pending)
        vm.onStartReject()
        vm.onConfirmReject()

        assertNotNull(vm.uiState.value.rejectForm?.error)
        coVerify(exactly = 0) { repository.reject(any(), any()) }
    }

    @Test
    fun `rechazar con motivo valido llama al servicio y muestra el resultado`() {
        val repository = repo()
        val done = pending.copy(status = EstadoReserva.RECHAZADA, rejectionReason = "Cancha en mantención")
        coEvery { repository.reject(1, "Cancha en mantención") } returns done
        val vm = viewModel(repository)

        vm.onOpenDetail(pending)
        vm.onStartReject()
        vm.onReasonChange("Cancha en mantención")
        vm.onConfirmReject()

        coVerify(exactly = 1) { repository.reject(1, "Cancha en mantención") }
        val state = vm.uiState.value
        assertEquals(DecisionResult(done), state.result)
        assertNull(state.rejectForm)
        assertNull(state.selected)
    }

    @Test
    fun `si rechazar falla muestra mensaje y deja de guardar`() {
        val repository = repo()
        coEvery { repository.reject(any(), any()) } throws RuntimeException("error")
        val vm = viewModel(repository)

        vm.onOpenDetail(pending)
        vm.onStartReject()
        vm.onReasonChange("Cancha en mantención")
        vm.onConfirmReject()

        val state = vm.uiState.value
        assertEquals("No pudimos rechazar la solicitud", state.message)
        assertEquals(false, state.rejectForm?.isSaving)
    }

    @Test
    fun `cerrar el resultado y el mensaje los limpia`() {
        val repository = repo()
        coEvery { repository.approve(any()) } returns pending.copy(status = EstadoReserva.APROBADA)
        val vm = viewModel(repository)

        vm.onOpenDetail(pending)
        vm.onApprove()
        assertNotNull(vm.uiState.value.result)
        vm.onResultClose()
        assertNull(vm.uiState.value.result)

        vm.onMessageShown()
        assertTrue(vm.uiState.value.message == null)
    }
}