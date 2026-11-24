package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.DisponibilidadDto
import com.example.gaja_prueba1.data.remote.EspacioRequestDto
import com.example.gaja_prueba1.data.remote.EspaciosApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private fun defaultEspaciosApi(): EspaciosApi =
    ApiClient.create(ApiConfig.ESPACIOS_URL, EspaciosApi::class.java)

/** Lo que ve el usuario en el Inicio: los espacios vienen del microservicio, no de una lista fija. */
class RemoteSpacesRepository(
    private val api: EspaciosApi = defaultEspaciosApi()
) : SpacesRepository {

    override suspend fun getSpaces(): List<Space> = withContext(Dispatchers.IO) {
        api.getEspacios()
    }
}

/** Lo que hace el administrador en "Espacios": crear, editar, deshabilitar y eliminar en el servicio. */
class RemoteAdminSpacesRepository(
    private val api: EspaciosApi = defaultEspaciosApi()
) : AdminSpacesRepository {

    override suspend fun getAll(): List<Space> = withContext(Dispatchers.IO) { api.getEspacios() }

    override suspend fun categories(): List<String> = withContext(Dispatchers.IO) {
        api.getCategorias().map { it.name }
    }

    override suspend fun create(space: Space): Space = withContext(Dispatchers.IO) {
        api.crear(space.toRequest(withId = false))
    }

    override suspend fun update(space: Space): Space = withContext(Dispatchers.IO) {
        api.actualizar(space.id, space.toRequest(withId = true))
    }

    override suspend fun setAvailable(id: String, available: Boolean): Space = withContext(Dispatchers.IO) {
        api.cambiarDisponibilidad(id, DisponibilidadDto(available))
    }

    override suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        api.eliminar(id)
    }
}

internal fun Space.toRequest(withId: Boolean) = EspacioRequestDto(
    id = if (withId) id else null,
    name = name,
    category = category,
    description = description,
    capacity = capacity,
    available = available,
    colorHex = colorHex,
    tags = tags,
    latitude = latitude,
    longitude = longitude
)
