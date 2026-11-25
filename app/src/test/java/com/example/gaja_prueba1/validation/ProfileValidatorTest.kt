package com.example.gaja_prueba1.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileValidatorTest {

    @Test
    fun la_contrasena_actual_es_obligatoria() {
        assertNotNull(ProfileValidator.validateCurrentPassword(""))
        assertNull(ProfileValidator.validateCurrentPassword("12345"))
    }

    @Test
    fun la_contrasena_nueva_tiene_largo_minimo_y_debe_ser_distinta_de_la_actual() {
        assertNotNull(ProfileValidator.validateNewPassword("1234", "12345"))
        assertNotNull(ProfileValidator.validateNewPassword("12345", "12345"))
        assertNotNull(ProfileValidator.validateNewPassword("x".repeat(61), "12345"))
        assertNull(ProfileValidator.validateNewPassword("nueva1", "12345"))
    }

    @Test
    fun la_confirmacion_debe_coincidir() {
        assertNotNull(ProfileValidator.validateConfirmation("nueva1", "nueva2"))
        assertNull(ProfileValidator.validateConfirmation("nueva1", "nueva1"))
    }

    @Test
    fun la_solicitud_necesita_tema_y_descripcion() {
        assertNotNull(ProfileValidator.validateRequestCategory(""))
        assertNull(ProfileValidator.validateRequestCategory("Reservas"))
        assertNotNull(ProfileValidator.validateRequestDescription("   "))
        assertNull(ProfileValidator.validateRequestDescription("No puedo reservar"))
        assertNotNull(ProfileValidator.validateRequestDescription("x".repeat(1001)))
    }
}
