package com.example.gaja_prueba1.validation

/** Reglas de las pantallas "Cambiar contraseña" y "Nueva solicitud". Devuelven el mensaje de error o null. */
object ProfileValidator {
    const val MAX_REQUEST_LENGTH = 1000

    fun validateCurrentPassword(current: String): String? =
        if (current.isEmpty()) "Escribe tu contraseña actual" else null

    fun validateNewPassword(new: String, current: String): String? = when {
        new.length < UserValidator.MIN_PASSWORD_LENGTH ->
            "Mínimo ${UserValidator.MIN_PASSWORD_LENGTH} caracteres"
        new.length > UserValidator.MAX_PASSWORD_LENGTH ->
            "Máximo ${UserValidator.MAX_PASSWORD_LENGTH} caracteres"
        new == current -> "La nueva contraseña debe ser distinta de la actual"
        else -> null
    }

    fun validateConfirmation(new: String, confirmation: String): String? =
        if (new != confirmation) "Las contraseñas no coinciden" else null

    fun validateRequestCategory(category: String): String? =
        if (category.isBlank()) "Elige un tema" else null

    fun validateRequestDescription(description: String): String? = when {
        description.isBlank() -> "Cuéntanos qué necesitas"
        description.trim().length > MAX_REQUEST_LENGTH -> "Máximo $MAX_REQUEST_LENGTH caracteres"
        else -> null
    }
}
