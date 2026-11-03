package com.example.gaja_prueba1.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SpaceDistanceTest {
    private fun space(id: String, lat: Double?, lon: Double?) =
        Space(id, id, "Deportes", "", 10, true, "#3B6989", emptyList(), lat, lon)

    @Test fun mismaPosicion_distanciaCero() =
        assertEquals(0.0, distanceMeters(-33.4, -70.5, -33.4, -70.5), 0.001)

    @Test fun unGradoDeLatitud_son111km() =
        assertEquals(111_195.0, distanceMeters(0.0, 0.0, 1.0, 0.0), 100.0)

    @Test fun ordenaDeMasCercaAMasLejos() {
        val cerca = space("cerca", -33.4001, -70.5)
        val lejos = space("lejos", -33.41, -70.5)
        val medio = space("medio", -33.402, -70.5)
        val ids = sortByDistance(listOf(lejos, cerca, medio), -33.4, -70.5).map { it.id }
        assertEquals(listOf("cerca", "medio", "lejos"), ids)
    }

    @Test fun sinCoordenadas_quedanAlFinal() {
        val sin = space("sin", null, null)
        val con = space("con", -33.41, -70.5)
        val ids = sortByDistance(listOf(sin, con), -33.4, -70.5).map { it.id }
        assertEquals(listOf("con", "sin"), ids)
    }

    @Test fun distanciaDeEspacioSinCoordenadas_esNula() =
        assertNull(space("x", null, null).distanceFrom(-33.4, -70.5))

    @Test fun formatoEnMetros() = assertEquals("850 m", formatDistance(850.4))
    @Test fun formatoEnKilometros() = assertEquals("1,2 km", formatDistance(1234.0))
}
