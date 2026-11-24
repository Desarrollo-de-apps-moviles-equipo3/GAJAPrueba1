package com.example.gaja_prueba1.validation

object CategoryValidator {
    const val MAX_NAME_LENGTH = 40

    /**
     * @param existing nombres que ya existen
     * @param editing nombre actual de la categoría que se está editando (null si es nueva),
     *        para que no cuente como duplicado de sí misma.
     */
    fun validateName(name: String, existing: List<String>, editing: String? = null): String? {
        val clean = name.trim()
        return when {
            clean.isEmpty() -> "Escribe el nombre de la categoría"
            clean.length > MAX_NAME_LENGTH -> "Máximo $MAX_NAME_LENGTH caracteres"
            existing.any { it.equals(clean, ignoreCase = true) && !it.equals(editing, ignoreCase = true) } ->
                "Ya existe una categoría con ese nombre"
            else -> null
        }
    }
}
