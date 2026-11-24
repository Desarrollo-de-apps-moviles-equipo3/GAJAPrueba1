package com.example.gaja_prueba1.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CategoryValidatorTest {

    private val existing = listOf("Deportes", "Recintos")

    @Test
    fun nombre_valido_no_da_error() {
        assertNull(CategoryValidator.validateName("Eventos", existing))
    }

    @Test
    fun nombre_vacio_o_con_espacios_da_error() {
        assertEquals("Escribe el nombre de la categoría", CategoryValidator.validateName("", existing))
        assertEquals("Escribe el nombre de la categoría", CategoryValidator.validateName("   ", existing))
    }

    @Test
    fun nombre_muy_largo_da_error() {
        val largo = "a".repeat(CategoryValidator.MAX_NAME_LENGTH + 1)
        assertEquals("Máximo 40 caracteres", CategoryValidator.validateName(largo, existing))
        assertNull(CategoryValidator.validateName("a".repeat(CategoryValidator.MAX_NAME_LENGTH), existing))
    }

    @Test
    fun nombre_repetido_da_error_sin_importar_mayusculas() {
        assertEquals("Ya existe una categoría con ese nombre", CategoryValidator.validateName("deportes", existing))
        assertEquals("Ya existe una categoría con ese nombre", CategoryValidator.validateName("  RECINTOS ", existing))
    }

    @Test
    fun al_editar_puede_quedar_con_su_mismo_nombre() {
        assertNull(CategoryValidator.validateName("Deportes", existing, editing = "Deportes"))
        assertNull(CategoryValidator.validateName("DEPORTES", existing, editing = "Deportes"))
    }

    @Test
    fun al_editar_no_puede_tomar_el_nombre_de_otra() {
        assertEquals(
            "Ya existe una categoría con ese nombre",
            CategoryValidator.validateName("Recintos", existing, editing = "Deportes")
        )
    }
}
