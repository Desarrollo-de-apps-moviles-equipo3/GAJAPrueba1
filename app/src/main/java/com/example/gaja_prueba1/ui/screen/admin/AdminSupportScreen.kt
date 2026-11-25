package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaFilterChip
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.viewmodels.*

/** Soporte del admin: lista, detalle de la solicitud y edición del contacto visible (A5). */
@Composable
fun AdminSupportScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminSupportViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }
    BackHandler(enabled = state.editingContact || state.selected != null) { viewModel.onBack() }

    Box(modifier.fillMaxSize()) {
        val selected = state.selected
        when {
            state.editingContact -> ContactEditor(state, viewModel)
            selected != null -> TicketDetail(selected, state, viewModel)
            else -> TicketList(state, viewModel)
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun ScreenHeader(title: String, onBack: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        }
        Text(title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold))
    }
}

private fun TicketStatus.tone() = when (this) {
    TicketStatus.NUEVA -> GajaChipTone.Neutral
    TicketStatus.EN_REVISION -> GajaChipTone.Info
    TicketStatus.RESUELTA -> GajaChipTone.Success
}

// ---------------- Lista ----------------

@Composable
private fun TicketList(state: AdminSupportUiState, vm: AdminSupportViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScreenHeader("Soporte")

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TicketStatus.entries.forEach { status ->
                val count = state.tickets.count { it.status == status }
                GajaFilterChip(
                    text = "${status.plural} $count",
                    selected = state.filter == status,
                    onClick = { vm.onFilter(status) }
                )
            }
        }

        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (state.visible.isEmpty()) {
                item {
                    Text(
                        "No hay solicitudes en esta categoría.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.visible, key = { it.code }) { ticket ->
                GajaCard(Modifier.fillMaxWidth(), onClick = { vm.onOpen(ticket.code) }) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                ticket.title,
                                Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            GajaStatusChip(ticket.status.label, tone = ticket.status.tone())
                        }
                        Text(
                            "${ticket.author} · ${ticket.code}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "${ticket.whenText} · ${ticket.summary}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = vm::onEditContact,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.Filled.Email, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Editar contacto visible")
        }
    }
}

// ---------------- Detalle ----------------

private val quickReplies = listOf("Revisaremos tu caso", "Ya está resuelto")

@Composable
private fun TicketDetail(ticket: SupportTicket, state: AdminSupportUiState, vm: AdminSupportViewModel) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("Solicitud ${ticket.code}", onBack = vm::onBack)
        Text(ticket.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GajaStatusChip(ticket.status.label, tone = ticket.status.tone())
            Text(
                "${ticket.author} · ${ticket.whenText}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        GajaCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Mensaje",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(ticket.message, style = MaterialTheme.typography.bodyMedium)
            }
        }

        ticket.lastReply?.let { reply ->
            GajaCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Tu última respuesta",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(reply, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (ticket.status != TicketStatus.RESUELTA) {
            Text("Tu respuesta", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = state.reply,
                onValueChange = vm::onReplyChange,
                placeholder = { Text("Escribe tu respuesta…") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Respuestas rápidas",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                quickReplies.forEach { quick ->
                    GajaFilterChip(text = quick, selected = false, onClick = { vm.onReplyChange(quick) })
                }
            }

            GajaButton(
                text = "Enviar respuesta",
                onClick = vm::onSendReply,
                enabled = state.reply.isNotBlank()
            )
            OutlinedButton(
                onClick = vm::onResolve,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Marcar como resuelta") }
        }
    }
}

// ---------------- Contacto visible ----------------

@Composable
private fun ContactEditor(state: AdminSupportUiState, vm: AdminSupportViewModel) {
    val draft = state.contactDraft
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ScreenHeader("Información de contacto", onBack = vm::onBack)

        OutlinedTextField(
            value = draft.email,
            onValueChange = { vm.onContactChange(draft.copy(email = it)) },
            label = { Text("Correo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = draft.phone,
            onValueChange = { vm.onContactChange(draft.copy(phone = it)) },
            label = { Text("Teléfono") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = draft.hours,
            onValueChange = { vm.onContactChange(draft.copy(hours = it)) },
            label = { Text("Horario de atención") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        GajaCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Así lo ven los usuarios",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                PreviewRow(Icons.Filled.Email, draft.email)
                PreviewRow(Icons.Filled.Phone, draft.phone)
                PreviewRow(Icons.Filled.Schedule, draft.hours)
            }
        }

        state.contactError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))
        GajaButton(text = "Guardar cambios", onClick = vm::onSaveContact)
    }
}

@Composable
private fun PreviewRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}