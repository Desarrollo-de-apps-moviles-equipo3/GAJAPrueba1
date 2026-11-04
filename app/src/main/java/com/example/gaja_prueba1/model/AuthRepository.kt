package com.example.gaja_prueba1.model

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class AuthRepository {

    private data class TestUser(
        val email: String,
        val password: String,
        val role: String
    )

    private val testUsers = listOf(
        TestUser("cadete@gaja.cl", "12345", "Cadete"),
        TestUser("oficial@gaja.cl", "12345", "Oficial"),
        TestUser(
            "oficial.semana@gaja.cl",
            "12345",
            "Oficial de semana"
        ),
        TestUser(
            "externo@gaja.cl",
            "12345",
            "Institución externa"
        )
    )

    suspend fun loginUser(
        email: String,
        password: String,
        selectedRole: String
    ): LoginResult = withContext(Dispatchers.IO) {

        try {
            delay(800)

            val normalizedEmail = email.trim().lowercase()

            // Se conserva la cuenta del profesor.
            if (
                normalizedEmail == "profe_exe@duoc.cl" &&
                password == "12345"
            ) {
                return@withContext LoginResult.Success(
                    token = "demo-token-profesor",
                    userId = normalizedEmail,
                    role = selectedRole
                )
            }

            val user = testUsers.firstOrNull {
                it.email == normalizedEmail &&
                        it.password == password &&
                        it.role == selectedRole
            }

            if (user != null) {
                LoginResult.Success(
                    token = "demo-token-${user.role.lowercase()}",
                    userId = user.email,
                    role = user.role
                )
            } else {
                LoginResult.Error(
                    Exception("Credenciales o rol incorrectos")
                )
            }

        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoginResult.Error(exception)
        }
    }
}