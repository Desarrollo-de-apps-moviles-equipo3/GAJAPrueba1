package com.example.gaja_prueba1.model

import java.text.Normalizer

interface AdminSpacesRepository {
    suspend fun getAll(): List<Space>
    suspend fun categories(): List<String>
    suspend fun create(space: Space): Space
    suspend fun update(space: Space): Space
    suspend fun setAvailable(id: String, available: Boolean): Space
    suspend fun delete(id: String)
}

class InMemoryAdminSpacesRepository(initial: List<Space>) : AdminSpacesRepository {
    private val spaces = initial.toMutableList()

    override suspend fun getAll(): List<Space> = spaces.toList()

    override suspend fun categories(): List<String> =
        spaces.map { it.category }.distinct().sorted()

    override suspend fun create(space: Space): Space {
        val base = slugOf(space.name)
        var id = base
        var n = 2
        while (spaces.any { it.id == id }) id = "$base-${n++}"
        return space.copy(id = id).also { spaces.add(it) }
    }

    override suspend fun update(space: Space): Space {
        val index = indexOf(space.id)
        spaces[index] = space
        return space
    }

    override suspend fun setAvailable(id: String, available: Boolean): Space {
        val index = indexOf(id)
        return spaces[index].copy(available = available).also { spaces[index] = it }
    }

    override suspend fun delete(id: String) {
        spaces.removeAt(indexOf(id))
    }

    private fun indexOf(id: String): Int =
        spaces.indexOfFirst { it.id == id }.also { require(it >= 0) { "No existe el espacio $id" } }

    private fun slugOf(name: String): String =
        Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace(Regex("\\p{M}"), "")
            .lowercase()
            .trim()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifEmpty { "espacio" }
}
