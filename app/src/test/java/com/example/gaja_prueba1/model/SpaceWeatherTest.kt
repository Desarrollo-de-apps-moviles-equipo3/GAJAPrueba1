package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.remote.ClimaActual
import com.example.gaja_prueba1.data.remote.ClimaApi
import com.example.gaja_prueba1.data.remote.ClimaResponse
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class SpaceWeatherTest {

    private fun space(id: String, lat: Double?, lon: Double?) = Space(
        id = id, name = id, category = "Deportes", description = "", capacity = 10,
        available = true, colorHex = "#000000", latitude = lat, longitude = lon
    )

    private class FakeClimaApi(
        private val response: ClimaResponse? = ClimaResponse(ClimaActual(21.6, 0, 11.4)),
        private val fail: Boolean = false
    ) : ClimaApi {
        var calls = 0
        override suspend fun actual(latitude: Double, longitude: Double, current: String, timezone: String): ClimaResponse {
            calls++
            if (fail) throw IOException("sin conexión")
            return response!!
        }
    }

    @Test
    fun codigos_del_clima_se_traducen_a_espanol() {
        assertEquals("Despejado", weatherDescription(0))
        assertEquals("Nublado", weatherDescription(3))
        assertEquals("Lluvia", weatherDescription(63))
        assertEquals("Tormenta", weatherDescription(95))
        assertEquals("Sin datos", weatherDescription(-1))
    }

    @Test
    fun el_resumen_redondea_y_junta_temperatura_y_descripcion() {
        val weather = SpaceWeather(temperatureC = 22, description = "Despejado", windKmh = 11)
        assertEquals("22° · Despejado", weather.summary)
    }

    @Test
    fun consulta_una_vez_por_zona_y_reparte_el_clima_a_cada_espacio() = runBlocking {
        val api = FakeClimaApi()
        val repo = RemoteWeatherRepository(api)
        val result = repo.weatherFor(
            listOf(
                space("a", -33.4042, -70.5781),
                space("b", -33.4039, -70.5770)
            )
        )
        assertEquals(1, api.calls)
        assertEquals(setOf("a", "b"), result.keys)
        assertEquals(22, result.getValue("a").temperatureC)
        assertEquals("Despejado", result.getValue("b").description)
        assertEquals(11, result.getValue("a").windKmh)
    }

    @Test
    fun los_espacios_sin_coordenadas_no_tienen_clima() = runBlocking {
        val api = FakeClimaApi()
        val result = RemoteWeatherRepository(api).weatherFor(listOf(space("a", null, null)))
        assertTrue(result.isEmpty())
        assertEquals(0, api.calls)
    }

    @Test(expected = IOException::class)
    fun si_no_hay_conexion_el_error_sube_para_que_el_viewmodel_lo_muestre() {
        runBlocking {
            RemoteWeatherRepository(FakeClimaApi(fail = true)).weatherFor(listOf(space("a", -33.4, -70.5)))
        }
    }

    @Test(expected = IOException::class)
    fun una_respuesta_sin_temperatura_cuenta_como_error() {
        runBlocking {
            val api = FakeClimaApi(response = ClimaResponse(null))
            RemoteWeatherRepository(api).weatherFor(listOf(space("a", -33.4, -70.5)))
        }
    }
}
