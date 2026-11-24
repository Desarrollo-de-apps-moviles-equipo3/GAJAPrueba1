package com.example.gaja_prueba1.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ReservasApi {

    @POST("api/reservas")
    suspend fun crearReserva(
        @Body request: CreateReservationRequest
    ): ReservationResponse

    @GET("api/reservas/usuario")
    suspend fun listarPorUsuario(
        @Query("email") email: String
    ): List<ReservationResponse>

    @GET("api/reservas/disponibilidad")
    suspend fun horariosOcupados(
        @Query("fecha") fecha: String,
        @Query("espacio") espacio: String
    ): List<String>

    @PATCH("api/reservas/{id}/cancelar")
    suspend fun cancelarReserva(
        @Path("id") id: Long,
        @Query("email") email: String
    ): ReservationResponse
}