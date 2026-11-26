package com.example.gaja_prueba1.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Direcciones de los microservicios (repo GAJA-microservicios).
 *
 * El emulador de Android llega al computador mediante 10.0.2.2.
 * En un celular real hay que usar la IPv4 del computador en la red local.
 *
 * Puertos acordados: usuarios 8081, espacios 8082, reservas 8083.
 */
object ApiConfig {
    private const val HOST = "http://10.0.2.2"

    const val USUARIOS_URL = "$HOST:8081/"
    const val ESPACIOS_URL = "$HOST:8082/"
    const val RESERVAS_URL = "$HOST:8083/"

    // API externa del clima (Open-Meteo): usa HTTPS.
    const val CLIMA_URL = "https://api.open-meteo.com/"
}

/** Crea los clientes Retrofit. Cada servicio usa su propia dirección base. */
object ApiClient {

    private val httpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()
    }

    /** Uso: val api = ApiClient.create(ApiConfig.ESPACIOS_URL, EspaciosApi::class.java) */
    fun <T> create(baseUrl: String, service: Class<T>): T =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(service)
}
