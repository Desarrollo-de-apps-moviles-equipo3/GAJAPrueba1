package com.example.gaja_prueba1.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Rutas de categorías del microservicio de espacios (mismo servicio, puerto 8082).
 * Usa [CategoriaDto], que ya está definido en EspaciosApi.kt.
 */
interface CategoriasApi {
    @GET("api/categorias")
    suspend fun getCategorias(): List<CategoriaDto>

    @POST("api/categorias")
    suspend fun crear(@Body body: CategoriaDto): CategoriaDto

    @PUT("api/categorias/{id}")
    suspend fun renombrar(@Path("id") id: Long, @Body body: CategoriaDto): CategoriaDto

    @DELETE("api/categorias/{id}")
    suspend fun eliminar(@Path("id") id: Long)
}
