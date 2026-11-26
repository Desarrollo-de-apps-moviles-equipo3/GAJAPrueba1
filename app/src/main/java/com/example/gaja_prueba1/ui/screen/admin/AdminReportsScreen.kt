package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.model.UsageReportRow
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.viewmodels.AdminReportsViewModel
import com.example.gaja_prueba1.ui.viewmodels.UsageReportType

@Composable
fun AdminReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminReportsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedUser by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.load() }
    BackHandler(enabled = selectedUser != null) { selectedUser = null }

    Box(modifier.fillMaxSize()) {
        val user = selectedUser
        if (user != null) {
            val approved = state.reservations.count {
                it.userEmail.equals(user, ignoreCase = true) && it.status == EstadoReserva.APROBADA
            }
            val rejected = state.reservations.count {
                it.userEmail.equals(user, ignoreCase = true) && it.status == EstadoReserva.RECHAZADA
            }
            UserReportDetail(
                email = user,
                approved = approved,
                rejected = rejected,
                onBack = { selectedUser = null }
            )
        } else {
            ReportList(
                state = state,
                onTypeSelected = viewModel::selectType,
                onUserSelected = { selectedUser = it },
                onRetry = viewModel::load
            )
        }
    }
}

@Composable
private fun ReportList(
    state: com.example.gaja_prueba1.ui.viewmodels.AdminReportsUiState,
    onTypeSelected: (UsageReportType) -> Unit,
    onUserSelected: (String) -> Unit,
    onRetry: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reportes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        if (state.type == UsageReportType.ESPACIOS) {
            Text(
                "Revisa cuántas solicitudes de cada recinto fueron aprobadas o rechazadas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UsageReportType.entries.forEach { type ->
                FilterChip(
                    selected = state.type == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(type.label) }
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryCard(
                label = "Aprobadas",
                count = state.totalApproved,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            SummaryCard(
                label = "Rechazadas",
                count = state.totalRejected,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
        }

        when {
            state.isLoading -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Column(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error.orEmpty(), textAlign = TextAlign.Center)
                OutlinedButton(onClick = onRetry) { Text("Reintentar") }
            }
            state.rows.isEmpty() -> Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Text("Aún no hay reservas aprobadas o rechazadas para mostrar.", textAlign = TextAlign.Center)
            }
            else -> LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.rows, key = { it.label }) { row ->
                    UsageRow(
                        row = row,
                        isUser = state.type == UsageReportType.USUARIOS,
                        onClick = { if (state.type == UsageReportType.USUARIOS) onUserSelected(row.label) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(label: String, count: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    GajaCard(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(count.toString(), style = MaterialTheme.typography.headlineSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun UsageRow(row: UsageReportRow, isUser: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .then(if (isUser) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    row.label,
                    Modifier.weight(1f),
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (isUser) Text("›", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Aprobadas ${row.approvedReservations}", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                Text("Rechazadas ${row.rejectedReservations}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun UserReportDetail(
    email: String,
    approved: Int,
    rejected: Int,
    onBack: () -> Unit
) {
    val approvedColor = MaterialTheme.colorScheme.primary
    val rejectedColor = MaterialTheme.colorScheme.error
    val total = approved + rejected
    val approvedPercent = if (total == 0) 0 else (approved * 100 / total)
    val rejectedPercent = if (total == 0) 0 else 100 - approvedPercent

    Column(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("← Volver a reportes") }
        Text("Reservas del usuario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        Text(email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Text(
            "Distribución de solicitudes decididas",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (total == 0) {
            Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                Text("Este usuario todavía no tiene solicitudes aprobadas o rechazadas.", textAlign = TextAlign.Center)
            }
        } else {
            Canvas(Modifier.size(230.dp).padding(12.dp)) {
                val approvedSweep = 360f * approved / total
                drawArc(
                    color = approvedColor,
                    startAngle = -90f,
                    sweepAngle = approvedSweep,
                    useCenter = true
                )
                drawArc(
                    color = rejectedColor,
                    startAngle = -90f + approvedSweep,
                    sweepAngle = 360f - approvedSweep,
                    useCenter = true
                )
            }

            GajaCard(Modifier.fillMaxWidth()) {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LegendRow("Aprobadas", approved, approvedPercent, approvedColor)
                    LegendRow("Rechazadas", rejected, rejectedPercent, rejectedColor)
                    Spacer(Modifier.height(1.dp))
                    Text("Total: $total solicitudes decididas", fontWeight = FontWeight.SemiBold)
                }
            }
            Text(
                "Las solicitudes pendientes y canceladas no se incluyen en este gráfico.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LegendRow(
    label: String,
    count: Int,
    percent: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.size(12.dp).background(color, CircleShape))
        Text(label, Modifier.weight(1f))
        Text("$count · $percent%", fontWeight = FontWeight.SemiBold)
    }
}
