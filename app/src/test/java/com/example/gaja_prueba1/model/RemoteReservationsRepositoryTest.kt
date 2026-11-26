package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ReglasReservaDto
import com.example.gaja_prueba1.data.remote.ReservasApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RemoteReservationsRepositoryTest {

    @Test
    fun reglas_remotas_se_convierten_al_modelo_de_la_app() = runTest {
        val api = mockk<ReservasApi>()
        coEvery { api.obtenerReglas() } returns ReglasReservaDto(
            maxDiasAdelante = 10,
            horasLimiteCancelacion = 3,
            horaApertura = 8,
            horaCierre = 23
        )

        val rules = RemoteReservationsRepository(api).reglas()

        assertEquals(ReservationRules(10, 3, 8, 23), rules)
    }

    @Test
    fun horarios_ocupados_se_devuelven_sin_duplicados() = runTest {
        val api = mockk<ReservasApi>()
        coEvery { api.horariosOcupados("2026-11-27", "Cancha A") } returns
            listOf("09:00 - 10:00", "09:00 - 10:00", "11:00 - 12:00")

        val occupied = RemoteReservationsRepository(api)
            .horariosOcupados("2026-11-27", "Cancha A")

        assertEquals(setOf("09:00 - 10:00", "11:00 - 12:00"), occupied)
    }
}
