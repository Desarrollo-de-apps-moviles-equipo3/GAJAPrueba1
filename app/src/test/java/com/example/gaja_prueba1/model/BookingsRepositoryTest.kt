package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ActualizarEstadoDto
import com.example.gaja_prueba1.data.remote.ActualizarReglasReservaDto
import com.example.gaja_prueba1.data.remote.AdminReservasApi
import com.example.gaja_prueba1.data.remote.ReservaDto
import com.example.gaja_prueba1.data.remote.ReglasReservaDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class BookingsRepositoryTest {

    private fun reserva(id: Long, estado: String, horario: String = "08:00 - 10:00") = ReservaDto(
        id = id, usuarioEmail = "cadete@gaja.cl", espacio = "Cancha 1", fecha = "2026-11-26",
        horario = horario, motivo = "Entrenamiento", estado = estado
    )

    private class FakeApi(val items: List<ReservaDto>) : AdminReservasApi {
        override suspend fun listar(estado: String?) = items
        override suspend fun actualizarEstado(id: Long, body: ActualizarEstadoDto): ReservaDto = items.first { it.id == id }
        override suspend fun obtenerReglas() = ReglasReservaDto(7, 2, 8, 23)
        override suspend fun actualizarReglas(body: ActualizarReglasReservaDto) =
            ReglasReservaDto(body.maxDiasAdelante, body.horasLimiteCancelacion, 8, 23)
    }

    @Test
    fun solo_las_reservas_pendientes_y_aprobadas_ocupan_horario() = runBlocking {
        val api = FakeApi(
            listOf(
                reserva(1, "PENDIENTE"),
                reserva(2, "APROBADA", "10:00 - 12:00"),
                reserva(3, "RECHAZADA", "12:00 - 14:00"),
                reserva(4, "CANCELADA", "14:00 - 16:00")
            )
        )
        val active = RemoteBookingsRepository(api).getActive()
        assertEquals(listOf("08:00 - 10:00", "10:00 - 12:00"), active.map { it.timeSlot })
    }

    @Test
    fun convierte_la_reserva_del_servicio_en_un_horario_ocupado() = runBlocking {
        val booking = RemoteBookingsRepository(FakeApi(listOf(reserva(1, "aprobada")))).getActive().single()
        assertEquals(Booking("Cancha 1", "2026-11-26", "08:00 - 10:00", "Entrenamiento"), booking)
    }
}
