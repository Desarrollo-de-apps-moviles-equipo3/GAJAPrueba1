package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.data.remote.BloqueoRequestDto
import com.example.gaja_prueba1.data.remote.BloqueosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BlocksRepository {
    suspend fun getAll(): List<BloqueoDto>
    suspend fun create(spaceId: String, date: String, timeSlot: String, reason: String?): BloqueoDto
    suspend fun delete(id: Long)
}

/** Los bloqueos viven en el servicio de espacios; el calendario los pide todos y los filtra en la app. */
class RemoteBlocksRepository(
    private val api: BloqueosApi = ApiClient.create(ApiConfig.ESPACIOS_URL, BloqueosApi::class.java)
) : BlocksRepository {
    override suspend fun getAll(): List<BloqueoDto> = withContext(Dispatchers.IO) { api.getBloqueos() }

    override suspend fun create(spaceId: String, date: String, timeSlot: String, reason: String?): BloqueoDto =
        withContext(Dispatchers.IO) { api.crear(spaceId, BloqueoRequestDto(date, timeSlot, reason)) }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) { api.eliminar(id) }
}
