package com.example.gaja_prueba1.data.remote

import com.example.gaja_prueba1.model.Space
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/** Cuerpo para crear o editar un espacio. Si [id] va null, el servicio lo genera desde el nombre. */
data class EspacioRequestDto(
    val id: String? = null,
    val name: String,
    val category: String,
    val description: String,
    val capacity: Int,
    val available: Boolean,
    val colorHex: String,
    val tags: List<String>,
    val latitude: Double?,
    val longitude: Double?
)

data class DisponibilidadDto(val available: Boolean)

data class CategoriaDto(val id: Long? = null, val name: String)

/**
 * Rutas del microservicio de espacios (repo GAJA-microservicios, carpeta espacios-service).
 * El JSON de un espacio tiene los mismos campos que [Space], por eso se lee directo.
 */
interface EspaciosApi {
    @GET("api/espacios")
    suspend fun getEspacios(): List<Space>

    @GET("api/categorias")
    suspend fun getCategorias(): List<CategoriaDto>

    @POST("api/espacios")
    suspend fun crear(@Body body: EspacioRequestDto): Space

    @PUT("api/espacios/{id}")
    suspend fun actualizar(@Path("id") id: String, @Body body: EspacioRequestDto): Space

    @PATCH("api/espacios/{id}/disponibilidad")
    suspend fun cambiarDisponibilidad(@Path("id") id: String, @Body body: DisponibilidadDto): Space

    @DELETE("api/espacios/{id}")
    suspend fun eliminar(@Path("id") id: String)
}
