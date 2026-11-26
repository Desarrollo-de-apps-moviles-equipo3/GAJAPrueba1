package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.CrearIncidenciaDto
import com.example.gaja_prueba1.data.remote.IncidenciaDto
import com.example.gaja_prueba1.data.remote.IncidenciasApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Datos que el panel de administración necesita para mostrar y gestionar una incidencia. */
data class AdminIncidencia(
    val id: Long,
    val email: String,
    val categoria: String,
    val descripcion: String,
    val estado: String,
    val creadaEn: String
) {
    val resuelta: Boolean get() = estado.equals("RESUELTA", ignoreCase = true)
}

interface AdminIncidenciasRepository {
    suspend fun listar(): List<AdminIncidencia>
    suspend fun crear(email: String, categoria: String, descripcion: String): AdminIncidencia
    suspend fun resolver(id: Long): AdminIncidencia
}

/** Implementación remota: Room no se usa porque el microservicio es la fuente persistente. */
class RemoteAdminIncidenciasRepository(
    private val api: IncidenciasApi = ApiClient.create(
        ApiConfig.RESERVAS_URL,
        IncidenciasApi::class.java
    )
) : AdminIncidenciasRepository {

    override suspend fun listar(): List<AdminIncidencia> = withContext(Dispatchers.IO) {
        api.listarTodas().map(IncidenciaDto::toAdminIncidencia)
    }

    override suspend fun crear(
        email: String,
        categoria: String,
        descripcion: String
    ): AdminIncidencia = withContext(Dispatchers.IO) {
        api.crear(
            CrearIncidenciaDto(
                usuarioEmail = email.trim(),
                espacio = categoria.trim(),
                descripcion = descripcion.trim()
            )
        ).toAdminIncidencia()
    }

    override suspend fun resolver(id: Long): AdminIncidencia = withContext(Dispatchers.IO) {
        api.resolver(id).toAdminIncidencia()
    }
}

private fun IncidenciaDto.toAdminIncidencia() = AdminIncidencia(
    id = id,
    email = usuarioEmail,
    categoria = espacio,
    descripcion = descripcion,
    estado = estado,
    creadaEn = creadaEn
)
