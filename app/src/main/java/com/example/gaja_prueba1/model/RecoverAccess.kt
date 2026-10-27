package com.example.gaja_prueba1.model

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object RecoverAccessValidator {
    private val emailRegex = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun validate(identifier: String): String? {
        val value = identifier.trim()
        return when {
            value.isEmpty() -> "Ingresa tu usuario o correo institucional"
            value.any { it.isWhitespace() } -> "No puede contener espacios"
            value.contains("@") && !emailRegex.matches(value) -> "Correo no válido"
            !value.contains("@") && value.length < 3 -> "Debe tener al menos 3 caracteres"
            else -> null
        }
    }
}

sealed class RecoverAccessResult {
    data object Success : RecoverAccessResult()
    data object NoConnection : RecoverAccessResult()
}

interface AccessRecoveryRepository {
    suspend fun requestRecovery(identifier: String): RecoverAccessResult
}

class FakeAccessRecoveryRepository(
    private val delayMillis: Long = 1200
) : AccessRecoveryRepository {

    override suspend fun requestRecovery(identifier: String): RecoverAccessResult =
        withContext(Dispatchers.IO) {
            delay(delayMillis)
            if (identifier.trim().equals("sinconexion", ignoreCase = true)) {
                RecoverAccessResult.NoConnection
            } else {
                RecoverAccessResult.Success
            }
        }
}