package com.example.gaja_prueba1.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoginValidatorTest {

    // ---------- Correo ----------

    @Test
    fun correo_vacio_da_error_de_obligatorio() {
        assertEquals("El correo es obligatorio", LoginValidator.validateEmail(""))
    }

    @Test
    fun correo_solo_con_espacios_da_error_de_obligatorio() {
        assertEquals("El correo es obligatorio", LoginValidator.validateEmail("   "))
    }

    @Test
    fun correo_sin_arroba_es_invalido() {
        assertEquals("Ingresa un correo valido", LoginValidator.validateEmail("profe.duoc.cl"))
    }

    @Test
    fun correo_sin_dominio_es_invalido() {
        assertEquals("Ingresa un correo valido", LoginValidator.validateEmail("profe@"))
    }

    @Test
    fun correo_sin_extension_es_invalido() {
        assertEquals("Ingresa un correo valido", LoginValidator.validateEmail("profe@duoc"))
    }

    @Test
    fun correo_valido_no_da_error() {
        assertNull(LoginValidator.validateEmail("profe_exe@duoc.cl"))
    }

    @Test
    fun correo_valido_con_espacios_alrededor_no_da_error() {
        assertNull(LoginValidator.validateEmail("  profe_exe@duoc.cl  "))
    }

    // ---------- Contraseña ----------

    @Test
    fun clave_vacia_da_error_de_obligatoria() {
        assertEquals("La contraseña es obligatoria", LoginValidator.validatePassword(""))
    }

    @Test
    fun clave_de_4_caracteres_es_demasiado_corta() {
        assertEquals(
            "Debe tener al menos 5 caracteres",
            LoginValidator.validatePassword("1234")
        )
    }

    @Test
    fun clave_de_5_caracteres_es_valida() {
        assertNull(LoginValidator.validatePassword("12345"))
    }

    @Test
    fun clave_larga_es_valida() {
        assertNull(LoginValidator.validatePassword("Prueba2026"))
    }
}