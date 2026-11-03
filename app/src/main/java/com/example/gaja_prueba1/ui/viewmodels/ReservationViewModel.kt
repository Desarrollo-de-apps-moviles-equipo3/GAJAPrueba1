package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.data.AppDatabase
import com.example.gaja_prueba1.data.ReservationEntity
import com.example.gaja_prueba1.model.ReservationRepository
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.validation.ReservationValidator
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReservationUiState(
    val space: Space? = null,
    val dateMillis: Long? = null,
    val selectedSlot: String? = null,
    val reason: String = "",
    val dateError: String? = null,
    val slotError: String? = null,
    val reasonError: String? = null,
    val unavailableSlots: Set<String> = emptySet(),
    val isLoadingSlots: Boolean = false,
    val slotsError: String? = null,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saved: Boolean = false
)

class ReservationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ReservationRepository(
        AppDatabase.get(application).reservationDao()
    )

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()
    private var availabilityJob: Job? = null

    fun start(space: Space) {
        if (_uiState.value.space?.id != space.id || _uiState.value.saved) {
            availabilityJob?.cancel()
            availabilityJob = null
            _uiState.value = ReservationUiState(space = space)
        }
    }

    fun onDateSelected(millis: Long) {
        availabilityJob?.cancel()
        _uiState.update {
            it.copy(
                dateMillis = millis,
                selectedSlot = null,
                dateError = null,
                slotError = null,
                saveError = null,
                unavailableSlots = emptySet(),
                isLoadingSlots = true,
                slotsError = null
            )
        }

        val space = _uiState.value.space
        if (space == null) {
            _uiState.update { it.copy(isLoadingSlots = false) }
            return
        }

        val isoDate = ReservationValidator.toIsoDate(millis)
        availabilityJob = viewModelScope.launch {
            repository.observeReservationsOn(isoDate)
                .catch {
                    _uiState.update { current ->
                        if (current.dateMillis != millis || current.space?.id != space.id) {
                            current
                        } else {
                            current.copy(
                                isLoadingSlots = false,
                                slotsError = "No se pudieron cargar los horarios"
                            )
                        }
                    }
                }
                .collect { reservations ->
                    val occupied = reservations
                        .filter { it.space == space.name }
                        .map { it.timeSlot }
                        .toSet()

                    _uiState.update { current ->
                        if (current.dateMillis != millis || current.space?.id != space.id) {
                            current
                        } else {
                            current.copy(
                                unavailableSlots = occupied,
                                isLoadingSlots = false,
                                slotsError = null
                            )
                        }
                    }
                }
        }
    }

    fun validateDateSelection(): Boolean {
        val error = ReservationValidator.validateDate(_uiState.value.dateMillis)
        _uiState.update { it.copy(dateError = error) }
        return error == null
    }

    fun onSlotSelected(slot: String) {
        val current = _uiState.value
        if (slot in current.unavailableSlots || current.isLoadingSlots || current.slotsError != null) {
            return
        }

        _uiState.update {
            it.copy(selectedSlot = slot, slotError = null, saveError = null)
        }
    }

    fun onReasonChanged(value: String) {
        _uiState.update {
            it.copy(reason = value, reasonError = null, saveError = null)
        }
    }

    fun submit() {
        val state = _uiState.value
        if (state.isSaving || state.isLoadingSlots || state.slotsError != null) return

        val dateError = ReservationValidator.validateDate(state.dateMillis)
        val slotError = when {
            state.selectedSlot == null -> "Selecciona un horario"
            state.selectedSlot in state.unavailableSlots -> "Ese horario ya está ocupado"
            else -> null
        }
        val reasonError = ReservationValidator.validateReason(state.reason)

        if (dateError != null || slotError != null || reasonError != null) {
            _uiState.update {
                it.copy(
                    dateError = dateError,
                    slotError = slotError,
                    reasonError = reasonError
                )
            }
            return
        }

        val space = state.space ?: return
        val dateMillis = state.dateMillis ?: return
        val slot = state.selectedSlot ?: return
        val isoDate = ReservationValidator.toIsoDate(dateMillis)

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            try {
                val sameDay = repository.reservationsOn(isoDate)

                if (sameDay.size >= ReservationValidator.MAX_PER_DAY) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            dateError = "Ese día ya alcanzó el máximo de reservas"
                        )
                    }
                    return@launch
                }

                if (sameDay.any { it.space == space.name && it.timeSlot == slot }) {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            slotError = "Ese horario ya está ocupado"
                        )
                    }
                    return@launch
                }

                repository.save(
                    ReservationEntity(
                        space = space.name,
                        date = isoDate,
                        timeSlot = slot,
                        reason = state.reason.trim()
                    )
                )

                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = "No se pudo guardar. Inténtalo otra vez."
                    )
                }
            }
        }
    }
}
