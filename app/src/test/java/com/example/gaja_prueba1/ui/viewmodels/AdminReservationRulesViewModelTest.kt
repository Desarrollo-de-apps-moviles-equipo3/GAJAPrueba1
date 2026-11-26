package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import com.example.gaja_prueba1.model.AdminReportsRepository
import com.example.gaja_prueba1.model.AdminReservationsRepository
import com.example.gaja_prueba1.model.ReservationRules
import com.example.gaja_prueba1.testutil.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AdminReservationRulesViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun carga_reglas_y_guarda_los_valores_ingresados() = runTest {
        val source = mockk<AdminReservationsRepository>()
        val original = ReservationRules(maxDaysAhead = 7, cancellationLimitHours = 2)
        val updated = ReservationRules(maxDaysAhead = 14, cancellationLimitHours = 4)
        coEvery { source.getRules() } returns original
        coEvery { source.updateRules(updated) } returns updated
        val viewModel = AdminReservationRulesViewModel(
            mockk<Application>(relaxed = true),
            AdminReportsRepository(source)
        )

        viewModel.load()
        advanceUntilIdle()
        viewModel.onMaxDaysChange("14")
        viewModel.onCancellationHoursChange("4")
        viewModel.save()
        advanceUntilIdle()

        assertEquals(updated, viewModel.uiState.value.rules)
        assertEquals("Reglas guardadas", viewModel.uiState.value.message)
        coVerify(exactly = 1) { source.updateRules(updated) }
    }

    @Test
    fun no_guarda_valores_fuera_de_rango() = runTest {
        val source = mockk<AdminReservationsRepository>()
        coEvery { source.getRules() } returns ReservationRules(7, 2)
        val viewModel = AdminReservationRulesViewModel(
            mockk<Application>(relaxed = true),
            AdminReportsRepository(source)
        )
        viewModel.load()
        advanceUntilIdle()
        viewModel.onMaxDaysChange("366")
        viewModel.onCancellationHoursChange("2")

        viewModel.save()

        assertEquals("Revisa los valores: días 0–365 y cancelación 0–48 horas.", viewModel.uiState.value.error)
        coVerify(exactly = 0) { source.updateRules(any()) }
    }
}
