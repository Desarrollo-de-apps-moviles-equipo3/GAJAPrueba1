package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.viewmodels.AdminReservationsViewModel
import com.example.gaja_prueba1.ui.viewmodels.ReservationsTab

/**
 * Sección "Reservas" del administrador (tarea A4):
 * listas Pendientes / Aprobadas / Rechazadas, detalle de la solicitud,
 * rechazo con motivo y pantallas de resultado.
 */
@Composable
fun AdminReservationsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminReservationsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHost.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    BackHandler(enabled = state.result != null) { viewModel.onResultClose() }
    BackHandler(enabled = state.result == null && state.selected != null) { viewModel.onCloseDetail() }

    Box(modifier = modifier.fillMaxSize()) {
        val result = state.result
        val selected = state.selected
        when {
            result != null -> DecisionResultView(
                reservation = result.reservation,
                onClose = viewModel::onResultClose
            )

            selected != null -> ReservationDetailView(
                reservation = selected,
                isApproving = state.isApproving,
                onBack = viewModel::onCloseDetail,
                onApprove = viewModel::onApprove,
                onReject = viewModel::onStartReject
            )

            else -> ReservationsListView(
                tab = state.tab,
                isLoading = state.isLoading,
                errorMessage = state.errorMessage,
                counts = ReservationsTab.entries.associateWith { state.items[it].orEmpty().size },
                items = state.current,
                onTabSelected = viewModel::onTabSelected,
                onOpen = viewModel::onOpenDetail,
                onRetry = viewModel::load
            )
        }

        state.rejectForm?.let { form ->
            AlertDialog(
                onDismissRequest = { if (!form.isSaving) viewModel.onCancelReject() },
                title = { Text("Rechazar solicitud") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Cuéntale a la persona por qué no se aprueba su reserva.")
                        GajaTextField(
                            value = form.reason,
                            onValueChange = viewModel::onReasonChange,
                            label = "Motivo del rechazo",
                            errorText = form.error
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = viewModel::onConfirmReject, enabled = !form.isSaving) {
                        Text(if (form.isSaving) "Enviando…" else "Rechazar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onCancelReject, enabled = !form.isSaving) {
                        Text("Cancelar")
                    }
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun ReservationsListView(
    tab: ReservationsTab,
    isLoading: Boolean,
    errorMessage: String?,
    counts: Map<ReservationsTab, Int>,
    items: List<AdminReservation>,
    onTabSelected: (ReservationsTab) -> Unit,
    onOpen: (AdminReservation) -> Unit,
    onRetry: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = tab.ordinal) {
            ReservationsTab.entries.forEach { entry ->
                Tab(
                    selected = entry == tab,
                    onClick = { onTabSelected(entry) },
                    text = { Text("${entry.label} (${counts[entry] ?: 0})") }
                )
            }
        }

        when {
            isLoading && items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            errorMessage != null -> Column(
                Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(errorMessage, textAlign = TextAlign.Center)
                Spacer(Modifier.height(16.dp))
                OutlinedButton(onClick = onRetry) { Text("Reintentar") }
            }

            items.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("No hay reservas ${tab.label.lowercase()}")
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items, key = { it.id }) { reservation ->
                    GajaCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpen(reservation) }) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(reservation.space, fontWeight = FontWeight.SemiBold)
                            Text("${reservation.date} · ${reservation.slot}")
                            Text(reservation.userEmail, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReservationDetailView(
    reservation: AdminReservation,
    isApproving: Boolean,
    onBack: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Volver") }
        Text("Detalle de la solicitud", style = MaterialTheme.typography.titleLarge)

        GajaCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Espacio", reservation.space)
                DetailRow("Fecha", reservation.date)
                DetailRow("Horario", reservation.slot)
                DetailRow("Solicitante", reservation.userEmail)
                DetailRow("Motivo", reservation.reason)
                DetailRow("Estado", reservation.status.etiqueta)
                if (reservation.status == EstadoReserva.RECHAZADA) {
                    DetailRow("Motivo del rechazo", reservation.rejectionReason ?: "Sin motivo")
                }
            }
        }

        if (reservation.status == EstadoReserva.PENDIENTE) {
            Spacer(Modifier.weight(1f))
            GajaButton(text = "Aprobar", onClick = onApprove, loading = isApproving)
            OutlinedButton(
                onClick = onReject,
                enabled = !isApproving,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Rechazar") }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DecisionResultView(reservation: AdminReservation, onClose: () -> Unit) {
    val approved = reservation.status == EstadoReserva.APROBADA
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (approved) "Solicitud aprobada" else "Solicitud rechazada",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${reservation.space} · ${reservation.date} · ${reservation.slot}",
            textAlign = TextAlign.Center
        )
        if (!approved && !reservation.rejectionReason.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text("Motivo: ${reservation.rejectionReason}", textAlign = TextAlign.Center)
        }
        Spacer(Modifier.height(24.dp))
        GajaButton(text = "Volver a solicitudes", onClick = onClose)
    }
}