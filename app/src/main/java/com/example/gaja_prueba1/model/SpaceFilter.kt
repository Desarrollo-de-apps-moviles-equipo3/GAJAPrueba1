package com.example.gaja_prueba1.model

import java.text.Normalizer

fun normalizeText(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
        .trim()

fun filterSpaces(spaces: List<Space>, query: String): List<Space> {
    val q = normalizeText(query)
    if (q.isEmpty()) return spaces
    return spaces.filter { space ->
        normalizeText(space.name).contains(q) ||
                normalizeText(space.category).contains(q) ||
                space.tags.any { normalizeText(it).contains(q) }
    }
}