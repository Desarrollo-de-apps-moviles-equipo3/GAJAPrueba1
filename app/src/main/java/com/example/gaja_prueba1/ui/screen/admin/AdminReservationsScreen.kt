package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.model.conflictWith
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaFilterChip
import com.example.gaja_prueba1.ui.viewmodels.AdminReservationsViewModel
import com.example.gaja_prueba1.ui.viewmodels.RejectFormState
import com.example.gaja_prueba1.ui.viewmodels.ReservationsTab

/**
 * Sección "Reservas" del administrador (tareas A4 y A5):
 * listas Pendientes / Aprobadas / Rechazadas, detalle de la solicitud (con aviso de
 * conflicto de horario), rechazo con motivo y pantallas de resultado.
 */
@Composable
fun AdminReservationsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminReservationsViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var historyQuery by rememberSaveable { mutableStateOf("") }
    var historyStatus by rememberSaveable { mutableStateOf("Todos") }
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
                conflict = if (selected.status == EstadoReserva.PENDIENTE)
                    selected.conflictWith(state.items[ReservationsTab.Pendientes].orEmpty())
                else null,
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
                historyQuery = historyQuery,
                onHistoryQueryChange = { historyQuery = it },
                historyStatus = historyStatus,
                onHistoryStatusChange = { historyStatus = it },
                onTabSelected = viewModel::onTabSelected,
                onOpen = viewModel::onOpenDetail,
                onRetry = viewModel::load
            )
        }

        state.rejectForm?.let { form ->
            RejectSheet(
                requester = state.selected?.userEmail ?: "el solicitante",
                form = form,
                onReasonChange = viewModel::onReasonChange,
                onConfirm = viewModel::onConfirmReject,
                onDismiss = viewModel::onCancelReject
            )
        }

        SnackbarHost(
            hostState = snackbarHost,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationsListView(
    tab: ReservationsTab,
    isLoading: Boolean,
    errorMessage: String?,
    counts: Map<ReservationsTab, Int>,
    items: List<AdminReservation>,
    historyQuery: String,
    onHistoryQueryChange: (String) -> Unit,
    historyStatus: String,
    onHistoryStatusChange: (String) -> Unit,
    onTabSelected: (ReservationsTab) -> Unit,
    onOpen: (AdminReservation) -> Unit,
    onRetry: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ScrollableTabRow(selectedTabIndex = tab.ordinal) {
            ReservationsTab.entries.forEach { entry ->
                Tab(
                    selected = entry == tab,
                    onClick = { onTabSelected(entry) },
                    text = { Text("${entry.label} (${counts[entry] ?: 0})") }
                )
            }
        }

        val filteredItems = if (tab == ReservationsTab.Historial) {
            items.filter { reservation ->
                reservation.userEmail.contains(historyQuery.trim(), ignoreCase = true) &&
                        (historyStatus == "Todos" || reservation.status.name == historyStatus)
            }.sortedByDescending { it.createdAt.orEmpty() }
        } else items

        if (tab == ReservationsTab.Historial) {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Historial completo", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Incluye las reservas que el usuario quitó de su lista.",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = historyQuery,
                    onValueChange = onHistoryQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Buscar por correo del usuario") }
                )
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Todos", "PENDIENTE", "APROBADA", "RECHAZADA", "CANCELADA").forEach { status ->
                        FilterChip(
                            selected = historyStatus == status,
                            onClick = { onHistoryStatusChange(status) },
                            label = {
                                Text(when (status) {
                                    "Todos" -> "Todos"
                                    "PENDIENTE" -> "Pendientes"
                                    "APROBADA" -> "Aprobadas"
                                    "RECHAZADA" -> "Rechazadas"
                                    else -> "Canceladas"
                                })
                            }
                        )
                    }
                }
            }
        }

        // Tirar hacia abajo recarga las listas (también funciona con la lista vacía).
        PullToRefreshBox(
            isRefreshing = isLoading && filteredItems.isNotEmpty(),
            onRefresh = onRetry,
            modifier = Modifier.fillMaxSize()
        ) {
            when {
                isLoading && filteredItems.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

                errorMessage != null -> Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(errorMessage, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    OutlinedButton(onClick = onRetry) { Text("Reintentar") }
                }

                filteredItems.isEmpty() -> Box(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    Alignment.Center
                ) {
                    Text(if (tab == ReservationsTab.Historial) "No hay registros que coincidan con la búsqueda" else "No hay reservas ${tab.label.lowercase()}")
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems, key = { it.id }) { reservation ->
                        GajaCard(modifier = Modifier.fillMaxWidth(), onClick = { onOpen(reservation) }) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(reservation.space, fontWeight = FontWeight.SemiBold)
                                Text("${reservation.date} · ${reservation.slot}")
                                Text(reservation.userEmail, style = MaterialTheme.typography.bodySmall)
                                Text("Estado: ${reservation.status.etiqueta}", style = MaterialTheme.typography.bodySmall)
                                if (reservation.hiddenForUser) {
                                    Text("Oculta para el usuario · registro conservado", style = MaterialTheme.typography.bodySmall)
                                }
                            }
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
    conflict: AdminReservation?,
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

        if (conflict != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.medium)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Column {
                    Text(
                        "Conflicto de horario",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        "RSV-${conflict.id} (${conflict.userEmail}) pide la misma cancha y hora.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

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

private val rejectReasons = listOf(
    "Horario bloqueado",
    "Espacio en mantención",
    "Conflicto con otra solicitud",
    "Fuera de las reglas",
    "Otro"
)

/** Hoja inferior para rechazar una solicitud: motivos rápidos + detalle obligatorio. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun RejectSheet(
    requester: String,
    form: RejectFormState,
    onReasonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var chip by rememberSaveable { mutableStateOf<String?>(null) }
    val missing = form.reason.isBlank()

    ModalBottomSheet(onDismissRequest = { if (!form.isSaving) onDismiss() }) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Rechazar solicitud", style = MaterialTheme.typography.titleLarge)
            Text(
                "El motivo se enviará a $requester.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Motivo (obligatorio)", style = MaterialTheme.typography.labelLarge)

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rejectReasons.forEach { reason ->
                    GajaFilterChip(
                        text = reason,
                        selected = chip == reason,
                        onClick = {
                            chip = reason
                            onReasonChange(if (reason == "Otro") "" else reason)
                        }
                    )
                }
            }

            OutlinedTextField(
                value = form.reason,
                onValueChange = onReasonChange,
                label = { Text("Detalle del motivo") },
                minLines = 3,
                isError = form.error != null || missing,
                supportingText = {
                    val msg = form.error ?: if (missing) "Escribe un motivo para continuar" else null
                    if (msg != null) Text(msg)
                },
                modifier = Modifier.fillMaxWidth()
            )

            GajaButton(
                text = "Confirmar rechazo",
                onClick = onConfirm,
                enabled = !missing,
                loading = form.isSaving
            )
            OutlinedButton(
                onClick = onDismiss,
                enabled = !form.isSaving,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Cancelar") }
        }
    }
}
