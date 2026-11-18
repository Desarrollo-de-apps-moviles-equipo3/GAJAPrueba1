package com.example.gaja_prueba1.validation

object AdminSpaceValidator {
    const val MAX_NAME_LENGTH = 60
    const val MAX_CAPACITY = 500

    fun validateName(name: String): String? = when {
        name.isBlank() -> "Escribe el nombre del espacio"
        name.trim().length > MAX_NAME_LENGTH -> "Máximo $MAX_NAME_LENGTH caracteres"
        else -> null
    }

    fun validateCategory(category: String): String? =
        if (category.isBlank()) "Elige una categoría" else null

    fun validateCapacity(text: String): String? {
        val value = text.trim().toIntOrNull()
        return when {
            value == null -> "Escribe un número"
            value < 1 -> "La capacidad debe ser al menos 1"
            value > MAX_CAPACITY -> "La capacidad máxima es $MAX_CAPACITY"
            else -> null
        }
    }
}
