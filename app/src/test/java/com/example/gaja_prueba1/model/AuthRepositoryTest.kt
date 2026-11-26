package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ActualizarRequestDto
import com.example.gaja_prueba1.data.remote.CambiarPasswordDto
import com.example.gaja_prueba1.data.remote.LoginRequestDto
import com.example.gaja_prueba1.data.remote.LoginResponseDto
import com.example.gaja_prueba1.data.remote.RechazoDto
import com.example.gaja_prueba1.data.remote.RegistroRequestDto
import com.example.gaja_prueba1.data.remote.SesionDto
import com.example.gaja_prueba1.data.remote.UsuarioDto
import com.example.gaja_prueba1.data.remote.UsuariosApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthRepositoryTest {

    private fun httpError(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType()))
    )

    /** API falsa de usuarios: solo el login hace algo; el resto no se usa en estas pruebas. */
    private class FakeUsuariosApi : UsuariosApi {
        var error: Exception? = null
        var lastLogin: LoginRequestDto? = null
        var calls = 0

        override suspend fun login(body: LoginRequestDto): LoginResponseDto {
            calls++
            lastLogin = body
            error?.let { throw it }
            return LoginResponseDto(7, body.email, "Cadete de Prueba", "Cadete", "APROBADO", "token-abc")
        }

        override suspend fun registrar(body: RegistroRequestDto): UsuarioDto = TODO()
        override suspend fun crear(body: RegistroRequestDto): UsuarioDto = TODO()
        override suspend fun getUsuarios(estado: String?, rol: String?): List<UsuarioDto> = TODO()
        override suspend fun getPorCorreo(email: String): UsuarioDto = TODO()
        override suspend fun getUsuario(id: Long): UsuarioDto = TODO()
        override suspend fun actualizar(id: Long, body: ActualizarRequestDto): UsuarioDto = TODO()
        override suspend fun cambiarPassword(id: Long, body: CambiarPasswordDto) = TODO()
        override suspend fun aprobar(id: Long): UsuarioDto = TODO()
        override suspend fun rechazar(id: Long, body: RechazoDto): UsuarioDto = TODO()
        override suspend fun desactivar(id: Long): UsuarioDto = TODO()
        override suspend fun activar(id: Long): UsuarioDto = TODO()
        override suspend fun getSesiones(id: Long): List<SesionDto> = TODO()
        override suspend fun eliminar(id: Long) = TODO()
    }

    @Test
    fun login_correcto_devuelve_token_correo_y_rol_del_servicio() = runBlocking {
        val api = FakeUsuariosApi()
        val result = AuthRepository(api).loginUser("  CADETE@gaja.cl ", "12345")

        assertEquals(LoginResult.Success("token-abc", "cadete@gaja.cl", "Cadete"), result)
        assertEquals("cadete@gaja.cl", api.lastLogin?.email)
        assertEquals("12345", api.lastLogin?.password)
    }

    @Test
    fun el_rol_de_la_cuenta_se_recibe_del_servicio() = runBlocking {
        val api = FakeUsuariosApi()
        val result = AuthRepository(api).loginUser("profe_exe@duoc.cl", "12345")

        assertEquals(LoginResult.Success("token-abc", "profe_exe@duoc.cl", "Cadete"), result)
        assertEquals(1, api.calls)
    }

    @Test
    fun credenciales_incorrectas_muestran_el_mensaje_del_servicio() = runBlocking {
        val api = FakeUsuariosApi().apply {
            error = httpError(401, """{"error":"Correo o contraseña incorrectos"}""")
        }
        val result = AuthRepository(api).loginUser("cadete@gaja.cl", "mala")

        assertTrue(result is LoginResult.Error)
        assertEquals("Correo o contraseña incorrectos", (result as LoginResult.Error).exception.message)
    }

    @Test
    fun un_perfil_rechazado_muestra_el_motivo() = runBlocking {
        val api = FakeUsuariosApi().apply {
            error = httpError(403, """{"error":"Tu perfil fue rechazado: Los datos no coinciden"}""")
        }
        val result = AuthRepository(api).loginUser("cadete@gaja.cl", "12345")

        assertEquals(
            "Tu perfil fue rechazado: Los datos no coinciden",
            (result as LoginResult.Error).exception.message
        )
    }

    @Test
    fun si_el_servicio_responde_sin_mensaje_se_usa_uno_generico_con_el_codigo() = runBlocking {
        val api = FakeUsuariosApi().apply { error = httpError(500, "no es json") }
        val result = AuthRepository(api).loginUser("cadete@gaja.cl", "12345")

        assertEquals(
            "No pudimos iniciar sesión (error 500)",
            (result as LoginResult.Error).exception.message
        )
    }

    @Test
    fun sin_conexion_avisa_que_hay_que_encender_el_servicio() = runBlocking {
        val api = FakeUsuariosApi().apply { error = IOException("Failed to connect") }
        val result = AuthRepository(api).loginUser("cadete@gaja.cl", "12345")

        assertTrue((result as LoginResult.Error).exception.message!!.contains("servicio de usuarios"))
    }
}
