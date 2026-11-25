package com.example.gaja_prueba1.validation

/** Reglas del formulario "Crear usuario". Cada función devuelve el mensaje de error o null si está bien. */
object UserValidator {
    const val MAX_NAME_LENGTH = 80
    const val MIN_PASSWORD_LENGTH = 5
    const val MAX_PASSWORD_LENGTH = 60
    const val MAX_PHONE_LENGTH = 20
    const val EXTERNAL_ROLE = "Institución externa"

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phoneRegex = Regex("^\\+?[0-9 ]+$")

    fun validateName(name: String): String? = when {
        name.isBlank() -> "Escribe el nombre"
        name.trim().length > MAX_NAME_LENGTH -> "Máximo $MAX_NAME_LENGTH caracteres"
        else -> null
    }

    fun validateEmail(email: String): String? = when {
        email.isBlank() -> "Escribe el correo"
        !emailRegex.matches(email.trim()) -> "Escribe un correo válido"
        else -> null
    }

    fun validatePassword(password: String): String? = when {
        password.length < MIN_PASSWORD_LENGTH -> "Mínimo $MIN_PASSWORD_LENGTH caracteres"
        password.length > MAX_PASSWORD_LENGTH -> "Máximo $MAX_PASSWORD_LENGTH caracteres"
        else -> null
    }

    fun validateRole(role: String): String? = if (role.isBlank()) "Elige un rol" else null

    /** El teléfono es opcional; si se escribe, solo números, espacios y un + al inicio. */
    fun validatePhone(phone: String): String? = when {
        phone.isBlank() -> null
        phone.trim().length > MAX_PHONE_LENGTH -> "Máximo $MAX_PHONE_LENGTH caracteres"
        !phoneRegex.matches(phone.trim()) -> "Solo números, espacios y +"
        else -> null
    }

    /** La institución es obligatoria solo para el rol "Institución externa". */
    fun validateInstitution(institution: String, role: String): String? =
        if (role == EXTERNAL_ROLE && institution.isBlank()) "Escribe la institución" else null

    fun validateRejectReason(reason: String): String? = when {
        reason.isBlank() -> "Escribe el motivo del rechazo"
        reason.trim().length > 200 -> "Máximo 200 caracteres"
        else -> null
    }
}
