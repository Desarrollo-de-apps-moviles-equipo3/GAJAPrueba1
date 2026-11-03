package com.example.gaja_prueba1.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.ui.theme.EspaciosPalette

@Composable
fun AllCourtsScreen(
    spaces: List<Space>,
    onBack: () -> Unit,
    onReserveClick: (Space) -> Unit
) {
    var selectedSpaceId by rememberSaveable { mutableStateOf<String?>(null) }
    val selectedSpace = spaces.firstOrNull { it.id == selectedSpaceId && it.available }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 18.dp, top = 8.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = EspaciosPalette.Steel
                )
            }
            Text(
                text = "Nueva reserva",
                color = EspaciosPalette.Navy,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        ReservationSteps(activeStep = 1)

        Text(
            text = "Selecciona una cancha",
            modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 12.dp),
            color = EspaciosPalette.Navy,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp
        )

        if (spaces.isEmpty()) {
            Text(
                text = "No hay canchas registradas todavía.",
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                color = EspaciosPalette.Slate,
                fontSize = 13.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 18.dp, vertical = 2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(spaces, key = { it.id }) { space ->
                    CourtSelectionCard(
                        space = space,
                        selected = selectedSpaceId == space.id,
                        onClick = { if (space.available) selectedSpaceId = space.id }
                    )
                }
            }
        }

        Button(
            onClick = { selectedSpace?.let(onReserveClick) },
            enabled = selectedSpace != null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .height(48.dp),
            shape = RoundedCornerShape(9.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EspaciosPalette.Navy,
                contentColor = EspaciosPalette.White,
                disabledContainerColor = EspaciosPalette.Navy.copy(alpha = 0.45f),
                disabledContentColor = EspaciosPalette.White
            )
        ) {
            Text("Siguiente   →", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ReservationSteps(activeStep: Int) {
    val labels = listOf("Cancha", "Fecha", "Horario")
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        labels.forEachIndexed { index, label ->
            val step = index + 1
            val active = step == activeStep
            val completed = step < activeStep
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            color = if (active) EspaciosPalette.Navy else EspaciosPalette.White,
                            shape = CircleShape
                        )
                        .then(
                            if (active) Modifier else Modifier.border(1.dp, EspaciosPalette.Steel, CircleShape)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (completed) "✓" else step.toString(),
                        color = if (active) EspaciosPalette.White else EspaciosPalette.Steel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Text(
                    text = label,
                    modifier = Modifier.padding(start = 5.dp),
                    color = if (active) EspaciosPalette.Navy else EspaciosPalette.Slate,
                    fontSize = 10.sp
                )
            }
            if (index < labels.lastIndex) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 5.dp)
                        .height(1.dp)
                        .background(EspaciosPalette.Beige)
                )
            }
        }
    }
}

@Composable
private fun CourtSelectionCard(
    space: Space,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(10.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(EspaciosPalette.White)
            .border(
                width = if (selected) 1.dp else 0.6.dp,
                color = if (selected) EspaciosPalette.Steel else EspaciosPalette.Beige,
                shape = shape
            )
            .clickable(enabled = space.available, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CourtThumbnail(colorHex = space.colorHex, modifier = Modifier.size(width = 40.dp, height = 32.dp))
        Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
            Text(
                text = space.name,
                color = EspaciosPalette.Navy,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (space.available) space.category else "${space.category} · No disponible",
                color = EspaciosPalette.Slate,
                fontSize = 10.sp
            )
        }
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = space.available,
            modifier = Modifier.size(32.dp),
            colors = RadioButtonDefaults.colors(
                selectedColor = EspaciosPalette.Navy,
                unselectedColor = EspaciosPalette.Steel,
                disabledUnselectedColor = EspaciosPalette.Beige
            )
        )
    }
}

@Composable
private fun CourtThumbnail(colorHex: String, modifier: Modifier = Modifier) {
    val courtColor = runCatching { Color(AndroidColor.parseColor(colorHex)) }
        .getOrDefault(EspaciosPalette.Steel)
    Canvas(modifier = modifier.clip(RoundedCornerShape(5.dp))) {
        drawRect(courtColor)
        val inset = 4.dp.toPx()
        val lineColor = Color.White.copy(alpha = 0.72f)
        drawRect(
            color = lineColor,
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            style = Stroke(width = 1.dp.toPx())
        )
        drawLine(
            color = lineColor,
            start = Offset(size.width / 2, inset),
            end = Offset(size.width / 2, size.height - inset),
            strokeWidth = 1.dp.toPx()
        )
        drawCircle(
            color = lineColor,
            radius = 4.dp.toPx(),
            center = Offset(size.width / 2, size.height / 2),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
