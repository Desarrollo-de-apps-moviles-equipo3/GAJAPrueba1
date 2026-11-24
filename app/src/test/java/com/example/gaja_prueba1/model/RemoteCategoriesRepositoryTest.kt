package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.CategoriaDto
import com.example.gaja_prueba1.data.remote.CategoriasApi
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RemoteCategoriesRepositoryTest {

    private fun httpError(code: Int, body: String) = HttpException(
        Response.error<Any>(code, body.toResponseBody("application/json".toMediaType()))
    )

    /** API falsa: guarda las categorías en memoria y puede simular un error del servicio. */
    private class FakeCategoriasApi : CategoriasApi {
        val items = mutableListOf(CategoriaDto(1, "Deportes"), CategoriaDto(2, "Recintos"))
        var error: HttpException? = null
        var lastSent: CategoriaDto? = null

        override suspend fun getCategorias(): List<CategoriaDto> = items.toList()

        override suspend fun crear(body: CategoriaDto): CategoriaDto {
            error?.let { throw it }
            lastSent = body
            return CategoriaDto(3, body.name).also { items.add(it) }
        }

        override suspend fun renombrar(id: Long, body: CategoriaDto): CategoriaDto {
            error?.let { throw it }
            lastSent = body
            val index = items.indexOfFirst { it.id == id }
            items[index] = CategoriaDto(id, body.name)
            return items[index]
        }

        override suspend fun eliminar(id: Long) {
            error?.let { throw it }
            items.removeAll { it.id == id }
        }
    }

    @Test
    fun getAll_convierte_los_dto_en_categorias() = runBlocking {
        val repo = RemoteCategoriesRepository(FakeCategoriasApi())
        assertEquals(listOf(Category(1, "Deportes"), Category(2, "Recintos")), repo.getAll())
    }

    @Test
    fun crear_manda_solo_el_nombre() = runBlocking {
        val api = FakeCategoriasApi()
        val created = RemoteCategoriesRepository(api).create("Eventos")
        assertEquals(Category(3, "Eventos"), created)
        assertEquals(null, api.lastSent?.id)
        assertEquals("Eventos", api.lastSent?.name)
    }

    @Test
    fun renombrar_cambia_el_nombre() = runBlocking {
        val api = FakeCategoriasApi()
        val renamed = RemoteCategoriesRepository(api).rename(2, "Salones")
        assertEquals(Category(2, "Salones"), renamed)
    }

    @Test
    fun eliminar_quita_la_categoria() = runBlocking {
        val api = FakeCategoriasApi()
        RemoteCategoriesRepository(api).delete(1)
        assertEquals(listOf(2L), api.items.map { it.id })
    }

    @Test
    fun el_error_409_del_servicio_llega_con_su_mensaje() = runBlocking {
        val api = FakeCategoriasApi().apply {
            error = httpError(409, """{"error":"La categoria tiene espacios; muevelos o eliminalos primero"}""")
        }
        try {
            RemoteCategoriesRepository(api).delete(1)
            fail("Debía lanzar CategoryException")
        } catch (e: CategoryException) {
            assertEquals("La categoria tiene espacios; muevelos o eliminalos primero", e.message)
        }
    }

    @Test
    fun si_el_error_no_trae_mensaje_se_muestra_el_codigo() = runBlocking {
        val api = FakeCategoriasApi().apply { error = httpError(500, "no es json") }
        try {
            RemoteCategoriesRepository(api).create("X")
            fail("Debía lanzar CategoryException")
        } catch (e: CategoryException) {
            assertEquals("El servicio respondió con error 500", e.message)
        }
    }
}
