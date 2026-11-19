package com.example.gaja_prueba1.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET

/** Llamada mínima para comprobar que la app llega a un servicio. */
interface PingApi {
    @GET("/")
    suspend fun ping(): Response<ResponseBody>
}

object ConnectionCheck {

    /** Escribe en Logcat (filtro "GAJA_API") si el servicio respondió. */
    suspend fun ping(baseUrl: String = ApiConfig.USUARIOS_URL) {
        withContext(Dispatchers.IO) {
            try {
                val api = ApiClient.create(baseUrl, PingApi::class.java)
                val response = api.ping()
                Log.d("GAJA_API", "Respondió $baseUrl -> código ${response.code()}")
            } catch (exception: Exception) {
                Log.e("GAJA_API", "No se pudo conectar a $baseUrl", exception)
            }
        }
    }
}