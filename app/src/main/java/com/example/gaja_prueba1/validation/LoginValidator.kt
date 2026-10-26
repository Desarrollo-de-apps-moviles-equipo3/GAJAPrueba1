package com.example.gaja_prueba1.validation

object LoginValidator {
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    const val MIN_PASSWORD_LENGTH = 5


    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "El correo es obligatorio"
        !EMAIL_REGEX.matches(email.trim()) -> "Ingresa un correo valido"
        else -> null

    }

    fun validatePassword (password: String): String? = when {
        password.isBlank()  -> "La contraseña es obligatoria"
        password.length < MIN_PASSWORD_LENGTH -> "Debe tener al menos $MIN_PASSWORD_LENGTH caracteres"
        else -> null
    }
}