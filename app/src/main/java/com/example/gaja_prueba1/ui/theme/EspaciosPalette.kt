package com.example.gaja_prueba1.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Colores de la marca en modo claro. Son fijos: los usa el esquema claro del tema y el panel lateral del administrador. */
object LightPalette {
    val Navy = Color(0xFF0F2537)
    val NavyTop = Color(0xFF17324A)
    val Steel = Color(0xFF3B6989)
    val Orange = Color(0xFFD97736)
    val Beige = Color(0xFFE6DDD4)
    val Cream = Color(0xFFF7F4EF)
    val White = Color(0xFFFFFFFF)
    val Slate = Color(0xFF718797)
    val SkyCard = Color(0xFFDBEAFE)
    val Green = Color(0xFF2E7D4F)
    val GreenSoft = Color(0xFFDCF5E5)
    val Danger = Color(0xFFC0392B)
    val DangerSoft = Color(0xFFFBE9E7)
}

/** Los mismos nombres en modo oscuro. Cada par de colores que se usa junto mantiene un contraste de al menos 4,5. */
object DarkPalette {
    val Navy = Color(0xFFDCE6EE)
    val NavyTop = Color(0xFFA9C8E3)
    val Steel = Color(0xFF8DB4D3)
    val Orange = Color(0xFFD97736)
    val Beige = Color(0xFF2A4157)
    val Cream = Color(0xFF0B1A27)
    val White = Color(0xFF132638)
    val Slate = Color(0xFFA9BAC7)
    val SkyCard = Color(0xFF24435E)
    val Green = Color(0xFF6FCF97)
    val GreenSoft = Color(0xFF17382A)
    val Danger = Color(0xFFFF8A7A)
    val DangerSoft = Color(0xFF3A1D1A)
}

/**
 * Paleta que usan las pantallas. Cambia sola entre claro y oscuro según [isDark], que se guarda como estado de Compose:
 * al cambiar el modo, todo lo que la lee se vuelve a dibujar.
 *
 * Los nombres siguen su papel en modo claro. En oscuro se invierten: `Navy` (texto y botones) pasa a ser claro y
 * `White` (tarjetas y texto sobre botones) pasa a ser oscuro, así que cada combinación sigue siendo legible.
 */
object EspaciosPalette {
    var isDark by mutableStateOf(false)

    val Navy: Color get() = if (isDark) DarkPalette.Navy else LightPalette.Navy
    val NavyTop: Color get() = if (isDark) DarkPalette.NavyTop else LightPalette.NavyTop
    val Steel: Color get() = if (isDark) DarkPalette.Steel else LightPalette.Steel
    val Orange: Color get() = if (isDark) DarkPalette.Orange else LightPalette.Orange
    val Beige: Color get() = if (isDark) DarkPalette.Beige else LightPalette.Beige
    val Cream: Color get() = if (isDark) DarkPalette.Cream else LightPalette.Cream
    val White: Color get() = if (isDark) DarkPalette.White else LightPalette.White
    val Slate: Color get() = if (isDark) DarkPalette.Slate else LightPalette.Slate
    val SkyCard: Color get() = if (isDark) DarkPalette.SkyCard else LightPalette.SkyCard
    val Green: Color get() = if (isDark) DarkPalette.Green else LightPalette.Green
    val GreenSoft: Color get() = if (isDark) DarkPalette.GreenSoft else LightPalette.GreenSoft
    val Danger: Color get() = if (isDark) DarkPalette.Danger else LightPalette.Danger
    val DangerSoft: Color get() = if (isDark) DarkPalette.DangerSoft else LightPalette.DangerSoft
}
