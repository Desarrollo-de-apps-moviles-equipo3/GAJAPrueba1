package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import com.example.gaja_prueba1.data.remote.CreateReservationRequest
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.model.BlocksRepository
import com.example.gaja_prueba1.model.RemoteReservationsRepository
import com.example.gaja_prueba1.model.ReservationRules
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class ReservationViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun elegir_inicio_y_final_construye_rango_y_restablecer_lo_limpia() = runTest {
        val repository = mockk<RemoteReservationsRepository>()
        val blocks = mockk<BlocksRepository>()
        stubAvailableDay(repository, blocks)
        val viewModel = ReservationViewModel(mockk<Application>(relaxed = true), repository, blocks)
        val space = testSpace()

        viewModel.start(space, "cadete@gaja.cl")
        advanceUntilIdle()
        viewModel.onDateSelected(tomorrow())
        advanceUntilIdle()

        viewModel.onTimeHourSelected(8)
        viewModel.onTimeHourSelected(11)
        assertEquals("08:00 - 11:00", viewModel.uiState.value.selectedSlot)

        viewModel.onResetTimeSelection()
        assertNull(viewModel.uiState.value.selectedStartHour)
        assertNull(viewModel.uiState.value.selectedEndHour)
        assertNull(viewModel.uiState.value.selectedSlot)
    }

    @Test
    fun enviar_rango_valido_guarda_la_reserva_con_el_horario_completo() = runTest {
        val repository = mockk<RemoteReservationsRepository>()
        val blocks = mockk<BlocksRepository>()
        stubAvailableDay(repository, blocks)
        coEvery { repository.crearReserva(any()) } returns ReservationResponse(
            id = 42,
            usuarioEmail = "cadete@gaja.cl",
            espacio = "Cancha A",
            fecha = "2026-11-27",
            horario = "08:00 - 11:00",
            motivo = "Entrenamiento",
            estado = "PENDIENTE",
            creadaEn = "2026-11-26T12:00:00"
        )
        val viewModel = ReservationViewModel(mockk<Application>(relaxed = true), repository, blocks)
        viewModel.start(testSpace(), " cadete@gaja.cl ")
        advanceUntilIdle()
        viewModel.onDateSelected(tomorrow())
        advanceUntilIdle()
        viewModel.onTimeHourSelected(8)
        viewModel.onTimeHourSelected(11)
        viewModel.onReasonChanged("Entrenamiento")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.saved)
        coVerify {
            repository.crearReserva(match<CreateReservationRequest> {
                it.usuarioEmail == "cadete@gaja.cl" && it.horario == "08:00 - 11:00"
            })
        }
    }

    @Test
    fun rango_que_cruza_un_bloque_ocupado_no_se_acepta() = runTest {
        val repository = mockk<RemoteReservationsRepository>()
        val blocks = mockk<BlocksRepository>()
        coEvery { repository.reglas() } returns ReservationRules(7, 2, 8, 23)
        coEvery { repository.horariosOcupados(any(), any()) } returns setOf("10:00 - 11:00")
        coEvery { blocks.getAll() } returns emptyList()
        val viewModel = ReservationViewModel(mockk<Application>(relaxed = true), repository, blocks)

        viewModel.start(testSpace(), "cadete@gaja.cl")
        advanceUntilIdle()
        viewModel.onDateSelected(tomorrow())
        advanceUntilIdle()
        viewModel.onTimeHourSelected(8)
        viewModel.onTimeHourSelected(11)

        assertNull(viewModel.uiState.value.selectedEndHour)
        assertEquals("Ese rango no está disponible", viewModel.uiState.value.slotError)
        coVerify(exactly = 0) { repository.crearReserva(any()) }
    }

    private fun stubAvailableDay(
        repository: RemoteReservationsRepository,
        blocks: BlocksRepository
    ) {
        coEvery { repository.reglas() } returns ReservationRules(7, 2, 8, 23)
        coEvery { repository.horariosOcupados(any(), any()) } returns emptySet()
        coEvery { blocks.getAll() } returns emptyList()
    }

    private fun tomorrow(): Long = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 12)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun testSpace() = Space(
        id = "cancha-a",
        name = "Cancha A",
        category = "Deportes",
        description = "Cancha de prueba",
        capacity = 14,
        available = true,
        colorHex = "#2F6B4F"
    )
}
