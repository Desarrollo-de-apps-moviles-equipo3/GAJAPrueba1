package com.example.gaja_prueba1.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query

data class ClimaResponse(val current: ClimaActual?)

data class ClimaActual(
    @SerializedName("temperature_2m") val temperature: Double?,
    @SerializedName("weather_code") val weatherCode: Int?,
    @SerializedName("wind_speed_10m") val windSpeed: Double?
)

/**
 * API externa Open-Meteo (no pide clave).
 * Ejemplo: https://api.open-meteo.com/v1/forecast?latitude=-33.40&longitude=-70.58&current=temperature_2m,weather_code,wind_speed_10m
 */
interface ClimaApi {
    @GET("v1/forecast")
    suspend fun actual(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String,
        @Query("timezone") timezone: String
    ): ClimaResponse
}
