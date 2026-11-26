package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.BlocksRepository
import com.example.gaja_prueba1.model.RemoteBlocksRepository
import com.example.gaja_prueba1.model.RemoteReservationsRepository
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.model.ReservationRules
import com.example.gaja_prueba1.validation.ReservationValidator
import com.example.gaja_prueba1.data.remote.CreateReservationRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar


data class ReservationUiState(
    val space: Space? = null,
    val userEmail: String = "",
    val dateMillis: Long? = null,
    val selectedSlot: String? = null,
    val selectedStartHour: Int? = null,
    val selectedEndHour: Int? = null,
    val rules: ReservationRules = ReservationRules(7, 2),
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

class ReservationViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: RemoteReservationsRepository = RemoteReservationsRepository(),
    // Los horarios que el administrador bloqueó viven en el servicio de espacios.
    private val blocksRepository: BlocksRepository = RemoteBlocksRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ReservationUiState())
    val uiState: StateFlow<ReservationUiState> = _uiState.asStateFlow()
    private var availabilityJob: Job? = null

    fun start(space: Space, userEmail: String) {
        if (
            _uiState.value.space?.id != space.id ||
            _uiState.value.userEmail != userEmail ||
            _uiState.value.saved
        ) {
            availabilityJob?.cancel()
            availabilityJob = null
            _uiState.value = ReservationUiState(
                space = space,
                userEmail = userEmail
            )
            viewModelScope.launch {
                runCatching { repository.reglas() }.onSuccess { rules ->
                    _uiState.update { current ->
                        if (current.space?.id == space.id && current.userEmail == userEmail) {
                            current.copy(rules = rules)
                        } else current
                    }
                }
            }
        }
    }

    fun onDateSelected(millis: Long) {
        availabilityJob?.cancel()

        _uiState.update {
            it.copy(
                dateMillis = millis,
                selectedSlot = null,
                selectedStartHour = null,
                selectedEndHour = null,
                dateError = null,
                slotError = null,
                saveError = null,
                unavailableSlots = emptySet(),
                isLoadingSlots = true,
                slotsError = null
            )
        }

        val space = _uiState.value.space ?: return
        val isoDate = ReservationValidator.toIsoDate(millis)

        availabilityJob = viewModelScope.launch {
            try {
                // Un horario no se puede elegir si ya lo reservaron (servicio de reservas)
                // o si el administrador lo bloqueó (servicio de espacios).
                val occupied = coroutineScope {
                    val reserved = async { repository.horariosOcupados(isoDate, space.name) }
                    val blocked = async { blockedSlots(space, isoDate) }
                    Pair(reserved.await() + blocked.await(), repository.reglas())
                }

                _uiState.update { current ->
                    if (current.dateMillis != millis || current.space?.id != space.id) {
                        current
                    } else {
                        current.copy(
                            unavailableSlots = occupied.first,
                            rules = occupied.second,
                            isLoadingSlots = false,
                            slotsError = null
                        )
                    }
                }
            } catch (_: Exception) {
                _uiState.update { current ->
                    if (current.dateMillis != millis || current.space?.id != space.id) {
                        current
                    } else {
                        current.copy(
                            isLoadingSlots = false,
                            slotsError = "No se pudieron cargar los horarios. Revisa tu conexión."
                        )
                    }
                }
            }
        }
    }

    private suspend fun blockedSlots(space: Space, isoDate: String): Set<String> =
        blocksRepository.getAll()
            .filter { it.spaceId == space.id && it.date == isoDate }
            .map { it.timeSlot.trim() }
            .toSet()

    fun validateDateSelection(): Boolean {
        val current = _uiState.value
        val error = ReservationValidator.validateDate(current.dateMillis, maxDaysAhead = current.rules.maxDaysAhead)
        _uiState.update { it.copy(dateError = error) }
        return error == null
    }

    fun onTimeHourSelected(hour: Int) {
        val current = _uiState.value
        if (current.isLoadingSlots || current.slotsError != null) return

        val startHour = current.selectedStartHour
        if (startHour == null || (current.selectedEndHour != null && hour <= startHour)) {
            if (hour >= current.rules.closingHour || maxAvailableDuration(current, hour) < 1) return
            _uiState.update {
                it.copy(
                    selectedStartHour = hour,
                    selectedEndHour = null,
                    selectedSlot = null,
                    slotError = null,
                    saveError = null
                )
            }
            return
        }

        if (hour <= startHour) {
            if (hour < current.rules.closingHour && maxAvailableDuration(current, hour) > 0) {
                _uiState.update {
                    it.copy(
                        selectedStartHour = hour,
                        selectedEndHour = null,
                        selectedSlot = null,
                        slotError = null,
                        saveError = null
                    )
                }
            }
            return
        }
        val duration = hour - startHour
        if (duration > maxAvailableDuration(current, startHour)) {
            _uiState.update { it.copy(slotError = "Ese rango no está disponible") }
            return
        }

        _uiState.update {
            it.copy(
                selectedEndHour = hour,
                selectedSlot = ReservationValidator.formatHourSlot(startHour, duration),
                slotError = null,
                saveError = null
            )
        }
    }

    fun onResetTimeSelection() {
        _uiState.update {
            it.copy(
                selectedStartHour = null,
                selectedEndHour = null,
                selectedSlot = null,
                slotError = null,
                saveError = null
            )
        }
    }

    private fun maxAvailableDuration(state: ReservationUiState, startHour: Int): Int {
        val selectedDate = state.dateMillis ?: return 0
        val now = Calendar.getInstance()
        val reservationDay = Calendar.getInstance().apply { timeInMillis = selectedDate }
        val today = Calendar.getInstance()
        val isToday = reservationDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                reservationDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
        if (isToday && startHour <= now.get(Calendar.HOUR_OF_DAY)) return 0
        val closingRemaining = state.rules.closingHour - startHour
        val upperBound = closingRemaining
        return (1..upperBound).takeWhile { duration ->
            val candidate = ReservationValidator.formatHourSlot(startHour, duration)
            state.unavailableSlots.none { ReservationValidator.overlaps(candidate, it) }
        }.lastOrNull() ?: 0
    }

    fun onReasonChanged(value: String) {
        _uiState.update {
            it.copy(reason = value, reasonError = null, saveError = null)
        }
    }

    fun submit() {
        val state = _uiState.value

        if (state.isSaving || state.isLoadingSlots || state.slotsError != null) return

        val dateError = ReservationValidator.validateDate(state.dateMillis, maxDaysAhead = state.rules.maxDaysAhead)
        val selectedStartHour = state.selectedStartHour
        val selectedEndHour = state.selectedEndHour
        val slotError = when {
            selectedStartHour == null || selectedEndHour == null || state.selectedSlot == null -> "Selecciona la hora inicial y la hora final"
            selectedEndHour <= selectedStartHour ||
                    selectedEndHour - selectedStartHour > maxAvailableDuration(state, selectedStartHour) ->
                "Ese rango ya no está disponible"
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

        val request = CreateReservationRequest(
            usuarioEmail = state.userEmail.trim(),
            espacio = space.name,
            fecha = ReservationValidator.toIsoDate(dateMillis),
            horario = slot,
            motivo = state.reason.trim()
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveError = null) }

            try {
                repository.crearReserva(request)
                _uiState.update { it.copy(isSaving = false, saved = true) }
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        saveError = "No se pudo guardar la reserva. Revisa tu conexión e inténtalo otra vez."
                    )
                }
            }
        }
    }
}
