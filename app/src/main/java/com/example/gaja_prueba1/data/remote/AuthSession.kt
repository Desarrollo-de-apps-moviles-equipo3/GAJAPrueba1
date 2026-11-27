package com.example.gaja_prueba1.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Guarda el token que entrega el login y avisa cuando el servidor lo rechaza (401).
 * El token vive en SharedPreferences (solo esta app puede leerlo) y se borra al cerrar sesión.
 */
object AuthSession {

    private const val PREFS = "gaja_auth"
    private const val KEY_TOKEN = "token"

    private var prefs: SharedPreferences? = null

    @Volatile
    private var cachedToken: String? = null

    private val _expired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Emite cuando un servicio responde 401 con un token puesto: la sesión venció o ya no es válida. */
    val expired: SharedFlow<Unit> = _expired.asSharedFlow()

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        cachedToken = prefs?.getString(KEY_TOKEN, null)
    }

    fun token(): String? = cachedToken

    fun save(token: String) {
        cachedToken = token
        prefs?.edit()?.putString(KEY_TOKEN, token)?.apply()
    }

    fun clear() {
        cachedToken = null
        prefs?.edit()?.remove(KEY_TOKEN)?.apply()
    }

    /** Borra el token y avisa a la pantalla principal para que vuelva al login. */
    fun notifyExpired() {
        clear()
        _expired.tryEmit(Unit)
    }
}
