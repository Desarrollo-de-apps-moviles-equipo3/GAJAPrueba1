package com.example.gaja_prueba1.ui.theme

import androidx.compose.material3.Typography
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

val Typography = Typography(
    headlineMedium = Base.headlineMedium.copy(fontFamily = InterFamily),
    titleLarge = Base.titleLarge.copy(fontFamily = InterFamily),
    titleMedium = Base.titleMedium.copy(fontFamily = InterFamily),
    bodyLarge = Base.bodyLarge.copy(fontFamily = InterFamily),
    bodyMedium = Base.bodyMedium.copy(fontFamily = InterFamily),
    labelLarge = Base.labelLarge.copy(fontFamily = InterFamily)
)