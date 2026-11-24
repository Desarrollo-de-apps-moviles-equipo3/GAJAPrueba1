package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.BlockPlanner
import com.example.gaja_prueba1.model.BlocksRepository
import com.example.gaja_prueba1.model.BookingsRepository
import com.example.gaja_prueba1.model.RemoteBlocksRepository
import com.example.gaja_prueba1.model.RemoteBookingsRepository
import com.example.gaja_prueba1.model.RemoteSpacesRepository
import com.example.gaja_prueba1.model.SpacesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Agenda del administrador: es el mismo calendario del usuario (semana, mes, buscar espacio)
 * más la edición de un día para bloquear o desbloquear horarios en el servicio.
 */
class AdminAgendaViewModel(
    spacesRepository: SpacesRepository = RemoteSpacesRepository(),
    blocksRepository: BlocksRepository = RemoteBlocksRepository(),
    bookingsRepository: BookingsRepository = RemoteBookingsRepository()
) : CalendarViewModel(spacesRepository, blocksRepository, bookingsRepository) {

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    fun onOpenEditor(day: String) {
        val state = _uiState.value
        val space = state.selectedSpace ?: return
        if (day < state.todayIso) {
            _uiState.update { it.copy(message = "No se puede editar un día pasado") }
            return
        }
        _uiState.update {
            it.copy(editor = DayEditorState(day, BlockPlanner.initialChecked(space, day, it.blocks)))
        }
    }

    fun onEditorClose() = _uiState.update { it.copy(editor = null) }

    fun onToggleSlot(slot: String) = updateEditor {
        it.copy(checked = if (slot in it.checked) it.checked - slot else it.checked + slot)
    }

    fun onReasonChange(value: String) = updateEditor { it.copy(reason = value.take(80)) }

    private fun updateEditor(change: (DayEditorState) -> DayEditorState) {
        _uiState.update { state -> state.editor?.let { state.copy(editor = change(it)) } ?: state }
    }

    fun onEditorSave() {
        val state = _uiState.value
        val editor = state.editor ?: return
        val space = state.selectedSpace ?: return
        if (editor.isSaving) return

        val plan = BlockPlanner.plan(space, editor.day, editor.checked, state.blocks, state.reservations)
        if (!plan.hasChanges) {
            onEditorClose()
            return
        }

        updateEditor { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val reason = editor.reason.trim().ifBlank { null }
                plan.toCreate.forEach { blocksRepository.create(space.id, editor.day, it, reason) }
                plan.toDelete.forEach { blocksRepository.delete(it.id) }
                val blocks = blocksRepository.getAll()
                _uiState.update { it.copy(blocks = blocks, editor = null, message = "Disponibilidad actualizada") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_ESPACIOS", "No se pudo guardar la disponibilidad", e)
                // Si falló a la mitad, volvemos a pedir los bloqueos para mostrar lo que realmente quedó.
                val blocks = runCatching { blocksRepository.getAll() }.getOrNull()
                _uiState.update {
                    it.copy(
                        blocks = blocks ?: it.blocks,
                        editor = it.editor?.copy(isSaving = false),
                        message = "No pudimos guardar los cambios"
                    )
                }
            }
        }
    }
}
