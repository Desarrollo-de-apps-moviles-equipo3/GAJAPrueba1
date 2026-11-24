package com.example.gaja_prueba1.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

data class AlertResponse(
    val id: Long,
    val usuarioEmail: String,
    val reservaId: Long?,
    val mensaje: String,
    val leida: Boolean,
    val creadaEn: String
)

interface AlertasApi {
    @GET("api/alertas")
    suspend fun listarPorUsuario(
        @Query("email") email: String
    ): List<AlertResponse>
}
