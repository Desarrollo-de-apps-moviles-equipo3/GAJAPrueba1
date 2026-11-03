package com.example.gaja_prueba1.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------- Claro (marca: azul marino, acero, naranja, crema) ----------
internal val LightColorScheme: ColorScheme = lightColorScheme(
    primary = EspaciosPalette.Navy,
    onPrimary = Color.White,
    primaryContainer = EspaciosPalette.SkyCard,
    onPrimaryContainer = EspaciosPalette.Navy,

    secondary = EspaciosPalette.Steel,
    onSecondary = Color.White,
    secondaryContainer = EspaciosPalette.Beige,
    onSecondaryContainer = EspaciosPalette.Navy,

    tertiary = EspaciosPalette.Orange,
    onTertiary = EspaciosPalette.Navy,
    tertiaryContainer = Color(0xFFFBE3D3),
    onTertiaryContainer = Color(0xFF5A2A0A),

    background = EspaciosPalette.Cream,
    onBackground = EspaciosPalette.Navy,

    surface = EspaciosPalette.Cream,
    onSurface = EspaciosPalette.Navy,
    surfaceVariant = EspaciosPalette.Beige,
    onSurfaceVariant = Color(0xFF4A5F70),
    surfaceTint = EspaciosPalette.Navy,

    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBF8F3),
    surfaceContainer = Color(0xFFF1ECE5),
    surfaceContainerHigh = Color(0xFFEAE4DB),
    surfaceContainerHighest = EspaciosPalette.Beige,

    outline = EspaciosPalette.Slate,
    outlineVariant = EspaciosPalette.Beige,

    error = EspaciosPalette.Danger,
    onError = Color.White,
    errorContainer = EspaciosPalette.DangerSoft,
    onErrorContainer = Color(0xFF7A1F15)
)

// ---------- Oscuro ----------
internal val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFA9C8E3),
    onPrimary = EspaciosPalette.Navy,
    primaryContainer = Color(0xFF24435E),
    onPrimaryContainer = EspaciosPalette.SkyCard,

    secondary = Color(0xFF9DBFD9),
    onSecondary = EspaciosPalette.Navy,
    secondaryContainer = Color(0xFF2E4B62),
    onSecondaryContainer = Color(0xFFDDE7EF),

    tertiary = Color(0xFFF0A878),
    onTertiary = Color(0xFF3A1A05),
    tertiaryContainer = Color(0xFF8A4519),
    onTertiaryContainer = Color(0xFFFBE3D3),

    background = Color(0xFF0B1A27),
    onBackground = Color(0xFFE6EAEE),

    surface = Color(0xFF0B1A27),
    onSurface = Color(0xFFE6EAEE),
    surfaceVariant = Color(0xFF233A51),
    onSurfaceVariant = Color(0xFFB7C4CE),
    surfaceTint = Color(0xFFA9C8E3),

    surfaceContainerLowest = Color(0xFF08141F),
    surfaceContainerLow = Color(0xFF0F2030),
    surfaceContainer = Color(0xFF132638),
    surfaceContainerHigh = Color(0xFF1B3045),
    surfaceContainerHighest = Color(0xFF233A51),

    outline = Color(0xFF7F93A3),
    outlineVariant = Color(0xFF2E4357),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6)
)

// ---------- Colores extendidos (éxito) ----------
@Immutable
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color
)

internal val LightExtendedColors = ExtendedColors(
    success = EspaciosPalette.Green,
    onSuccess = Color.White,
    successContainer = EspaciosPalette.GreenSoft,
    onSuccessContainer = Color(0xFF1B5E3A)
)

internal val DarkExtendedColors = ExtendedColors(
    success = Color(0xFF8FD9AE),
    onSuccess = Color(0xFF00391C),
    successContainer = Color(0xFF1B5E3A),
    onSuccessContainer = EspaciosPalette.GreenSoft
)

internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }