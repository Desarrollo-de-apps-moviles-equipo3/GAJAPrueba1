package com.example.gaja_prueba1.model

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminSpacesRepositoryTest {

    private fun space(id: String, category: String = "Deportes", available: Boolean = true) = Space(
        id = id, name = id, category = category, description = "", capacity = 10,
        available = available, colorHex = "#000000"
    )

    private fun repo() = InMemoryAdminSpacesRepository(
        listOf(space("futbol-a"), space("gimnasio-1", category = "Recintos"))
    )

    @Test
    fun crear_genera_el_id_desde_el_nombre_sin_tildes() = runBlocking {
        val created = repo().create(space("").copy(name = "Cancha de Fútbol C"))
        assertEquals("cancha-de-futbol-c", created.id)
    }

    @Test
    fun crear_con_nombre_repetido_agrega_un_numero_al_id() = runBlocking {
        val repo = repo()
        val first = repo.create(space("").copy(name = "Piscina"))
        val second = repo.create(space("").copy(name = "Piscina"))
        assertEquals("piscina", first.id)
        assertEquals("piscina-2", second.id)
    }

    @Test
    fun deshabilitar_y_habilitar_cambia_available() = runBlocking {
        val repo = repo()
        assertFalse(repo.setAvailable("futbol-a", false).available)
        assertTrue(repo.setAvailable("futbol-a", true).available)
    }

    @Test
    fun editar_reemplaza_los_datos() = runBlocking {
        val repo = repo()
        repo.update(space("futbol-a").copy(capacity = 22))
        assertEquals(22, repo.getAll().first { it.id == "futbol-a" }.capacity)
    }

    @Test
    fun eliminar_quita_el_espacio() = runBlocking {
        val repo = repo()
        repo.delete("futbol-a")
        assertEquals(listOf("gimnasio-1"), repo.getAll().map { it.id })
    }

    @Test(expected = IllegalArgumentException::class)
    fun eliminar_un_espacio_que_no_existe_falla() {
        runBlocking { repo().delete("no-existe") }
    }

    @Test
    fun las_categorias_salen_sin_repetir_y_ordenadas() = runBlocking {
        assertEquals(listOf("Deportes", "Recintos"), repo().categories())
    }
}
