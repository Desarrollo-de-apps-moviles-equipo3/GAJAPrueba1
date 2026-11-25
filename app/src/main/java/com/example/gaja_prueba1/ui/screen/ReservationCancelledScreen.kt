package com.example.gaja_prueba1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ReservationCancelledScreen(
    reservation: ReservationResponse?,
    onViewCancelled: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = EspaciosPalette.Green,
            modifier = Modifier.size(68.dp)
        )
        Text(
            text = "Reserva cancelada",
            modifier = Modifier.padding(top = 18.dp),
            color = EspaciosPalette.Navy,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "La solicitud se canceló correctamente.",
            modifier = Modifier.padding(top = 7.dp),
            color = EspaciosPalette.Slate,
            textAlign = TextAlign.Center
        )

        if (reservation != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp),
                shape = RoundedCornerShape(12.dp),
                color = EspaciosPalette.White
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(reservation.espacio, color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(5.dp))
                    Text(formatCancelledDate(reservation.fecha), color = EspaciosPalette.Slate)
                    Text(reservation.horario, color = EspaciosPalette.Slate)
                }
            }
        }

        Button(
            onClick = onViewCancelled,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EspaciosPalette.Navy)
        ) {
            Text("Ver reservas canceladas")
        }
        TextButton(onClick = onHome) {
            Text("Volver al inicio", color = EspaciosPalette.Slate)
        }
    }
}

private fun formatCancelledDate(value: String): String = try {
    val spanishChile = Locale.forLanguageTag("es-CL")
    LocalDate.parse(value)
        .format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", spanishChile))
        .replaceFirstChar { it.uppercase(spanishChile) }
} catch (_: Exception) {
    value
}
