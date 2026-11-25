package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ApiClient
import com.example.gaja_prueba1.data.remote.ApiConfig
import com.example.gaja_prueba1.data.remote.CrearIncidenciaDto
import com.example.gaja_prueba1.data.remote.IncidenciaDto
import com.example.gaja_prueba1.data.remote.IncidenciasApi
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/** Una solicitud de ayuda que la persona envió. [resolved] es true cuando el administrador la cerró. */
data class SupportRequest(
    val id: Long,
    val category: String,
    val description: String,
    val resolved: Boolean,
    val date: String
)

/** Error de negocio con un mensaje listo para mostrar. */
class SupportException(message: String) : Exception(message)

/** Categorías que se pueden elegir al crear una solicitud. */
val SUPPORT_CATEGORIES = listOf("Reservas", "Mi cuenta", "Una cancha o espacio", "Otro")

interface SupportRepository {
    suspend fun create(email: String, category: String, description: String): SupportRequest
    suspend fun mine(email: String): List<SupportRequest>
}

private fun defaultIncidenciasApi(): IncidenciasApi =
    ApiClient.create(ApiConfig.RESERVAS_URL, IncidenciasApi::class.java)

/**
 * Las solicitudes de ayuda se guardan como incidencias en el servicio de reservas, así el administrador
 * las ve en su contador de incidencias. La categoría viaja en el campo "espacio" de la incidencia.
 */
class RemoteSupportRepository(
    private val api: IncidenciasApi = defaultIncidenciasApi()
) : SupportRepository {

    override suspend fun create(email: String, category: String, description: String): SupportRequest =
        call {
            api.crear(
                CrearIncidenciaDto(usuarioEmail = email.trim(), espacio = category, descripcion = description.trim())
            ).toRequest()
        }

    override suspend fun mine(email: String): List<SupportRequest> =
        call { api.getPorUsuario(email.trim()).map { it.toRequest() } }

    private suspend fun <T> call(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: HttpException) {
            throw SupportException(e.serverMessage() ?: "El servicio respondió con error ${e.code()}")
        }
    }
}

private fun IncidenciaDto.toRequest() = SupportRequest(
    id = id,
    category = espacio,
    description = descripcion,
    resolved = estado.equals("RESUELTA", ignoreCase = true),
    date = creadaEn.take(10)
)

/** El servicio de reservas manda {"mensaje": "..."}; los otros, {"error": "..."}. Probamos las dos formas. */
private fun HttpException.serverMessage(): String? {
    return try {
        val raw = response()?.errorBody()?.string() ?: return null
        val json = JsonParser.parseString(raw).asJsonObject
        (json.get("mensaje") ?: json.get("error"))?.asString
    } catch (e: Exception) {
        null
    }
}
