package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.AdminIncidencia
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaFilterChip
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.viewmodels.AdminIncidenciasViewModel
import com.example.gaja_prueba1.ui.viewmodels.IncidenciaFilter
import com.example.gaja_prueba1.ui.viewmodels.ReservationReferenceMode
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

/** Entrada de administración: lista persistida, registro nuevo y detalle con acción de resolver. */
@Composable
fun AdminIncidenciasScreen(
    userEmail: String,
    modifier: Modifier = Modifier,
    viewModel: AdminIncidenciasViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }
    BackHandler(enabled = state.creating || state.selected != null) { viewModel.back() }

    Box(modifier.fillMaxSize()) {
        when {
            state.creating -> NewIncidentForm(
                email = state.email,
                category = state.category,
                spaces = state.spaces,
                selectedSpaceId = state.selectedSpaceId,
                reservations = state.reservations,
                selectedReservationId = state.selectedReservationId,
                referenceMode = state.reservationReferenceMode,
                reservationDate = state.reservationDate,
                loadingReferences = state.loadingReferences,
                spacesError = state.spacesError,
                reservationsError = state.reservationsError,
                description = state.descripcion,
                error = state.error,
                saving = state.isSaving,
                onEmailChange = viewModel::onEmailChanged,
                onCategoryChange = viewModel::onCategoryChanged,
                onSpaceSelected = viewModel::onSpaceSelected,
                onReservationSelected = viewModel::onReservationSelected,
                onReferenceModeChange = viewModel::onReservationReferenceModeChanged,
                onReservationDateSelected = viewModel::onReservationDateSelected,
                onRetryReferences = viewModel::retryReferenceOptions,
                onDescriptionChange = viewModel::onDescriptionChanged,
                onSave = viewModel::create,
                onBack = viewModel::back
            )
            state.selected != null -> IncidentDetail(
                incident = state.selected!!,
                busy = state.resolvingId == state.selected!!.id,
                error = state.error,
                onBack = viewModel::back,
                onResolve = { viewModel.resolve(state.selected!!.id) }
            )
            else -> IncidentList(
                state = state,
                onFilter = viewModel::onFilterChanged,
                onOpen = viewModel::open,
                onCreate = { viewModel.openCreate(userEmail) },
                onRefresh = viewModel::refresh
            )
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

/** Alertas activas: resumen de incidencias pendientes del servicio, con acceso a su detalle. */
@Composable
fun AdminAlertsScreen(
    onOpenIncidents: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AdminIncidenciasViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val pending = state.incidencias.filterNot { it.resuelta }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Alertas", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
        GajaCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Column(Modifier.weight(1f)) {
                    Text("Incidencias pendientes", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (state.isLoading) "Actualizando…" else "${pending.size} por revisar",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = viewModel::refresh) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Actualizar alertas")
                }
            }
        }

        when {
            state.isLoading && state.incidencias.isEmpty() -> Box(
                Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            state.error != null && state.incidencias.isEmpty() -> Column(
                Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = viewModel::refresh) { Text("Reintentar") }
            }
            pending.isEmpty() -> Box(
                Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center
            ) {
                Text("No hay alertas pendientes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            else -> LazyColumn(
                Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(pending.take(5), key = { it.id }) { incident ->
                    IncidentCard(incident = incident, onClick = {
                        viewModel.open(incident.id)
                        onOpenIncidents()
                    })
                }
            }
        }

        GajaButton(text = "Ver incidencias", onClick = onOpenIncidents)
    }
}

@Composable
private fun IncidentList(
    state: com.example.gaja_prueba1.ui.viewmodels.AdminIncidenciasUiState,
    onFilter: (IncidenciaFilter) -> Unit,
    onOpen: (Long) -> Unit,
    onCreate: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Incidencias",
                Modifier.weight(1f),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
            )
            IconButton(onClick = onRefresh) {
                Icon(Icons.Filled.Refresh, contentDescription = "Actualizar incidencias")
            }
        }
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IncidenciaFilter.entries.forEach { filter ->
                val count = when (filter) {
                    IncidenciaFilter.PENDIENTES -> state.incidencias.count { !it.resuelta }
                    IncidenciaFilter.RESUELTAS -> state.incidencias.count { it.resuelta }
                    IncidenciaFilter.TODAS -> state.incidencias.size
                }
                GajaFilterChip(
                    text = "${filter.label} $count",
                    selected = state.filter == filter,
                    onClick = { onFilter(filter) }
                )
            }
        }

        when {
            state.isLoading && state.incidencias.isEmpty() -> Box(
                Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            state.error != null && state.incidencias.isEmpty() -> Column(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
                OutlinedButton(onClick = onRefresh) { Text("Reintentar") }
            }
            state.visible.isEmpty() -> Box(
                Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center
            ) { Text("No hay incidencias en esta categoría.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            else -> LazyColumn(
                Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.visible, key = { it.id }) { incident ->
                    IncidentCard(incident = incident, onClick = { onOpen(incident.id) })
                }
            }
        }

        GajaButton(text = "＋ Registrar incidencia", onClick = onCreate)
    }
}

