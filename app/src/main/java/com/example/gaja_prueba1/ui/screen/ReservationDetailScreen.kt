package com.example.gaja_prueba1.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * "Detalle de reserva" a pantalla completa. Cubre las variantes del prototipo:
 * confirmada, pendiente, rechazada, cancelada, pasada y fuera de plazo.
 *
 * @param statusLabel estado que se muestra en la etiqueta (por ejemplo "Confirmada").
 * @param notice aviso bajo el detalle, como "Faltan menos de 2 horas: ya no puedes cancelar esta reserva."
 * @param cancelEnabled si la persona todavía puede cancelar; si no, el botón se ve apagado.
 */
@Composable
fun ReservationDetailScreen(
    reservation: ReservationResponse,
    statusLabel: String,
    notice: String?,
    cancelEnabled: Boolean,
    showCancel: Boolean,
    reservedBy: String,
    onBack: () -> Unit,
    onCancel: () -> Unit
) {
    BackHandler(onBack = onBack)

    val tone = when (statusLabel) {
        "Confirmada" -> GajaChipTone.Success
        "Pendiente" -> GajaChipTone.Info
        "Rechazada" -> GajaChipTone.Danger
        else -> GajaChipTone.Neutral
    }
    val code = reservationCode(reservation)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Volver", tint = EspaciosPalette.Steel)
            }
            Text(
                "Detalle de reserva",
                modifier = Modifier.padding(start = 6.dp),
                color = EspaciosPalette.Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 26.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        reservation.espacio,
                        modifier = Modifier.weight(1f),
                        color = EspaciosPalette.Navy,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    GajaStatusChip(statusLabel, tone = tone)
                }
                Text("Código $code", color = EspaciosPalette.Slate, fontSize = 13.sp)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(EspaciosPalette.White.copy(alpha = 0.55f))
                    .border(1.dp, EspaciosPalette.Beige, RoundedCornerShape(14.dp))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                DetailItem(Icons.Outlined.LocationOn, "Cancha", reservation.espacio)
                DetailItem(Icons.Outlined.CalendarMonth, "Fecha", formatLongDate(reservation.fecha))
                DetailItem(Icons.Outlined.Schedule, "Horario", reservation.horario)
                DetailItem(Icons.Outlined.ChatBubbleOutline, "Motivo", reservation.motivo)
                DetailItem(Icons.Outlined.Tag, "Código de reserva", code)
                DetailItem(Icons.Outlined.Person, "Reservado por", reservedBy)
                if (reservation.estado.equals("RECHAZADA", ignoreCase = true)) {
                    DetailItem(
                        Icons.Outlined.Info,
                        "Motivo del rechazo",
                        reservation.motivoRechazo?.takeIf { it.isNotBlank() } ?: "Sin motivo"
                    )
                }
            }

            notice?.let {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = EspaciosPalette.Danger, modifier = Modifier.size(16.dp))
                    Text(it, color = EspaciosPalette.Danger, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)) {
                GajaButton(text = "Volver a mis reservas", onClick = onBack)
                if (showCancel) {
                    // Fuera de plazo el botón se ve apagado y no hace nada, como en el prototipo.
                    GajaButton(
                        text = "Cancelar reserva",
                        onClick = onCancel,
                        enabled = cancelEnabled
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(icon, contentDescription = null, tint = EspaciosPalette.Navy, modifier = Modifier.size(22.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
            Text(label, color = EspaciosPalette.Slate, fontSize = 11.sp)
            Text(value, color = EspaciosPalette.Navy, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}

/** Código visible de la reserva, con el formato del prototipo: RSV-2026-003. */
internal fun reservationCode(reservation: ReservationResponse): String =
    "RSV-%s-%03d".format(Locale.ROOT, reservation.fecha.take(4).ifBlank { "0000" }, reservation.id)

/** "2026-11-30" → "Lunes, 30 de noviembre". Si la fecha no se puede leer, la deja como llegó. */
private fun formatLongDate(date: String): String = try {
    val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).parse(date)
    if (parsed == null) date
    else SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es", "CL")).format(parsed)
        .replaceFirstChar { it.titlecase(Locale("es", "CL")) }
} catch (_: Exception) {
    date
}