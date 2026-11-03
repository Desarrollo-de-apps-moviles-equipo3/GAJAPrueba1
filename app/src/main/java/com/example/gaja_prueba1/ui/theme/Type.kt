package com.example.gaja_prueba1.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.gaja_prueba1.R

val InterFamily = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold)
)

private val Base = Typography()

private fun TextStyle.inter(): TextStyle = copy(fontFamily = InterFamily)

val Typography = Typography(
    displayLarge = Base.displayLarge.inter(),
    displayMedium = Base.displayMedium.inter(),
    displaySmall = Base.displaySmall.inter(),
    headlineLarge = Base.headlineLarge.inter(),
    headlineMedium = Base.headlineMedium.inter(),
    headlineSmall = Base.headlineSmall.inter(),
    titleLarge = Base.titleLarge.inter(),
    titleMedium = Base.titleMedium.inter(),
    titleSmall = Base.titleSmall.inter(),
    bodyLarge = Base.bodyLarge.inter(),
    bodyMedium = Base.bodyMedium.inter(),
    bodySmall = Base.bodySmall.inter(),
    labelLarge = Base.labelLarge.inter(),
    labelMedium = Base.labelMedium.inter(),
    labelSmall = Base.labelSmall.inter()
)