package com.example.gaja_prueba1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.data.remote.AlertResponse
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.AlertasViewModel

@Composable
fun AlertasScreen(
    email: String,
    onBack: () -> Unit,
    viewModel: AlertasViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(email) {
        viewModel.loadAlerts(email)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .padding(horizontal = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = EspaciosPalette.Navy)
            }
            Column {
                Text("Alertas", color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text("Novedades de tus reservas", color = EspaciosPalette.Slate, fontSize = 12.sp)
            }
        }

        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EspaciosPalette.Navy)
            }

            state.error != null -> OfflineErrorScreen(
                message = state.error.orEmpty(),
                onRetry = { viewModel.loadAlerts(email) },
                modifier = Modifier.weight(1f)
            )

            state.alerts.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Notifications,
                        contentDescription = null,
                        tint = EspaciosPalette.Slate,
                        modifier = Modifier.size(42.dp)
                    )
                    Text(
                        "No tienes alertas",
                        modifier = Modifier.padding(top = 10.dp),
                        color = EspaciosPalette.Navy,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Aquí aparecerán las novedades de tus reservas.",
                        modifier = Modifier.padding(top = 5.dp),
                        color = EspaciosPalette.Slate,
                        fontSize = 12.sp
                    )
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.alerts, key = AlertResponse::id) { alert ->
                    AlertCard(alert)
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: AlertResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EspaciosPalette.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(EspaciosPalette.SkyCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = EspaciosPalette.Navy,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(alert.mensaje, color = EspaciosPalette.Navy, fontSize = 13.sp)
                Text(
                    text = formatAlertDate(alert.creadaEn),
                    modifier = Modifier.padding(top = 5.dp),
                    color = EspaciosPalette.Slate,
                    fontSize = 11.sp
                )
            }
            if (!alert.leida) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EspaciosPalette.Orange)
                )
            }
        }
    }
}

private fun formatAlertDate(value: String): String =
    value.replace('T', ' ').substringBefore('.')
