package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ActualizarRequestDto
import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.CambiarPasswordDto
import com.example.gaja_prueba1.data.remote.RechazoDto
import com.example.gaja_prueba1.data.remote.RegistroRequestDto
import com.example.gaja_prueba1.data.remote.UsuarioDto
import com.example.gaja_prueba1.data.remote.UsuariosApi
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/** Estado de un perfil. [label] es lo que se muestra en pantalla. */
enum class EstadoPerfil(val label: String) {
    Pendiente("Pendiente"),
    Aprobado("Aprobado"),
    Rechazado("Rechazado"),
    Desactivado("Desactivado");

    companion object {
        /** El servicio manda PENDIENTE, APROBADO, RECHAZADO o DESACTIVADO. */
        fun fromService(value: String): EstadoPerfil =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: Pendiente
    }
}

data class Usuario(
    val id: Long,
    val email: String,
    val nombre: String,
    val rol: String,
    val estado: EstadoPerfil,
    val telefono: String = "",
    val institucion: String = "",
    val motivoRechazo: String? = null,
    val notifReservas: Boolean = true,
    val notifAlertas: Boolean = true,
    val creadoEn: String = ""
)

/** Fecha de alta lista para mostrar: el servicio manda "2026-11-24T19:30:00Z" y mostramos "2026-11-24". */
val Usuario.fechaAlta: String get() = creadoEn.take(10)

/** Error de negocio con un mensaje listo para mostrar (por ejemplo "Ya existe un usuario con ese correo"). */
class UsuarioException(message: String) : Exception(message)

data class Sesion(val id: Long, val fecha: String)

interface UsuariosRepository {
    suspend fun getAll(): List<Usuario>
    suspend fun get(id: Long): Usuario

    /** El perfil de quien tiene la sesión abierta. Falla con [UsuarioException] si no existe en el servicio. */
    suspend fun getByEmail(email: String): Usuario
    suspend fun create(
        nombre: String,
        email: String,
        password: String,
        rol: String,
        telefono: String,
        institucion: String
    ): Usuario

    suspend fun update(
        id: Long,
        nombre: String,
        telefono: String,
        institucion: String,
        notifReservas: Boolean? = null,
        notifAlertas: Boolean? = null
    ): Usuario

    suspend fun changePassword(id: Long, actual: String, nueva: String)
    suspend fun approve(id: Long): Usuario
    suspend fun reject(id: Long, motivo: String): Usuario
    suspend fun deactivate(id: Long): Usuario
    suspend fun activate(id: Long): Usuario
    suspend fun sessions(id: Long): List<Sesion>
}

private fun defaultUsuariosApi(): UsuariosApi =
    ApiClient.create(ApiConfig.USUARIOS_URL, UsuariosApi::class.java)

/**
 * Perfiles guardados en el microservicio de usuarios.
 * Si el servicio responde un error (por ejemplo 409 por un correo repetido) lo convertimos en
 * [UsuarioException] con el mensaje que mandó el servicio.
 */
class RemoteUsuariosRepository(
    private val api: UsuariosApi = defaultUsuariosApi()
) : UsuariosRepository {

    override suspend fun getAll(): List<Usuario> = call { api.getUsuarios().map { it.toUsuario() } }

    override suspend fun get(id: Long): Usuario = call { api.getUsuario(id).toUsuario() }

    override suspend fun getByEmail(email: String): Usuario =
        call { api.getPorCorreo(email.trim()).toUsuario() }

    override suspend fun create(
        nombre: String,
        email: String,
        password: String,
        rol: String,
        telefono: String,
        institucion: String
    ): Usuario = call {
        api.crear(
            RegistroRequestDto(
                email = email.trim(),
                password = password,
                nombre = nombre.trim(),
                rol = rol,
                telefono = telefono.trim().ifBlank { null },
                institucion = institucion.trim().ifBlank { null }
            )
        ).toUsuario()
    }

    /**
     * Solicitud pública de perfil ("Crear perfil" en el inicio de sesión).
     * El perfil queda PENDIENTE hasta que el administrador lo apruebe.
     */
    suspend fun register(
        nombre: String,
        email: String,
        password: String,
        rol: String,
        telefono: String,
        institucion: String
    ): Usuario = call {
        api.registrar(
            RegistroRequestDto(
                email = email.trim(),
                password = password,
                nombre = nombre.trim(),
                rol = rol,
                telefono = telefono.trim().ifBlank { null },
                institucion = institucion.trim().ifBlank { null }
            )
        ).toUsuario()
    }

    override suspend fun update(
        id: Long,
        nombre: String,
        telefono: String,
        institucion: String,
        notifReservas: Boolean?,
        notifAlertas: Boolean?
    ): Usuario = call {
        api.actualizar(
            id,
            ActualizarRequestDto(
                nombre = nombre.trim(),
                telefono = telefono.trim().ifBlank { null },
                institucion = institucion.trim().ifBlank { null },
                notifReservas = notifReservas,
                notifAlertas = notifAlertas
            )
        ).toUsuario()
    }

    override suspend fun changePassword(id: Long, actual: String, nueva: String) =
        call { api.cambiarPassword(id, CambiarPasswordDto(actual, nueva)) }

    override suspend fun approve(id: Long): Usuario = call { api.aprobar(id).toUsuario() }

    override suspend fun reject(id: Long, motivo: String): Usuario =
        call { api.rechazar(id, RechazoDto(motivo.trim())).toUsuario() }

    override suspend fun deactivate(id: Long): Usuario = call { api.desactivar(id).toUsuario() }

    override suspend fun activate(id: Long): Usuario = call { api.activar(id).toUsuario() }

    override suspend fun sessions(id: Long): List<Sesion> =
        call { api.getSesiones(id).map { Sesion(it.id, it.fecha) } }

    private suspend fun <T> call(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: HttpException) {
            throw UsuarioException(e.serverMessage() ?: "El servicio respondió con error ${e.code()}")
        }
    }
}

private fun UsuarioDto.toUsuario() = Usuario(
    id = id,
    email = email,
    nombre = nombre,
    rol = rol,
    estado = EstadoPerfil.fromService(estado),
    telefono = telefono.orEmpty(),
    institucion = institucion.orEmpty(),
    motivoRechazo = motivoRechazo,
    notifReservas = notifReservas,
    notifAlertas = notifAlertas,
    creadoEn = creadoEn
)

/** El servicio responde los errores como {"error": "mensaje"}. */
private fun HttpException.serverMessage(): String? {
    return try {
        val raw = response()?.errorBody()?.string() ?: return null
        JsonParser.parseString(raw).asJsonObject.get("error")?.asString
    } catch (e: Exception) {
        null
    }
}