package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.CreateReservationRequest
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.data.remote.ReservasApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun defaultReservasApi(): ReservasApi =
    ApiClient.create(
        ApiConfig.RESERVAS_URL,
        ReservasApi::class.java
    )

class RemoteReservationsRepository(
    private val api: ReservasApi = defaultReservasApi()
) {
    suspend fun crearReserva(
        request: CreateReservationRequest
    ): ReservationResponse = withContext(Dispatchers.IO) {
        api.crearReserva(request)
    }

    suspend fun listarPorUsuario(
        email: String
    ): List<ReservationResponse> = withContext(Dispatchers.IO) {
        api.listarPorUsuario(email)
    }

    suspend fun reglas(): ReservationRules = withContext(Dispatchers.IO) {
        api.obtenerReglas().let {
            ReservationRules(
                maxDaysAhead = it.maxDiasAdelante,
                cancellationLimitHours = it.horasLimiteCancelacion,
                openingHour = it.horaApertura,
                closingHour = it.horaCierre
            )
        }
    }

    suspend fun horariosOcupados(
        fecha: String,
        espacio: String
    ): Set<String> = withContext(Dispatchers.IO) {
        api.horariosOcupados(fecha, espacio).toSet()
    }

    suspend fun cancelarReserva(
        id: Long,
        email: String
    ): ReservationResponse =
        withContext(Dispatchers.IO) {
            api.cancelarReserva(id, email)
        }

    suspend fun ocultarParaUsuario(id: Long, email: String): ReservationResponse =
        withContext(Dispatchers.IO) {
            api.ocultarParaUsuario(id, email)
        }
}
