package com.example.gaja_prueba1.ui.components

import android.R.attr.content
import android.text.Highlights
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.theme.InterFamily
/** Tarjeta blanca con esquinas redondeadas y sombra suave. Si le pasas onClick, se puede tocar. */
@Composable
fun GajaCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit) ? = null,
    containerColor: Color = EspaciosPalette.White,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val colors = CardDefaults.cardColors(containerColor = containerColor)
    val elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    if (onClick == null) {
        Card(modifier = modifier, shape = shape, colors = colors, elevation = elevation) {
            content()
        }
    } else {
        Card(onClick = onClick, modifier = modifier, shape = shape, colors = colors, elevation = elevation) {
            content()
        }
    }
}

/** Tarjeta con un número grande y una etiqueta, como los contadores del Inicio. */
@Composable
fun GajaStatCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    val color = if (highlight) EspaciosPalette.Green else EspaciosPalette.Navy
    Column(
        modifier = modifier
            .background(EspaciosPalette.SkyCard, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = color,
            fontFamily = InterFamily,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = color,
            fontFamily = InterFamily,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GajaCardPreview() {
    GAJAPrueba1Theme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)){
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GajaStatCard("6", "Áreas disponibles", highlight = true, modifier = Modifier.weight(1f))
                GajaStatCard("2", "Próximas reservas", modifier = Modifier.weight(1f))
                GajaStatCard("1", "Reservas Pasadas", modifier = Modifier.weight(1f))
            }
            GajaCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Cancha de Fútbol A",
                        color = EspaciosPalette.Navy,
                        fontFamily = InterFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Tarjeta con contenido libre.",
                        color = EspaciosPalette.Slate,
                        fontFamily = InterFamily,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}