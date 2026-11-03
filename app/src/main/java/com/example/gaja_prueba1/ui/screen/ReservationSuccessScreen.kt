package com.example.gaja_prueba1.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.validation.ReservationValidator

@Composable
fun ReservationSuccessScreen(
    spaceName: String,
    dateMillis: Long,
    slot: String,
    onViewReservations: () -> Unit,
    onHome: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn()) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = EspaciosPalette.Orange,
                modifier = Modifier.size(72.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text("¡Reserva confirmada!", fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = EspaciosPalette.Navy)
        Text("Tu solicitud ha sido registrada correctamente.", color = EspaciosPalette.Slate, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Text(spaceName, modifier = Modifier.fillMaxWidth(), color = EspaciosPalette.Navy)
        Text(ReservationValidator.formatDate(dateMillis), modifier = Modifier.fillMaxWidth(), color = EspaciosPalette.Slate)
        Text(slot, modifier = Modifier.fillMaxWidth(), color = EspaciosPalette.Slate)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onViewReservations,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EspaciosPalette.Navy)
        ) { Text("Ver mis reservas") }
        Button(
            onClick = onHome,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = EspaciosPalette.NavyTop)
        ) { Text("Volver al inicio") }
    }
}