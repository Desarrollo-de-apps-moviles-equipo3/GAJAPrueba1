package com.example.gaja_prueba1.model

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RemoteUsuariosRepositoryTest {

    private fun httpError(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun getAll_convierte_los_dto_en_usuarios() = runBlocking {
        val users = RemoteUsuariosRepository(InMemoryUsuariosApi()).getAll()

        assertEquals(2, users.size)
        assertEquals(EstadoPerfil.Aprobado, users[0].estado)
        assertEquals(EstadoPerfil.Pendiente, users[1].estado)
        // Un teléfono que el servicio manda como null se muestra como texto vacío.
        assertEquals("", users[1].telefono)
    }

    @Test
    fun fechaAlta_muestra_solo_el_dia() = runBlocking {
        val user = RemoteUsuariosRepository(InMemoryUsuariosApi()).get(1)
        assertEquals("2026-11-24", user.fechaAlta)
    }

    @Test
    fun el_estado_del_servicio_se_traduce_sin_importar_mayusculas() {
        assertEquals(EstadoPerfil.Desactivado, EstadoPerfil.fromService("DESACTIVADO"))
        assertEquals(EstadoPerfil.Rechazado, EstadoPerfil.fromService("rechazado"))
    }

    @Test
    fun create_manda_los_datos_sin_espacios_y_los_campos_vacios_como_null() = runBlocking {
        val api = InMemoryUsuariosApi()
        val created = RemoteUsuariosRepository(api).create(
            nombre = "  Ana Pérez ",
            email = " ana@gaja.cl ",
            password = "abcde",
            rol = "Oficial",
            telefono = "  ",
            institucion = ""
        )

        assertEquals("Ana Pérez", api.lastCreate?.nombre)
        assertEquals("ana@gaja.cl", api.lastCreate?.email)
        assertNull(api.lastCreate?.telefono)
        assertNull(api.lastCreate?.institucion)
        assertEquals(EstadoPerfil.Aprobado, created.estado)
    }

    @Test
    fun approve_deactivate_y_activate_cambian_el_estado() = runBlocking {
        val repo = RemoteUsuariosRepository(InMemoryUsuariosApi())

        assertEquals(EstadoPerfil.Aprobado, repo.approve(2).estado)
        assertEquals(EstadoPerfil.Desactivado, repo.deactivate(2).estado)
        assertEquals(EstadoPerfil.Aprobado, repo.activate(2).estado)
    }

    @Test
    fun reject_guarda_el_motivo_sin_espacios_sobrantes() = runBlocking {
        val api = InMemoryUsuariosApi()
        val rejected = RemoteUsuariosRepository(api).reject(2, "  Datos incompletos ")

        assertEquals("Datos incompletos", api.lastReject?.motivo)
        assertEquals(EstadoPerfil.Rechazado, rejected.estado)
        assertEquals("Datos incompletos", rejected.motivoRechazo)
    }

    @Test
    fun update_manda_las_notificaciones_solo_si_se_indican() = runBlocking {
        val api = InMemoryUsuariosApi()
        RemoteUsuariosRepository(api).update(1, "Nuevo Nombre", "+56911112222", "", notifAlertas = false)

        assertEquals("Nuevo Nombre", api.lastUpdate?.nombre)
        assertEquals("+56911112222", api.lastUpdate?.telefono)
        assertNull(api.lastUpdate?.notifReservas)
        assertEquals(false, api.lastUpdate?.notifAlertas)
    }

    @Test
    fun sessions_convierte_las_sesiones() = runBlocking {
        val sessions = RemoteUsuariosRepository(InMemoryUsuariosApi()).sessions(1)
        assertEquals(listOf(Sesion(1, "2026-11-24T19:30:00Z")), sessions)
    }

    @Test
    fun un_error_del_servicio_llega_con_su_mensaje() = runBlocking {
        val api = InMemoryUsuariosApi().apply {
            error = httpError(409, """{"error":"Ya existe un usuario con ese correo"}""")
        }
        try {
            RemoteUsuariosRepository(api).create("Ana", "ana@gaja.cl", "abcde", "Cadete", "", "")
            fail("Debía lanzar UsuarioException")
        } catch (e: UsuarioException) {
            assertEquals("Ya existe un usuario con ese correo", e.message)
        }
    }

    @Test
    fun si_el_error_no_trae_mensaje_se_usa_el_codigo() = runBlocking {
        val api = InMemoryUsuariosApi().apply { error = httpError(500, "no es json") }
        try {
            RemoteUsuariosRepository(api).getAll()
            fail("Debía lanzar UsuarioException")
        } catch (e: UsuarioException) {
            assertEquals("El servicio respondió con error 500", e.message)
        }
    }
}
