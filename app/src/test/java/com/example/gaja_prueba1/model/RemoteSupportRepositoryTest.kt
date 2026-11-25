package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.CantidadDto
import com.example.gaja_prueba1.data.remote.CrearIncidenciaDto
import com.example.gaja_prueba1.data.remote.IncidenciaDto
import com.example.gaja_prueba1.data.remote.IncidenciasApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RemoteSupportRepositoryTest {

    private fun httpError(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType()))
    )

    private class FakeIncidenciasApi : IncidenciasApi {
        val items = mutableListOf(
            IncidenciaDto(1, "cadete@gaja.cl", "Reservas", "No puedo reservar", "RESUELTA", "2026-11-20T10:15:00")
        )
        var error: HttpException? = null
        var lastCreate: CrearIncidenciaDto? = null
        var lastEmailQuery: String? = null

        override suspend fun contarPendientes() = CantidadDto(items.count { it.estado == "PENDIENTE" }.toLong())

        override suspend fun crear(body: CrearIncidenciaDto): IncidenciaDto {
            error?.let { throw it }
            lastCreate = body
            return IncidenciaDto(2, body.usuarioEmail, body.espacio, body.descripcion, "PENDIENTE", "2026-11-24T19:30:00")
                .also { items.add(it) }
        }

        override suspend fun getPorUsuario(email: String): List<IncidenciaDto> {
            error?.let { throw it }
            lastEmailQuery = email
            return items.filter { it.usuarioEmail == email }
        }
    }

    @Test
    fun mine_convierte_las_incidencias_en_solicitudes() = runBlocking {
        val api = FakeIncidenciasApi()
        val list = RemoteSupportRepository(api).mine(" cadete@gaja.cl ")

        assertEquals("cadete@gaja.cl", api.lastEmailQuery)
        assertEquals(listOf(SupportRequest(1, "Reservas", "No puedo reservar", true, "2026-11-20")), list)
    }

    @Test
    fun create_manda_la_categoria_como_espacio_y_la_solicitud_nace_pendiente() = runBlocking {
        val api = FakeIncidenciasApi()
        val created = RemoteSupportRepository(api).create(" cadete@gaja.cl ", "Mi cuenta", "  No puedo entrar  ")

        assertEquals("cadete@gaja.cl", api.lastCreate?.usuarioEmail)
        assertEquals("Mi cuenta", api.lastCreate?.espacio)
        assertEquals("No puedo entrar", api.lastCreate?.descripcion)
        assertFalse(created.resolved)
        assertEquals("2026-11-24", created.date)
    }

    @Test
    fun un_error_del_servicio_de_reservas_llega_con_su_mensaje() = runBlocking {
        val api = FakeIncidenciasApi().apply { error = httpError(400, """{"mensaje":"Describe el problema"}""") }
        try {
            RemoteSupportRepository(api).create("cadete@gaja.cl", "Otro", "x")
            fail("Debía lanzar SupportException")
        } catch (e: SupportException) {
            assertEquals("Describe el problema", e.message)
        }
    }

    @Test
    fun si_el_error_no_trae_mensaje_se_usa_el_codigo() = runBlocking {
        val api = FakeIncidenciasApi().apply { error = httpError(500, "no es json") }
        try {
            RemoteSupportRepository(api).mine("cadete@gaja.cl")
            fail("Debía lanzar SupportException")
        } catch (e: SupportException) {
            assertTrue(e.message!!.contains("500"))
        }
    }
}
