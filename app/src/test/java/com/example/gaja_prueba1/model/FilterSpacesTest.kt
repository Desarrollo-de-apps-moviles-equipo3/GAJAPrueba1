package com.example.gaja_prueba1.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FilterSpacesTest {

    private val spaces = listOf(
        Space("futbol-a", "Cancha de Fútbol A", "Deportes", "", 14, true, "#2F6B4F", listOf("Fútbol")),
        Space("tenis-c", "Cancha de Tenis C", "Deportes", "", 4, true, "#3B6989", listOf("Tenis")),
        Space("sala-1", "Sala de Reuniones 1", "Salas", "", 20, true, "#17324A", listOf("Reuniones"))
    )

    @Test
    fun busqueda_vacia_devuelve_todos() {
        assertEquals(3, filterSpaces(spaces, "").size)
    }

    @Test
    fun busca_por_nombre() {
        assertEquals(listOf("tenis-c"), filterSpaces(spaces, "tenis").map { it.id })
    }

    @Test
    fun ignora_mayusculas_y_tildes() {
        assertEquals(listOf("futbol-a"), filterSpaces(spaces, "FUTBOL").map { it.id })
    }

    @Test
    fun busca_por_categoria() {
        assertEquals(listOf("sala-1"), filterSpaces(spaces, "salas").map { it.id })
    }

    @Test
    fun busca_por_etiqueta() {
        assertEquals(listOf("sala-1"), filterSpaces(spaces, "reuniones").map { it.id })
    }

    @Test
    fun sin_coincidencias_devuelve_lista_vacia() {
        assertEquals(0, filterSpaces(spaces, "piscina").size)
    }
}