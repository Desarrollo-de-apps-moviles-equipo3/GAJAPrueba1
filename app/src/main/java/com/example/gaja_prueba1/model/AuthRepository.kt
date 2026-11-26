package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.LoginRequestDto
import com.example.gaja_prueba1.data.remote.UsuariosApi
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Inicio de sesión contra el microservicio de usuarios (puerto 8081).
 * El servicio revisa correo y contraseña, devuelve el rol de la cuenta y también verifica el estado del perfil:
 * uno pendiente, rechazado o desactivado no entra y el servicio explica por qué.
 */
class AuthRepository(
    private val api: UsuariosApi = ApiClient.create(ApiConfig.USUARIOS_URL, UsuariosApi::class.java)
) {

    suspend fun loginUser(
        email: String,
        password: String
    ): LoginResult = withContext(Dispatchers.IO) {

        try {
            val normalizedEmail = email.trim().lowercase()

            val response = api.login(LoginRequestDto(normalizedEmail, password))
            LoginResult.Success(
                token = response.token,
                userId = response.email,
                role = response.rol
            )

        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpException) {
            // 401: datos incorrectos. 403: perfil pendiente, rechazado o desactivado.
            LoginResult.Error(
                Exception(exception.serverMessage() ?: "No pudimos iniciar sesión (error ${exception.code()})")
            )
        } catch (exception: IOException) {
            LoginResult.Error(
                Exception("No pudimos conectar con el servicio de usuarios. Revisa que esté encendido.")
            )
        } catch (exception: Exception) {
            LoginResult.Error(exception)
        }
    }
}

/** El servicio responde los errores como {"error": "mensaje"}. */
private fun HttpException.serverMessage(): String? {
    return try {
        val raw = response()?.errorBody()?.string() ?: return null
        JsonParser.parseString(raw).asJsonObject.get("error")?.asString
    } catch (e: Exception) {
        null
    }
}
