package com.example.gaja_prueba1.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class UserValidatorTest {

    @Test
    fun el_nombre_es_obligatorio_y_tiene_largo_maximo() {
        assertNotNull(UserValidator.validateName("   "))
        assertNotNull(UserValidator.validateName("a".repeat(81)))
        assertNull(UserValidator.validateName("Ana Pérez"))
    }

    @Test
    fun el_correo_debe_tener_formato_valido() {
        assertNotNull(UserValidator.validateEmail(""))
        assertNotNull(UserValidator.validateEmail("sin-arroba"))
        assertNotNull(UserValidator.validateEmail("a@b"))
        assertNull(UserValidator.validateEmail("ana@gaja.cl"))
        assertNull(UserValidator.validateEmail("  ana.perez+1@duoc.cl  "))
    }

    @Test
    fun la_contrasena_tiene_largo_minimo_y_maximo() {
        assertNotNull(UserValidator.validatePassword("1234"))
        assertNull(UserValidator.validatePassword("12345"))
        assertNotNull(UserValidator.validatePassword("x".repeat(61)))
    }

    @Test
    fun hay_que_elegir_un_rol() {
        assertNotNull(UserValidator.validateRole(""))
        assertNull(UserValidator.validateRole("Cadete"))
    }

    @Test
    fun el_telefono_es_opcional_pero_si_se_escribe_debe_ser_valido() {
        assertNull(UserValidator.validatePhone(""))
        assertNull(UserValidator.validatePhone("+56 9 1111 2222"))
        assertNotNull(UserValidator.validatePhone("12-34"))
        assertNotNull(UserValidator.validatePhone("abc"))
        assertNotNull(UserValidator.validatePhone("1".repeat(21)))
    }

    @Test
    fun la_institucion_es_obligatoria_solo_para_instituciones_externas() {
        assertNotNull(UserValidator.validateInstitution("", "Institución externa"))
        assertNull(UserValidator.validateInstitution("Liceo Vecino", "Institución externa"))
        assertNull(UserValidator.validateInstitution("", "Cadete"))
    }

    @Test
    fun el_motivo_de_rechazo_es_obligatorio() {
        assertNotNull(UserValidator.validateRejectReason("  "))
        assertNull(UserValidator.validateRejectReason("Datos incompletos"))
        assertNotNull(UserValidator.validateRejectReason("x".repeat(201)))
    }
}
