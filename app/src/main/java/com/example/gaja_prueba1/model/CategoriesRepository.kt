package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.CategoriaDto
import com.example.gaja_prueba1.data.remote.CategoriasApi
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

data class Category(val id: Long, val name: String)

/** Error de negocio con un mensaje listo para mostrarle al administrador. */
class CategoryException(message: String) : Exception(message)

interface CategoriesRepository {
    suspend fun getAll(): List<Category>
    suspend fun create(name: String): Category
    suspend fun rename(id: Long, name: String): Category
    suspend fun delete(id: Long)
}

private fun defaultCategoriasApi(): CategoriasApi =
    ApiClient.create(ApiConfig.ESPACIOS_URL, CategoriasApi::class.java)

/**
 * Categorías guardadas en el microservicio de espacios.
 * Si el servicio responde un error (por ejemplo 409 al borrar una categoría con espacios),
 * lo convertimos en [CategoryException] con el mensaje que mandó el servicio.
 */
class RemoteCategoriesRepository(
    private val api: CategoriasApi = defaultCategoriasApi()
) : CategoriesRepository {

    override suspend fun getAll(): List<Category> =
        call { api.getCategorias().map { it.toCategory() } }

    override suspend fun create(name: String): Category =
        call { api.crear(CategoriaDto(name = name)).toCategory() }

    override suspend fun rename(id: Long, name: String): Category =
        call { api.renombrar(id, CategoriaDto(name = name)).toCategory() }

    override suspend fun delete(id: Long) = call { api.eliminar(id) }

    private suspend fun <T> call(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: HttpException) {
            throw CategoryException(e.serverMessage() ?: "El servicio respondió con error ${e.code()}")
        }
    }
}

private fun CategoriaDto.toCategory() = Category(id = id ?: 0L, name = name)

/** El servicio responde los errores como {"error": "mensaje"}. */
private fun HttpException.serverMessage(): String? {
    return try {
        val raw = response()?.errorBody()?.string() ?: return null
        JsonParser.parseString(raw).asJsonObject.get("error")?.asString
    } catch (e: Exception) {
        null
    }
}
