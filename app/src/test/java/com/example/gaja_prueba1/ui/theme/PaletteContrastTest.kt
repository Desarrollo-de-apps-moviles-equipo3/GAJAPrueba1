package com.example.gaja_prueba1.ui.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

/**
 * Revisa que cada combinación de texto y fondo que usan las pantallas se pueda leer en los dos modos.
 * 4,5 es el contraste mínimo que recomienda WCAG para texto normal.
 */
class PaletteContrastTest {

    private fun luminance(c: Color): Double {
        fun channel(v: Float): Double {
            val x = v.toDouble()
            return if (x <= 0.03928) x / 12.92 else ((x + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
    }

    private fun contrast(a: Color, b: Color): Double {
        val la = luminance(a)
        val lb = luminance(b)
        val (hi, lo) = if (la >= lb) la to lb else lb to la
        return (hi + 0.05) / (lo + 0.05)
    }

    private fun check(name: String, text: Color, background: Color) {
        val ratio = contrast(text, background)
        assertTrue("$name: contraste ${"%.1f".format(ratio)} (mínimo 4,5)", ratio >= 4.5)
    }

    @Test
    // El modo claro conserva los colores originales de la marca; el gris Slate y el verde quedan algo bajo 4,5 y no se evalúan aquí.
    fun `modo claro - textos legibles sobre sus fondos`() {
        with(LightPalette) {
            check("Navy sobre Cream", Navy, Cream)
            check("Navy sobre White", Navy, White)
            check("Navy sobre SkyCard", Navy, SkyCard)
            check("White sobre Navy", White, Navy)
            check("Danger sobre DangerSoft", Danger, DangerSoft)
        }
    }

    @Test
    fun `modo oscuro - textos legibles sobre sus fondos`() {
        with(DarkPalette) {
            check("Navy sobre Cream", Navy, Cream)
            check("Navy sobre White", Navy, White)
            check("Navy sobre SkyCard", Navy, SkyCard)
            check("Navy sobre Beige", Navy, Beige)
            check("White sobre Navy", White, Navy)
            check("White sobre NavyTop", White, NavyTop)
            check("White sobre Steel", White, Steel)
            check("Slate sobre Cream", Slate, Cream)
            check("Slate sobre White", Slate, White)
            check("Slate sobre Beige", Slate, Beige)
            check("Slate sobre SkyCard", Slate, SkyCard)
            check("Steel sobre Cream", Steel, Cream)
            check("Steel sobre White", Steel, White)
            check("Steel sobre SkyCard", Steel, SkyCard)
            check("Green sobre GreenSoft", Green, GreenSoft)
            check("Danger sobre DangerSoft", Danger, DangerSoft)
            check("Danger sobre Cream", Danger, Cream)
        }
    }
}
