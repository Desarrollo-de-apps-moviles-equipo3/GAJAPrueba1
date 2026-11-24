package com.example.gaja_prueba1.data.remote

import retrofit2.http.GET

/** Respuesta de los contadores del servicio: {"cantidad": 2}. */
data class CantidadDto(val cantidad: Long)

/** Rutas de incidencias del microservicio de reservas (ya existen en reservas-service). */
interface  IncidenciasApi {
    @GET("api/incidencias/pendientes/count")
    suspend fun contarPendientes(): CantidadDto
}