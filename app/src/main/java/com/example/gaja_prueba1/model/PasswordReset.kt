package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.RecuperarRequestDto
import com.example.gaja_prueba1.data.remote.RestablecerRequestDto
import com.example.gaja_prueba1.data.remote.SeguridadApi
import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/** Paso 1: pedir el código por correo. Usa el servicio de usuarios. */
class RemoteAccessRecoveryRepository(
    private val api: SeguridadApi = ApiClient.create(ApiConfig.USUARIOS_URL, SeguridadApi::class.java)
) : AccessRecoveryRepository {

    override suspend fun requestRecovery(identifier: String): RecoverAccessResult =
        withContext(Dispatchers.IO) {
            val value = identifier.trim().lowercase()
            // El servicio busca por correo. Si escribió solo un usuario, se responde igual que si no existiera.
            if (!value.contains("@")) return@withContext RecoverAccessResult.Success
            try {
                api.recuperar(RecuperarRequestDto(value))
                RecoverAccessResult.Success
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                RecoverAccessResult.NoConnection
            } catch (exception: HttpException) {
                // Misma respuesta neutra: no se le dice a nadie si el correo existe o si hay un límite.
                RecoverAccessResult.Success
            }
        }
}

sealed class ResetPasswordResult {
    data object Success : ResetPasswordResult()
    data class Error(val message: String) : ResetPasswordResult()
}

object ResetPasswordValidator {
    const val MIN_PASSWORD = 5
    const val MAX_PASSWORD = 60

    fun validate(email: String, code: String, password: String, confirm: String): String? {
        val mail = email.trim()
        return when {
            mail.isEmpty() || !mail.contains("@") -> "Ingresa el correo con el que pediste el código"
            code.trim().isEmpty() -> "Ingresa el código que llegó a tu correo"
            password.length < MIN_PASSWORD -> "La contraseña debe tener al menos $MIN_PASSWORD caracteres"
            password.length > MAX_PASSWORD -> "La contraseña no puede superar $MAX_PASSWORD caracteres"
            password != confirm -> "Las contraseñas no coinciden"
            else -> null
        }
    }
}

interface PasswordResetRepository {
    suspend fun reset(email: String, code: String, newPassword: String): ResetPasswordResult
}

/** Paso 2: cambiar la contraseña con el código recibido. */
class RemotePasswordResetRepository(
    private val api: SeguridadApi = ApiClient.create(ApiConfig.USUARIOS_URL, SeguridadApi::class.java)
) : PasswordResetRepository {

    override suspend fun reset(email: String, code: String, newPassword: String): ResetPasswordResult =
        withContext(Dispatchers.IO) {
            try {
                api.restablecer(RestablecerRequestDto(email.trim().lowercase(), code.trim(), newPassword))
                ResetPasswordResult.Success
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: HttpException) {
                ResetPasswordResult.Error(
                    exception.errorFromServer() ?: "No pudimos cambiar la contraseña (error ${exception.code()})"
                )
            } catch (exception: IOException) {
                ResetPasswordResult.Error("Sin conexión. Revisa tu internet e inténtalo de nuevo.")
            }
        }
}

/** El servicio responde los errores como {"error": "mensaje"}. */
private fun HttpException.errorFromServer(): String? {
    return try {
        val raw = response()?.errorBody()?.string() ?: return null
        JsonParser.parseString(raw).asJsonObject.get("error")?.asString
    } catch (e: Exception) {
        null
    }
}
