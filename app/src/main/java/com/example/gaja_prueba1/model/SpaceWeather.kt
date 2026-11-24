package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.ClimaApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.math.roundToInt

/** Clima actual de un recinto. */
data class SpaceWeather(
    val temperatureC: Int,
    val description: String,
    val windKmh: Int?
) {
    /** Texto corto para la tarjeta, por ejemplo "22° · Despejado". */
    val summary: String get() = "$temperatureC° · $description"
}

/** Traduce el código WMO que entrega Open-Meteo a una descripción en español. */
fun weatherDescription(code: Int): String = when (code) {
    0 -> "Despejado"
    1 -> "Mayormente despejado"
    2 -> "Parcialmente nublado"
    3 -> "Nublado"
    45, 48 -> "Niebla"
    in 51..57 -> "Llovizna"
    in 61..67 -> "Lluvia"
    in 71..77 -> "Nieve"
    in 80..82 -> "Chubascos"
    85, 86 -> "Chubascos de nieve"
    in 95..99 -> "Tormenta"
    else -> "Sin datos"
}

interface WeatherRepository {
    /** Devuelve el clima por id de espacio. Los espacios sin coordenadas no aparecen en el mapa. */
    suspend fun weatherFor(spaces: List<Space>): Map<String, SpaceWeather>
}

class RemoteWeatherRepository(
    private val api: ClimaApi = ApiClient.create(ApiConfig.CLIMA_URL, ClimaApi::class.java)
) : WeatherRepository {

    override suspend fun weatherFor(spaces: List<Space>): Map<String, SpaceWeather> =
        withContext(Dispatchers.IO) {
            coroutineScope {
                // Los recintos están a pocos metros: se agrupan por coordenadas redondeadas a 2 decimales
                // (~1 km) para no repetir la misma consulta.
                spaces
                    .filter { it.latitude != null && it.longitude != null }
                    .groupBy { cell(it.latitude!!) to cell(it.longitude!!) }
                    .map { (_, group) ->
                        async {
                            val first = group.first()
                            val weather = fetch(first.latitude!!, first.longitude!!)
                            group.map { it.id to weather }
                        }
                    }
                    .awaitAll()
                    .flatten()
                    .toMap()
            }
        }

    private suspend fun fetch(latitude: Double, longitude: Double): SpaceWeather {
        val current = api.actual(
            latitude = latitude,
            longitude = longitude,
            current = "temperature_2m,weather_code,wind_speed_10m",
            timezone = "auto"
        ).current
        val temperature = current?.temperature ?: throw IOException("El clima llegó sin temperatura")
        return SpaceWeather(
            temperatureC = temperature.roundToInt(),
            description = weatherDescription(current.weatherCode ?: -1),
            windKmh = current.windSpeed?.roundToInt()
        )
    }

    private fun cell(value: Double): Long = (value * 100).roundToInt().toLong()
}
