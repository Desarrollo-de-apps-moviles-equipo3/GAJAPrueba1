package com.example.gaja_prueba1.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme

@Composable
fun GajaSelector(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 2,
    icons: List<ImageVector> = emptyList(),
    label: String? = null
) {
    val safeColumns = columns.coerceAtLeast(1)
    Column(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (label != null) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
            )
        }
        options.indices.chunked(safeColumns).forEach { rowIndices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowIndices.forEach { index ->
                    SelectorOption(
                        text = options[index],
                        icon = icons.getOrNull(index),
                        selected = index == selectedIndex,
                        onClick = { onSelect(index) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(safeColumns - rowIndices.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SelectorOption(
    text: String,
    icon: ImageVector?,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    val shape = MaterialTheme.shapes.medium
    val containerColor = if (selected) scheme.primary else scheme.surfaceContainerLow
    val contentColor = if (selected) scheme.onPrimary else scheme.onSurface
    val borderColor = if (selected) scheme.primary else scheme.outlineVariant

    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(shape)
            .background(containerColor)
            .border(1.dp, borderColor, shape)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFAF6EF)
@Composable
fun GajaSelectorPreview() {
    GAJAPrueba1Theme {
        var role by remember { mutableStateOf(0) }
        GajaSelector(
            modifier = Modifier.padding(16.dp),
            label = "Ingresar como",
            options = listOf("Cadete", "Oficial", "Oficial de semana", "Institución externa"),
            icons = listOf(
                Icons.Outlined.Person,
                Icons.Outlined.Shield,
                Icons.Outlined.AssignmentTurnedIn,
                Icons.Outlined.Apartment
            ),
            selectedIndex = role,
            onSelect = { role = it }
        )
    }
}