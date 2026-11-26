package com.example.gaja_prueba1.model

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AdminReportsRepositoryTest {

    @Test
    fun reportes_delegan_la_lectura_al_repositorio_de_reservas() = runTest {
        val source = mockk<AdminReservationsRepository>()
        val reservation = AdminReservation(
            id = 1,
            userEmail = "cadete@gaja.cl",
            space = "Cancha A",
            date = "2026-11-27",
            slot = "09:00 - 11:00",
            reason = "Entrenamiento",
            status = EstadoReserva.APROBADA
        )
        coEvery { source.getAll() } returns listOf(reservation)

        assertEquals(listOf(reservation), AdminReportsRepository(source).reservations())
    }

    @Test
    fun guardar_reglas_retorna_la_configuracion_persistida() = runTest {
        val source = mockk<AdminReservationsRepository>()
        val rules = ReservationRules(maxDaysAhead = 12, cancellationLimitHours = 4)
        coEvery { source.updateRules(rules) } returns rules

        assertEquals(rules, AdminReportsRepository(source).updateRules(rules))
    }
}
