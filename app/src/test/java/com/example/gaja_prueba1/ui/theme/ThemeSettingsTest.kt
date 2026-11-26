package com.example.gaja_prueba1.ui.theme

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeSettingsTest {

    private class FakeStorage(var dark: Boolean = false) : ThemeStorage {
        var writes = 0
        override fun readDark(): Boolean = dark
        override fun writeDark(dark: Boolean) {
            this.dark = dark
            writes++
        }
    }

    @After
    fun volverAClaro() {
        ThemeSettings.init(FakeStorage(false))
    }

    @Test
    fun `sin nada guardado la app abre en modo claro`() {
        ThemeSettings.init(FakeStorage(dark = false))

        assertFalse(ThemeSettings.isDark)
    }

    @Test
    fun `si quedo guardado el modo oscuro, la app abre en oscuro`() {
        ThemeSettings.init(FakeStorage(dark = true))

        assertTrue(ThemeSettings.isDark)
    }

    @Test
    fun `cambiar de modo se aplica al tiro y se guarda`() {
        val storage = FakeStorage(dark = false)
        ThemeSettings.init(storage)

        ThemeSettings.chooseDark(true)

        assertTrue(ThemeSettings.isDark)
        assertTrue(storage.dark)
        assertEquals(1, storage.writes)
    }

    @Test
    fun `volver a claro tambien se guarda`() {
        val storage = FakeStorage(dark = true)
        ThemeSettings.init(storage)

        ThemeSettings.chooseDark(false)

        assertFalse(ThemeSettings.isDark)
        assertFalse(storage.dark)
    }

    @Test
    fun `la paleta de las pantallas sigue al modo`() {
        EspaciosPalette.isDark = false
        assertEquals(LightPalette.Navy, EspaciosPalette.Navy)
        assertEquals(LightPalette.White, EspaciosPalette.White)

        EspaciosPalette.isDark = true
        assertEquals(DarkPalette.Navy, EspaciosPalette.Navy)
        assertEquals(DarkPalette.White, EspaciosPalette.White)

        EspaciosPalette.isDark = false
    }
}
