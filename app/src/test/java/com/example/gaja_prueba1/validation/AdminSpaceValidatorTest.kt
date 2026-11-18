package com.example.gaja_prueba1.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AdminSpaceValidatorTest {

    @Test
    fun nombre_vacio_da_error() {
        assertEquals("Escribe el nombre del espacio", AdminSpaceValidator.validateName("   "))
    }

    @Test
    fun nombre_muy_largo_da_error() {
        assertEquals("Máximo 60 caracteres", AdminSpaceValidator.validateName("a".repeat(61)))
    }

    @Test
    fun nombre_normal_es_valido() {
        assertNull(AdminSpaceValidator.validateName("Cancha de Fútbol C"))
    }

    @Test
    fun categoria_vacia_da_error() {
        assertEquals("Elige una categoría", AdminSpaceValidator.validateCategory(""))
        assertNull(AdminSpaceValidator.validateCategory("Deportes"))
    }

    @Test
    fun capacidad_no_numerica_da_error() {
        assertEquals("Escribe un número", AdminSpaceValidator.validateCapacity(""))
        assertEquals("Escribe un número", AdminSpaceValidator.validateCapacity("abc"))
    }

    @Test
    fun capacidad_cero_o_sobre_el_maximo_da_error() {
        assertEquals("La capacidad debe ser al menos 1", AdminSpaceValidator.validateCapacity("0"))
        assertEquals("La capacidad máxima es 500", AdminSpaceValidator.validateCapacity("501"))
    }

    @Test
    fun capacidad_valida_pasa() {
        assertNull(AdminSpaceValidator.validateCapacity("14"))
        assertNull(AdminSpaceValidator.validateCapacity(" 500 "))
    }
}
