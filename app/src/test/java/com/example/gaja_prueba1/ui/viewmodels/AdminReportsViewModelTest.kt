package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import com.example.gaja_prueba1.model.AdminReportsRepository
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.AdminReservationsRepository
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminReportsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun carga_reportes_solo_con_reservas_aprobadas_y_rechazadas() = runTest {
        val source = mockk<AdminReservationsRepository>()
        coEvery { source.getAll() } returns listOf(
            reservation(1, "a@gaja.cl", EstadoReserva.APROBADA),
            reservation(2, "a@gaja.cl", EstadoReserva.RECHAZADA),
            reservation(3, "b@gaja.cl", EstadoReserva.PENDIENTE),
            reservation(4, "b@gaja.cl", EstadoReserva.CANCELADA)
        )
        val viewModel = AdminReportsViewModel(
            mockk<Application>(relaxed = true),
            AdminReportsRepository(source)
        )

        viewModel.load()
        advanceUntilIdle()

        val row = viewModel.uiState.value.rows.single()
        assertEquals("a@gaja.cl", row.label)
        assertEquals(1, row.approvedReservations)
        assertEquals(1, row.rejectedReservations)
        assertEquals(2, viewModel.uiState.value.totalApproved + viewModel.uiState.value.totalRejected)
    }

    @Test
    fun cambiar_a_espacios_agrupa_las_decisiones_por_recinto() = runTest {
        val source = mockk<AdminReservationsRepository>()
        coEvery { source.getAll() } returns listOf(
            reservation(1, "a@gaja.cl", EstadoReserva.APROBADA, "Cancha A"),
            reservation(2, "b@gaja.cl", EstadoReserva.RECHAZADA, "Cancha A"),
            reservation(3, "b@gaja.cl", EstadoReserva.APROBADA, "Cancha B")
        )
        val viewModel = AdminReportsViewModel(
            mockk<Application>(relaxed = true),
            AdminReportsRepository(source)
        )
        viewModel.load()
        advanceUntilIdle()

        viewModel.selectType(UsageReportType.ESPACIOS)
        val canchaA = viewModel.uiState.value.rows.first { it.label == "Cancha A" }
        assertEquals(1, canchaA.approvedReservations)
        assertEquals(1, canchaA.rejectedReservations)
    }

    private fun reservation(
        id: Long,
        email: String,
        status: EstadoReserva,
        space: String = "Cancha A"
    ) = AdminReservation(
        id = id,
        userEmail = email,
        space = space,
        date = "2026-11-27",
        slot = "09:00 - 10:00",
        reason = "Entrenamiento",
        status = status
    )
}
