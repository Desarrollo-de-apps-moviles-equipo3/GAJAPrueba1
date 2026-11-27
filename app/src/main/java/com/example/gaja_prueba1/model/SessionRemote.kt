package com.example.gaja_prueba1.model

import android.util.Log
import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.AuthSession
import com.example.gaja_prueba1.data.remote.SeguridadApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Lo que la app le pide al servidor sobre la sesión: cerrarla y renovar el token.
 * Ninguna de las dos debe romper la app: si no hay conexión, la sesión local se maneja igual.
 */
object SessionRemote {

    private val api: SeguridadApi by lazy {
        ApiClient.create(ApiConfig.USUARIOS_URL, SeguridadApi::class.java)
    }

    /** Avisa al servidor que se cerró la sesión. Si no puede, igual se cierra en el teléfono. */
    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            api.logout()
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.w("GAJA_SESSION", "No se pudo cerrar la sesión en el servidor", exception)
        }
    }

    /**
     * Pide un token nuevo y lo guarda, así la sesión se alarga mientras la persona use la app.
     * Si el servidor dice que la sesión ya no sirve (401), el interceptor la cierra solo.
     */
    suspend fun renew() = withContext(Dispatchers.IO) {
        try {
            AuthSession.save(api.renovar().token)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Log.w("GAJA_SESSION", "No se pudo renovar el token", exception)
        }
    }
}