@Composable
private fun IncidentCard(incident: AdminIncidencia, onClick: () -> Unit) {
    GajaCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    incident.categoria.ifBlank { "Incidencia" },
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                StatusChip(incident)
            }
            Text(incident.descripcion, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                "${incident.email} · ${displayDate(incident.creadaEn)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun IncidentDetail(
    incident: AdminIncidencia,
    busy: Boolean,
    error: String?,
    onBack: () -> Unit,
    onResolve: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ScreenTitle("Detalle de incidencia", onBack)
        GajaCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Incidencia #${incident.id}", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    StatusChip(incident)
                }
                DetailLine("Categoría o espacio", incident.categoria)
                DetailLine("Correo", incident.email)
                DetailLine("Fecha", displayDate(incident.creadaEn))
                DetailLine("Descripción", incident.descripcion)
            }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        if (!incident.resuelta) {
            GajaButton(
                text = if (busy) "Guardando…" else "Marcar como resuelta",
                onClick = onResolve,
                enabled = !busy,
            )
        } else {
            Text("Esta incidencia ya fue resuelta.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewIncidentForm(
    email: String,
    category: String?,
    spaces: List<Space>,
    selectedSpaceId: String?,
    reservations: List<AdminReservation>,
    selectedReservationId: Long?,
    referenceMode: ReservationReferenceMode,
    reservationDate: String?,
    loadingReferences: Boolean,
    spacesError: String?,
    reservationsError: String?,
    description: String,
    error: String?,
    saving: Boolean,
    onEmailChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onSpaceSelected: (String) -> Unit,
    onReservationSelected: (Long) -> Unit,
    onReferenceModeChange: (ReservationReferenceMode) -> Unit,
    onReservationDateSelected: (String) -> Unit,
    onRetryReferences: () -> Unit,
    onDescriptionChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var showDatePicker by remember { androidx.compose.runtime.mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenTitle("Nueva incidencia", onBack)
        Text(
            "Registra un problema para que quede guardado y aparezca en la lista de administración.",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DropdownSelector(
            label = "Categoría",
            value = category ?: "Selecciona una categoría",
            options = listOf("Canchas", "Reservas", "Cuenta", "Otro").map { it to it },
            onSelected = onCategoryChange
        )

        when (category) {
            "Canchas" -> {
                if (loadingReferences) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text("  Cargando canchas…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                spacesError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = onRetryReferences) { Text("Reintentar") }
                }
                DropdownSelector(
                    label = "Cancha relacionada",
                    value = spaces.firstOrNull { it.id == selectedSpaceId }?.name ?: "Selecciona una cancha",
                    options = spaces.map { it.id to it.name },
                    onSelected = onSpaceSelected
                )
                if (!loadingReferences && spaces.isEmpty() && spacesError == null) {
                    Text(
                        "No hay canchas disponibles para relacionar.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            "Reservas" -> {
                Text("¿Con qué se relaciona?", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReservationReferenceMode.entries.forEach { mode ->
                        GajaFilterChip(
                            text = mode.label,
                            selected = referenceMode == mode,
                            onClick = { onReferenceModeChange(mode) }
                        )
                    }
                }
                if (referenceMode == ReservationReferenceMode.REQUEST) {
                    if (loadingReferences) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("  Cargando solicitudes…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    reservationsError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = onRetryReferences) { Text("Reintentar") }
                    }
                    DropdownSelector(
                        label = "Solicitud de reserva",
                        value = reservations.firstOrNull { it.id == selectedReservationId }
                            ?.let(::reservationLabel) ?: "Selecciona una solicitud",
                        options = reservations.map { it.id to reservationLabel(it) },
                        onSelected = onReservationSelected
                    )
                    if (!loadingReferences && reservations.isEmpty() && reservationsError == null) {
                        Text(
                            "No hay solicitudes para elegir. Puedes relacionarla con un día en particular.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(reservationDate?.let(::formatSelectedDate) ?: "Seleccionar día")
                    }
                }
            }
        }

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Correo relacionado") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Describe el problema") },
            supportingText = { Text("${description.length}/1000 · mínimo 10 caracteres") },
            minLines = 4,
            maxLines = 7,
            modifier = Modifier.fillMaxWidth()
        )
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error)
        GajaButton(
            text = if (saving) "Guardando…" else "Guardar incidencia",
            onClick = onSave,
            enabled = !saving
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onReservationDateSelected(datePickerMillisToIso(it))
                        }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) { DatePicker(state = datePickerState, showModeToggle = false) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownSelector(
    label: String,
    value: String,
    options: List<Pair<T, String>>,
    onSelected: (T) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && options.isNotEmpty(),
        onExpandedChange = { expanded = it && options.isNotEmpty() }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            enabled = options.isNotEmpty(),
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.fillMaxWidth().menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded && options.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (option, optionLabel) ->
                DropdownMenuItem(
                    text = { Text(optionLabel) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun reservationLabel(reservation: AdminReservation): String =
    "#${reservation.id} · ${reservation.space} · ${reservation.date} · ${reservation.slot}"

private fun datePickerMillisToIso(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(java.util.Date(millis))

private fun formatSelectedDate(isoDate: String): String = try {
    val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(isoDate)
    if (date == null) isoDate else SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale("es", "CL")).format(date)
} catch (_: Exception) {
    isoDate
}

@Composable
private fun ScreenTitle(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
        }
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
    }
}

@Composable
private fun StatusChip(incident: AdminIncidencia) {
    val status = if (incident.resuelta) "Resuelta" else "Pendiente"
    val tone = if (incident.resuelta) GajaChipTone.Success else GajaChipTone.Info
    GajaStatusChip(status, tone = tone)
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value.ifBlank { "Sin información" }, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun displayDate(value: String): String {
    if (value.isBlank()) return "Fecha no disponible"
    val date = value.substringBefore('T')
    val time = value.substringAfter('T', "").take(5)
    return if (time.isBlank()) date else "$date · $time"
}
