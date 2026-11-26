package com.example.gaja_prueba1.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

/** Respuesta de los contadores del servicio: {"cantidad": 2}. */
data class CantidadDto(val cantidad: Long)

/** Cuerpo para crear una incidencia o solicitud de ayuda. [espacio] guarda la categoría o el lugar. */
data class CrearIncidenciaDto(val usuarioEmail: String, val espacio: String, val descripcion: String)

/** [estado] es PENDIENTE o RESUELTA; [creadaEn] llega como "2026-11-24T19:30:00". */
data class IncidenciaDto(
    val id: Long,
    val usuarioEmail: String = "",
    val espacio: String = "",
    val descripcion: String = "",
    val estado: String = "PENDIENTE",
    val creadaEn: String = ""
)

/** Rutas de incidencias del microservicio de reservas (ya existen en reservas-service). */
interface IncidenciasApi {
    @GET("api/incidencias/pendientes/count")
    suspend fun contarPendientes(): CantidadDto

    @POST("api/incidencias")
    suspend fun crear(@Body body: CrearIncidenciaDto): IncidenciaDto

    @GET("api/incidencias/usuario")
    suspend fun getPorUsuario(@Query("email") email: String): List<IncidenciaDto>

    /** Lista incidencias para el panel de administración. */
    @GET("api/incidencias")
    suspend fun listarTodas(): List<IncidenciaDto>

    /** Marca una incidencia como resuelta en el servidor. */
    @PATCH("api/incidencias/{id}/resolver")
    suspend fun resolver(@Path("id") id: Long): IncidenciaDto
}
