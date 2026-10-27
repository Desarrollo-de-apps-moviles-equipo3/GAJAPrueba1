package com.example.gaja_prueba1.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RecoverAccessValidatorTest {

    @Test
    fun vacio_pide_usuario_o_correo() {
        assertEquals(
            "Ingresa tu usuario o correo institucional",
            RecoverAccessValidator.validate("")
        )
    }

    @Test
    fun solo_espacios_cuenta_como_vacio() {
        assertEquals(
            "Ingresa tu usuario o correo institucional",
            RecoverAccessValidator.validate("   ")
        )
    }

    @Test
    fun usuario_con_espacio_en_medio_es_invalido() {
        assertEquals("No puede contener espacios", RecoverAccessValidator.validate("juan perez"))
    }

    @Test
    fun usuario_de_2_caracteres_es_muy_corto() {
        assertEquals("Debe tener al menos 3 caracteres", RecoverAccessValidator.validate("ab"))
    }

    @Test
    fun usuario_de_3_caracteres_es_valido() {
        assertNull(RecoverAccessValidator.validate("abc"))
    }

    @Test
    fun correo_mal_formado_es_invalido() {
        assertEquals("Correo no válido", RecoverAccessValidator.validate("juan@duoc"))
    }

    @Test
    fun correo_valido_no_da_error() {
        assertNull(RecoverAccessValidator.validate("galcala@duoc.cl"))
    }

    @Test
    fun usuario_de_prueba_galcala_es_valido() {
        assertNull(RecoverAccessValidator.validate("galcala"))
    }
}