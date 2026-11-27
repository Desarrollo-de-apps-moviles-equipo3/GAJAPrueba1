package com.example.gaja_prueba1.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

data class RenovarResponseDto(val token: String)

data class RecuperarRequestDto(val email: String)

data class RestablecerRequestDto(val email: String, val codigo: String, val nueva: String)

/**
 * Rutas de seguridad del microservicio de usuarios: cierre de sesión, renovación del token y recuperar contraseña.
 * Van en su propia interfaz para no mezclarse con UsuariosApi. Los errores llegan como {"error": "mensaje"}.
 */
interface SeguridadApi {

    /** Cierra la sesión actual en el servidor (el token que lleva la petición deja de servir). */
    @POST("api/usuarios/logout")
    suspend fun logout()

    /** Token nuevo para la misma sesión. */
    @POST("api/usuarios/renovar")
    suspend fun renovar(): RenovarResponseDto

    /** Pide el código de recuperación por correo. Responde igual exista o no el correo. */
    @POST("api/usuarios/recuperar")
    suspend fun recuperar(@Body body: RecuperarRequestDto)

    /** Cambia la contraseña con el código recibido por correo. */
    @POST("api/usuarios/restablecer")
    suspend fun restablecer(@Body body: RestablecerRequestDto)
}
