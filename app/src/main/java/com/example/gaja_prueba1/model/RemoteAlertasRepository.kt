package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.AlertasApi
import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.AlertResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun defaultAlertasApi(): AlertasApi =
    ApiClient.create(ApiConfig.RESERVAS_URL, AlertasApi::class.java)

class RemoteAlertasRepository(
    private val api: AlertasApi = defaultAlertasApi()
) {
    suspend fun listarPorUsuario(email: String): List<AlertResponse> =
        withContext(Dispatchers.IO) {
            api.listarPorUsuario(email)
        }
}
