package com.example.gaja_prueba1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.theme.extendedColors

enum class GajaChipTone { Success, Danger, Info, Neutral }

private data class ChipColors(val background: Color, val content: Color)

@Composable
private fun GajaChipTone.colors(): ChipColors {
    val scheme = MaterialTheme.colorScheme
    val extended = MaterialTheme.extendedColors
    return when (this) {
        GajaChipTone.Success -> ChipColors(extended.successContainer, extended.onSuccessContainer)
        GajaChipTone.Danger -> ChipColors(scheme.errorContainer, scheme.onErrorContainer)
        GajaChipTone.Info -> ChipColors(scheme.primaryContainer, scheme.onPrimaryContainer)
        GajaChipTone.Neutral -> ChipColors(scheme.surfaceVariant, scheme.onSurfaceVariant)
    }
}

/** Etiqueta de estado (Confirmada, Pendiente, Rechazada...). No es clicable. */
@Composable
fun GajaStatusChip(
    text: String,
    modifier: Modifier = Modifier,
    tone: GajaChipTone = GajaChipTone.Neutral
) {
    val colors = tone.colors()
    Text(
        text = text,
        color = colors.content,
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
        maxLines = 1,
        modifier = modifier
            .clip(CircleShape)
            .background(colors.background)
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
    val scheme = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        shape = CircleShape,
        label = {
            Text(
                text = text,
                maxLines = 1,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = scheme.surfaceContainerLowest,
            labelColor = scheme.onSurface,
            selectedContainerColor = scheme.primary,
            selectedLabelColor = scheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = scheme.outlineVariant,
            selectedBorderColor = scheme.primary
        )
    )
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