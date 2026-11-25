package com.example.gaja_prueba1.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

data class LoginRequestDto(val email: String, val password: String, val rol: String)

data class LoginResponseDto(
    val id: Long,
    val email: String,
    val nombre: String,
    val rol: String,
    val estado: String,
    val token: String
)

/** Perfil tal como lo devuelve el servicio. [estado] es PENDIENTE, APROBADO, RECHAZADO o DESACTIVADO. */
data class UsuarioDto(
    val id: Long,
    val email: String,
    val nombre: String,
    val rol: String,
    val estado: String,
    val telefono: String? = null,
    val institucion: String? = null,
    val motivoRechazo: String? = null,
    val notifReservas: Boolean = true,
    val notifAlertas: Boolean = true,
    val creadoEn: String = ""
)

data class RegistroRequestDto(
    val email: String,
    val password: String,
    val nombre: String,
    val rol: String,
    val telefono: String? = null,
    val institucion: String? = null
)

data class ActualizarRequestDto(
    val nombre: String,
    val telefono: String? = null,
    val institucion: String? = null,
    val notifReservas: Boolean? = null,
    val notifAlertas: Boolean? = null
)

data class CambiarPasswordDto(val actual: String, val nueva: String)

data class RechazoDto(val motivo: String)

data class SesionDto(val id: Long, val fecha: String)

/** Rutas del microservicio de usuarios (puerto 8081). Los errores llegan como {"error": "mensaje"}. */
interface UsuariosApi {
    @POST("api/usuarios/login")
    suspend fun login(@Body body: LoginRequestDto): LoginResponseDto

    /** Registro público: el perfil queda PENDIENTE hasta que el administrador lo apruebe. */
    @POST("api/usuarios/registro")
    suspend fun registrar(@Body body: RegistroRequestDto): UsuarioDto

    /** "Crear usuario" del administrador: el perfil nace APROBADO. */
    @POST("api/usuarios")
    suspend fun crear(@Body body: RegistroRequestDto): UsuarioDto

    @GET("api/usuarios")
    suspend fun getUsuarios(
        @Query("estado") estado: String? = null,
        @Query("rol") rol: String? = null
    ): List<UsuarioDto>

    /** Perfil de quien tiene ese correo: la app lo usa con el correo de la sesión. */
    @GET("api/usuarios/por-correo")
    suspend fun getPorCorreo(@Query("email") email: String): UsuarioDto

    @GET("api/usuarios/{id}")
    suspend fun getUsuario(@Path("id") id: Long): UsuarioDto

    @PUT("api/usuarios/{id}")
    suspend fun actualizar(@Path("id") id: Long, @Body body: ActualizarRequestDto): UsuarioDto

    @PATCH("api/usuarios/{id}/password")
    suspend fun cambiarPassword(@Path("id") id: Long, @Body body: CambiarPasswordDto)

    @PATCH("api/usuarios/{id}/aprobar")
    suspend fun aprobar(@Path("id") id: Long): UsuarioDto

    @PATCH("api/usuarios/{id}/rechazar")
    suspend fun rechazar(@Path("id") id: Long, @Body body: RechazoDto): UsuarioDto

    @PATCH("api/usuarios/{id}/desactivar")
    suspend fun desactivar(@Path("id") id: Long): UsuarioDto

    @PATCH("api/usuarios/{id}/activar")
    suspend fun activar(@Path("id") id: Long): UsuarioDto

    @GET("api/usuarios/{id}/sesiones")
    suspend fun getSesiones(@Path("id") id: Long): List<SesionDto>

    @DELETE("api/usuarios/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}
