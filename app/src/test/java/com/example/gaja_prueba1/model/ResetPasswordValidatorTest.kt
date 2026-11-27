package com.example.gaja_prueba1.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ResetPasswordValidatorTest {

    private fun validar(
        email: String = "ana@gaja.cl",
        code: String = "ABCD2345",
        password: String = "nueva123",
        confirm: String = "nueva123"
    ) = ResetPasswordValidator.validate(email, code, password, confirm)

    @Test
    fun datosCorrectosNoTienenError() {
        assertNull(validar())
    }

    @Test
    fun exigeUnCorreo() {
        assertNotNull(validar(email = ""))
        assertNotNull(validar(email = "sin-arroba"))
    }

    @Test
    fun exigeElCodigo() {
        assertNotNull(validar(code = "   "))
    }

    @Test
    fun laContrasenaDebeTenerElLargoPermitido() {
        assertNotNull(validar(password = "abc", confirm = "abc"))
        val larga = "a".repeat(ResetPasswordValidator.MAX_PASSWORD + 1)
        assertNotNull(validar(password = larga, confirm = larga))
        assertNull(validar(password = "abcde", confirm = "abcde"))
    }

    @Test
    fun lasContrasenasDebenCoincidir() {
        assertEquals("Las contraseñas no coinciden", validar(confirm = "otra1234"))
    }
}
