package com.example.gaja_prueba1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.theme.InterFamily

enum class GajaChipTone(val background: Color, val content: Color) {
    Success(EspaciosPalette.GreenSoft, EspaciosPalette.Green),
    Danger(EspaciosPalette.DangerSoft, EspaciosPalette.Danger),
    Info(EspaciosPalette.SkyCard, EspaciosPalette.Navy),
    Neutral(EspaciosPalette.Beige, EspaciosPalette.Slate)
}

/** Etiqueta de estado (Confirmada, Pendiente, Rechazada...). No es clicable. */
@Composable
fun GajaStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: GajaChipTone = GajaChipTone.Neutral
) {
    Text(
        text = text,
        color = tone.content,
        fontFamily = InterFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        maxLines = 1,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(tone.background)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

/** Chip de filtro que se puede marcar y desmarcar. */
@Composable
fun GajaFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .heightIn(min = 36.dp)
            .clip(shape)
            .background(if (selected) EspaciosPalette.Navy else EspaciosPalette.White)
            .border(1.dp, if (selected) EspaciosPalette.Navy else EspaciosPalette.Beige, shape)
            .selectable(selected = selected, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (selected) EspaciosPalette.White else EspaciosPalette.Navy,
            fontFamily = InterFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            fontSize = 13.sp,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF6EF)
@Composable
fun GajaChipPreview() {
    GAJAPrueba1Theme {
        var selected by remember { mutableStateOf(0) }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GajaStatusChip("Confirmada", tone = GajaChipTone.Success)
                GajaStatusChip("Pendiente", tone = GajaChipTone.Info)
                GajaStatusChip("Rechazada", tone = GajaChipTone.Danger)
                GajaStatusChip("Pasada")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Todas", "Fútbol", "Tenis").forEachIndexed { index, name ->
                    GajaFilterChip(
                        text = name,
                        selected = selected == index,
                        onClick = { selected = index }
                    )
                }
            }
        }
    }
}