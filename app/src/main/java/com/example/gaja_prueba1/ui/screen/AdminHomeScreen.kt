package com.example.gaja_prueba1.ui.screen.admin

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.AdminHomeItem
import com.example.gaja_prueba1.model.initialsOf
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaStatCard
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.viewmodels.AdminHomeViewModel

/**
 * Inicio del administrador (según el Figma): saludo, cuatro contadores,
 * "Pendientes" y accesos rápidos.
 * Los contadores de perfiles y ayuda salen como "—" hasta que existan esos servicios.
 */
@Composable
fun AdminHomeScreen(
    userName: String,
    modifier: Modifier = Modifier,
    onSeeAllReservations: () -> Unit = {},
    onBlockSchedule: () -> Unit = {},
    onCreateUser: () -> Unit = {},
    onReports: () -> Unit = {},
    viewModel: AdminHomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Se recarga cada vez que se abre el Inicio.
    LaunchedEffect(Unit) { viewModel.load() }

    val summary = state.summary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Saludo, campana y avatar
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(state.dateLabel, style = MaterialTheme.typography.bodySmall)
                Text(
                    "Hola, ${userName.trim().split(" ").firstOrNull().orEmpty()}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(Icons.Filled.Notifications, contentDescription = "Notificaciones")
            Spacer(Modifier.size(12.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                    Text(initialsOf(userName), fontWeight = FontWeight.Bold)
                }
            }
        }

        when {
            state.isLoading && summary == null -> Box(
                Modifier.fillMaxWidth().height(200.dp),
                Alignment.Center
            ) { CircularProgressIndicator() }

            state.errorMessage != null && summary == null -> Column(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.errorMessage.orEmpty(), textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
            }

            summary != null -> {
                // Contadores (2 x 2)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GajaStatCard(
                        value = summary.pendingReservations.toString(),
                        label = "Reservas por aprobar",
                        modifier = Modifier.weight(1f),
                        highlight = summary.pendingReservations > 0
                    )
                    GajaStatCard(
                        value = "—",
                        label = "Perfiles por aprobar",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    GajaStatCard(
                        value = summary.openIncidents?.toString() ?: "—",
                        label = "Incidencias abiertas",
                        modifier = Modifier.weight(1f)
                    )
                    GajaStatCard(
                        value = "—",
                        label = "Ayuda sin responder",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Pendientes
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Pendientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onSeeAllReservations) { Text("Ver todas") }
                }
                if (summary.items.isEmpty()) {
                    Text("No hay solicitudes pendientes", style = MaterialTheme.typography.bodyMedium)
                } else {
                    summary.items.forEach { item ->
                        PendingRow(item, onClick = onSeeAllReservations)
                    }
                }
            }
        }

        // Accesos rápidos
        Text(
            "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickAccess("Bloquear horario", Icons.Filled.Lock, onBlockSchedule, Modifier.weight(1f))
            QuickAccess("Crear usuario", Icons.Filled.Add, onCreateUser, Modifier.weight(1f))
            QuickAccess("Reportes", Icons.Filled.DateRange, onReports, Modifier.weight(1f))
        }
    }
}

@Composable
private fun PendingRow(item: AdminHomeItem, onClick: () -> Unit) {
    GajaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "${item.reservation.space} · ${item.whenLabel}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(item.reservation.userEmail, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.size(8.dp))
            GajaStatusChip(
                text = if (item.hasConflict) "Conflicto" else "Pendiente",
                tone = if (item.hasConflict) GajaChipTone.Danger else GajaChipTone.Info
            )
        }
    }
}

@Composable
private fun QuickAccess(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GajaCard(modifier = modifier, onClick = onClick) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null)
            Text(label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium)
        }
    }
}