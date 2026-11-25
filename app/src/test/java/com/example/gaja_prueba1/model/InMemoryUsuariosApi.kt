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
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException

/** API de usuarios falsa que guarda todo en memoria y puede simular un error del servicio. */
class InMemoryUsuariosApi : UsuariosApi {
    val users = mutableListOf(
        UsuarioDto(1, "cadete@gaja.cl", "Cadete de Prueba", "Cadete", "APROBADO", creadoEn = "2026-11-24T19:30:00Z"),
        UsuarioDto(2, "nuevo@gaja.cl", "Cadete Nuevo", "Cadete", "PENDIENTE", telefono = null)
    )
    var error: HttpException? = null
    var lastCreate: RegistroRequestDto? = null
    var lastReject: RechazoDto? = null
    var lastUpdate: ActualizarRequestDto? = null
    var lastEmailQuery: String? = null

    private fun check() {
        error?.let { throw it }
    }

    private fun replace(id: Long, change: (UsuarioDto) -> UsuarioDto): UsuarioDto {
        val i = users.indexOfFirst { it.id == id }
        users[i] = change(users[i])
        return users[i]
    }

    override suspend fun login(body: LoginRequestDto): LoginResponseDto = TODO("no se usa en estas pruebas")

    override suspend fun registrar(body: RegistroRequestDto): UsuarioDto = crear(body)

    override suspend fun crear(body: RegistroRequestDto): UsuarioDto {
        check()
        lastCreate = body
        val dto = UsuarioDto(
            id = 100L + users.size,
            email = body.email,
            nombre = body.nombre,
            rol = body.rol,
            estado = "APROBADO",
            telefono = body.telefono,
            institucion = body.institucion
        )
        users.add(dto)
        return dto
    }

    override suspend fun getUsuarios(estado: String?, rol: String?): List<UsuarioDto> {
        check()
        return users.filter { (estado == null || it.estado == estado) && (rol == null || it.rol == rol) }
    }

    override suspend fun getPorCorreo(email: String): UsuarioDto {
        check()
        lastEmailQuery = email
        return users.firstOrNull { it.email.equals(email, ignoreCase = true) }
            ?: throw HttpException(
                retrofit2.Response.error<Any>(
                    404,
                    """{"error":"Usuario no encontrado"}""".toResponseBody("application/json".toMediaType())
                )
            )
    }

    override suspend fun getUsuario(id: Long): UsuarioDto {
        check()
        return users.first { it.id == id }
    }

    override suspend fun actualizar(id: Long, body: ActualizarRequestDto): UsuarioDto {
        check()
        lastUpdate = body
        return replace(id) { it.copy(nombre = body.nombre, telefono = body.telefono, institucion = body.institucion) }
    }

    override suspend fun cambiarPassword(id: Long, body: CambiarPasswordDto) {
        check()
    }

    override suspend fun aprobar(id: Long): UsuarioDto {
        check()
        return replace(id) { it.copy(estado = "APROBADO") }
    }

    override suspend fun rechazar(id: Long, body: RechazoDto): UsuarioDto {
        check()
        lastReject = body
        return replace(id) { it.copy(estado = "RECHAZADO", motivoRechazo = body.motivo) }
    }

    override suspend fun desactivar(id: Long): UsuarioDto {
        check()
        return replace(id) { it.copy(estado = "DESACTIVADO") }
    }

    override suspend fun activar(id: Long): UsuarioDto {
        check()
        return replace(id) { it.copy(estado = "APROBADO") }
    }

    override suspend fun getSesiones(id: Long): List<SesionDto> {
        check()
        return listOf(SesionDto(1, "2026-11-24T19:30:00Z"))
    }

    override suspend fun eliminar(id: Long) {
        check()
        users.removeAll { it.id == id }
    }
}
