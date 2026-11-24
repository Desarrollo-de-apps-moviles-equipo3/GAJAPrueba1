package com.example.gaja_prueba1.ui.screen.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.BlockPlanner
import com.example.gaja_prueba1.model.SlotStatus
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.model.SpaceAvailability
import com.example.gaja_prueba1.ui.screen.CalendarContent
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.AdminAgendaViewModel
import com.example.gaja_prueba1.ui.viewmodels.CalendarUiState
import com.example.gaja_prueba1.ui.viewmodels.DayEditorState
import com.example.gaja_prueba1.validation.ReservationValidator

/**
 * Pestaña "Agenda" del administrador. Muestra la disponibilidad real de cada espacio (semana o mes).
 * Al tocar un día (o "Editar disponibilidad") se abre el editor para bloquear o desbloquear horarios.
 */
@Composable
fun AdminAgendaScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminAgendaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) { viewModel.refresh() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        CalendarContent(
            state = state,
            onQueryChange = viewModel::onQueryChange,
            onSelectSpace = viewModel::onSelectSpace,
            onModeChange = viewModel::onModeChange,
            onPrevious = viewModel::onPrevious,
            onNext = viewModel::onNext,
            onSelectDay = viewModel::onSelectDay,
            onRetry = viewModel::refresh,
            onReserveClick = null,
            onDayClick = viewModel::onOpenEditor
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    val editor = state.editor
    val space = state.selectedSpace
    if (editor != null && space != null) {
        DayEditorDialog(
            space = space,
            editor = editor,
            state = state,
            onToggleSlot = viewModel::onToggleSlot,
            onReasonChange = viewModel::onReasonChange,
            onSave = viewModel::onEditorSave,
            onClose = viewModel::onEditorClose
        )
    }
}

@Composable
private fun DayEditorDialog(
    space: Space,
    editor: DayEditorState,
    state: CalendarUiState,
    onToggleSlot: (String) -> Unit,
    onReasonChange: (String) -> Unit,
    onSave: () -> Unit,
    onClose: () -> Unit
) {
    val plan = BlockPlanner.plan(space, editor.day, editor.checked, state.blocks, state.reservations)
    val dayText = ReservationValidator.formatDate(SpaceAvailability.parseIso(editor.day))
        .replaceFirstChar { it.uppercase() }

    AlertDialog(
        onDismissRequest = { if (!editor.isSaving) onClose() },
        title = { Text("Editar disponibilidad") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${space.name} · $dayText",
                    fontWeight = FontWeight.SemiBold,
                    color = EspaciosPalette.Navy
                )
                Text(
                    text = "Marca los horarios que quieres bloquear. Los desmarcados quedan disponibles.",
                    style = MaterialTheme.typography.bodySmall,
                    color = EspaciosPalette.Slate
                )

                ReservationValidator.SLOTS.forEach { slot ->
                    val checked = slot in editor.checked
                    val reserved = state.reservations.any {
                        it.space == space.name && it.date == editor.day && it.timeSlot.trim() == slot
                    }
                    val reason = SpaceAvailability.blockFor(space, editor.day, slot, state.blocks)
                        ?.reason?.takeIf { it.isNotBlank() }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !editor.isSaving) { onToggleSlot(slot) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onToggleSlot(slot) },
                            enabled = !editor.isSaving
                        )
                        Column {
                            Text(slot, fontSize = 14.sp)
                            val note = listOfNotNull(
                                if (reserved) "Tiene una reserva" else null,
                                reason?.let { "Motivo: $it" }
                            ).joinToString(" · ")
                            if (note.isNotEmpty()) {
                                Text(note, fontSize = 11.sp, color = EspaciosPalette.Slate)
                            }
                        }
                    }
                }

                if (plan.toCreate.isNotEmpty()) {
                    OutlinedTextField(
                        value = editor.reason,
                        onValueChange = onReasonChange,
                        label = { Text("Motivo del bloqueo (opcional)") },
                        singleLine = true,
                        enabled = !editor.isSaving,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (plan.affected.isNotEmpty()) {
                    AffectedReservations(plan.affected.map { "${it.timeSlot} · ${it.reason}" })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = plan.hasChanges && !editor.isSaving) {
                Text(if (editor.isSaving) "Guardando…" else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onClose, enabled = !editor.isSaving) { Text("Cancelar") }
        }
    )
}

@Composable
private fun AffectedReservations(lines: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Reservas afectadas (${lines.size})",
            fontWeight = FontWeight.SemiBold,
            color = EspaciosPalette.Danger
        )
        lines.forEach { Text("• $it", fontSize = 12.sp, color = EspaciosPalette.Danger) }
        Text(
            text = "Estas reservas no se cancelan solas: avisa a quienes reservaron.",
            fontSize = 11.sp,
            color = EspaciosPalette.Slate
        )
    }
}
