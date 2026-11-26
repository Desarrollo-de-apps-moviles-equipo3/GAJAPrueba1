package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ActualizarEstadoDto
import com.example.gaja_prueba1.data.remote.ActualizarReglasReservaDto
import com.example.gaja_prueba1.data.remote.ReservaDto
import com.example.gaja_prueba1.data.remote.AdminReservasApi
import com.example.gaja_prueba1.data.remote.ReglasReservaDto
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class AdminReservationsRepositoryTest {

    private fun dto(id: Long, estado: String, motivoRechazo: String? = null) = ReservaDto(
        id = id, usuarioEmail = "cadete@gaja.cl", espacio = "Cancha 1", fecha = "2026-11-30",
        horario = "10:00 - 12:00", motivo = "Entrenamiento", estado = estado, motivoRechazo = motivoRechazo
    )

    /** API falsa: guarda lo que le piden, igual que lo haría el servicio. */
    private class FakeReservasApi(val data: MutableList<ReservaDto>) : AdminReservasApi {
        var lastFilter: String? = null
        var lastBody: ActualizarEstadoDto? = null
        var lastId: Long? = null

        override suspend fun listar(estado: String?): List<ReservaDto> {
            lastFilter = estado
            return data.filter { estado == null || it.estado == estado }
        }

        override suspend fun actualizarEstado(id: Long, body: ActualizarEstadoDto): ReservaDto {
            lastId = id
            lastBody = body
            val updated = data.first { it.id == id }.copy(
                estado = body.estado, motivoRechazo = body.motivoRechazo
            )
            data[data.indexOfFirst { it.id == id }] = updated
            return updated
        }

        override suspend fun obtenerReglas() = ReglasReservaDto(7, 2, 8, 23)

        override suspend fun actualizarReglas(body: ActualizarReglasReservaDto) =
            ReglasReservaDto(body.maxDiasAdelante, body.horasLimiteCancelacion, 8, 23)
    }

    @Test
    fun `getByStatus pide al servicio solo el estado indicado`() = runBlocking {
        val api = FakeReservasApi(mutableListOf(dto(1, "PENDIENTE"), dto(2, "APROBADA")))
        val result = RemoteAdminReservationsRepository(api).getByStatus(EstadoReserva.PENDIENTE)

        assertEquals("PENDIENTE", api.lastFilter)
        assertEquals(listOf(1L), result.map { it.id })
        assertEquals(EstadoReserva.PENDIENTE, result.first().status)
    }

    @Test
    fun `approve envia estado APROBADA sin motivo`() = runBlocking {
        val api = FakeReservasApi(mutableListOf(dto(1, "PENDIENTE")))
        val result = RemoteAdminReservationsRepository(api).approve(1)

        assertEquals(1L, api.lastId)
        assertEquals("APROBADA", api.lastBody?.estado)
        assertNull(api.lastBody?.motivoRechazo)
        assertEquals(EstadoReserva.APROBADA, result.status)
    }

    @Test
    fun `reject envia estado RECHAZADA con el motivo recortado`() = runBlocking {
        val api = FakeReservasApi(mutableListOf(dto(1, "PENDIENTE")))
        val result = RemoteAdminReservationsRepository(api).reject(1, "  Cancha en mantención  ")

        assertEquals("RECHAZADA", api.lastBody?.estado)
        assertEquals("Cancha en mantención", api.lastBody?.motivoRechazo)
        assertEquals(EstadoReserva.RECHAZADA, result.status)
        assertEquals("Cancha en mantención", result.rejectionReason)
    }

    @Test
    fun `un estado desconocido del servicio se muestra como pendiente`() {
        assertEquals(EstadoReserva.PENDIENTE, dto(1, "OTRO").toModel().status)
    }

    @Test
    fun `el motivo de rechazo es obligatorio`() {
        assertNotNull(RejectionValidator.validate(""))
        assertNotNull(RejectionValidator.validate("   "))
        assertNotNull(RejectionValidator.validate("no"))
        assertNotNull(RejectionValidator.validate("x".repeat(RejectionValidator.MAX + 1)))
        assertNull(RejectionValidator.validate("Cancha en mantención"))
    }
}
