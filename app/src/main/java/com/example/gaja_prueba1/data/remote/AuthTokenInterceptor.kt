package com.example.gaja_prueba1.data.remote

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Agrega "Authorization: Bearer <token>" a las peticiones a NUESTROS servicios.
 * Nunca se envía a APIs externas (como la del clima). Si un servicio responde 401 con token puesto,
 * la sesión ya no sirve: se borra el token y se avisa para volver al login.
 */
class AuthTokenInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val token = AuthSession.token()
        if (token == null || !ApiConfig.isOwnService(original.url.toString())) {
            return chain.proceed(original)
        }

        val response = chain.proceed(
            original.newBuilder().header("Authorization", "Bearer $token").build()
        )
        if (response.code == 401) {
            AuthSession.notifyExpired()
        }
        return response
    }
}
