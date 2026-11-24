package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.CategoriaDto
import com.example.gaja_prueba1.data.remote.DisponibilidadDto
import com.example.gaja_prueba1.data.remote.EspacioRequestDto
import com.example.gaja_prueba1.data.remote.EspaciosApi
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class RemoteSpacesRepositoryTest {

    private fun space(id: String, name: String = id) = Space(
        id = id, name = name, category = "Deportes", description = "d", capacity = 10,
        available = true, colorHex = "#2F6B4F", tags = listOf("Fútbol"), latitude = -33.4, longitude = -70.5
    )

    /** API falsa: guarda lo que le mandan, igual que lo haría el servicio. */
    private class FakeEspaciosApi(var espacios: MutableList<Space> = mutableListOf()) : EspaciosApi {
        var lastRequest: EspacioRequestDto? = null
        var lastId: String? = null

        override suspend fun getEspacios(): List<Space> = espacios.toList()
        override suspend fun getCategorias() = listOf(CategoriaDto(1, "Deportes"), CategoriaDto(2, "Recintos"))

        override suspend fun crear(body: EspacioRequestDto): Space {
            lastRequest = body
            return Space("nuevo", body.name, body.category, body.description, body.capacity,
                body.available, body.colorHex, body.tags, body.latitude, body.longitude)
        }

        override suspend fun actualizar(id: String, body: EspacioRequestDto): Space {
            lastRequest = body
            lastId = id
            return Space(id, body.name, body.category, body.description, body.capacity,
                body.available, body.colorHex, body.tags, body.latitude, body.longitude)
        }

        override suspend fun cambiarDisponibilidad(id: String, body: DisponibilidadDto): Space {
            lastId = id
            return espacios.first { it.id == id }.copy(available = body.available)
        }

        override suspend fun eliminar(id: String) {
            lastId = id
            espacios.removeAll { it.id == id }
        }
    }

    @Test
    fun el_inicio_recibe_los_espacios_del_servicio() = runBlocking {
        val api = FakeEspaciosApi(mutableListOf(space("a"), space("b")))
        assertEquals(listOf("a", "b"), RemoteSpacesRepository(api).getSpaces().map { it.id })
    }

    @Test
    fun crear_no_manda_id_para_que_lo_genere_el_servicio() = runBlocking {
        val api = FakeEspaciosApi()
        val created = RemoteAdminSpacesRepository(api).create(space("", name = "Piscina"))
        assertNull(api.lastRequest!!.id)
        assertEquals("Piscina", api.lastRequest!!.name)
        assertEquals("nuevo", created.id)
    }

    @Test
    fun editar_usa_el_id_del_espacio_en_la_ruta() = runBlocking {
        val api = FakeEspaciosApi()
        RemoteAdminSpacesRepository(api).update(space("futbol-a").copy(capacity = 22))
        assertEquals("futbol-a", api.lastId)
        assertEquals(22, api.lastRequest!!.capacity)
    }

    @Test
    fun deshabilitar_cambia_available_en_el_servicio() = runBlocking {
        val api = FakeEspaciosApi(mutableListOf(space("a")))
        assertFalse(RemoteAdminSpacesRepository(api).setAvailable("a", false).available)
    }

    @Test
    fun eliminar_llama_al_servicio_con_el_id() = runBlocking {
        val api = FakeEspaciosApi(mutableListOf(space("a")))
        RemoteAdminSpacesRepository(api).delete("a")
        assertEquals("a", api.lastId)
        assertEquals(emptyList<Space>(), api.espacios)
    }

    @Test
    fun las_categorias_son_los_nombres_del_servicio() = runBlocking {
        assertEquals(listOf("Deportes", "Recintos"), RemoteAdminSpacesRepository(FakeEspaciosApi()).categories())
    }
}
