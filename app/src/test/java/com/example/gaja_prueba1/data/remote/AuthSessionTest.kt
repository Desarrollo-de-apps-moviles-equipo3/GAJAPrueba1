package com.example.gaja_prueba1.data.remote

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthSessionTest {

    @After
    fun limpiar() = AuthSession.clear()

    @Test
    fun guardaYBorraElToken() {
        assertNull(AuthSession.token())

        AuthSession.save("abc.def.ghi")
        assertEquals("abc.def.ghi", AuthSession.token())

        AuthSession.clear()
        assertNull(AuthSession.token())
    }

    @Test
    fun alVencerLaSesionSeBorraElToken() {
        AuthSession.save("abc.def.ghi")

        AuthSession.notifyExpired()

        assertNull(AuthSession.token())
    }

    @Test
    fun elTokenSoloSeMandaANuestrosServicios() {
        assertTrue(ApiConfig.isOwnService(ApiConfig.USUARIOS_URL + "api/usuarios/login"))
        assertTrue(ApiConfig.isOwnService(ApiConfig.RESERVAS_URL + "api/reservas"))
        assertTrue(ApiConfig.isOwnService(ApiConfig.ESPACIOS_URL + "api/espacios"))
        assertFalse(ApiConfig.isOwnService(ApiConfig.CLIMA_URL + "v1/forecast"))
    }
}
