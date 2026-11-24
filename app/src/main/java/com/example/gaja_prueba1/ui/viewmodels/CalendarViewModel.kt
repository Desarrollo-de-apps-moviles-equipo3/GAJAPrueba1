package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.data.AppDatabase
import com.example.gaja_prueba1.data.ReservationEntity
import com.example.gaja_prueba1.data.remote.BloqueoDto
import com.example.gaja_prueba1.model.BlocksRepository
import com.example.gaja_prueba1.model.RemoteBlocksRepository
import com.example.gaja_prueba1.model.RemoteSpacesRepository
import com.example.gaja_prueba1.model.ReservationRepository
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.model.SpacesRepository
import com.example.gaja_prueba1.model.filterSpaces
import com.example.gaja_prueba1.validation.ReservationValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class CalendarMode(val label: String) { Semana("Semana"), Mes("Mes") }

data class CalendarUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val spaces: List<Space> = emptyList(),
    val blocks: List<BloqueoDto> = emptyList(),
    val reservations: List<ReservationEntity> = emptyList(),
    val query: String = "",
    val selectedSpaceId: String? = null,
    val mode: CalendarMode = CalendarMode.Semana,
    val weekOffset: Int = 0,
    val monthOffset: Int = 0,
    val selectedDay: String? = null,
    val nowMillis: Long = System.currentTimeMillis()
) {
    /** Solo los espacios habilitados: los deshabilitados no se pueden reservar. */
    val bookable: List<Space> get() = spaces.filter { it.available }
    val results: List<Space> get() = filterSpaces(bookable, query)
    val selectedSpace: Space? get() = bookable.firstOrNull { it.id == selectedSpaceId }
    val todayIso: String get() = ReservationValidator.toIsoDate(nowMillis)
}

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val spacesRepository: SpacesRepository = RemoteSpacesRepository()
    private val blocksRepository: BlocksRepository = RemoteBlocksRepository()
    private val reservationRepository = ReservationRepository(
        AppDatabase.get(application).reservationDao()
    )

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        // Las reservas del usuario viven en Room: el calendario se actualiza solo cuando reserva algo.
        viewModelScope.launch {
            reservationRepository.observeAll().collect { list ->
                _uiState.update { it.copy(reservations = list) }
            }
        }
    }

    /** Pide espacios y bloqueos al servicio. La pantalla lo llama cada vez que se abre el Calendario. */
    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isLoading = it.spaces.isEmpty(), errorMessage = null, nowMillis = System.currentTimeMillis())
            }
            try {
                val (spaces, blocks) = coroutineScope {
                    val s = async { spacesRepository.getSpaces() }
                    val b = async { blocksRepository.getAll() }
                    s.await() to b.await()
                }
                _uiState.update { state ->
                    val stillValid = state.selectedSpaceId
                        ?.takeIf { id -> spaces.any { it.id == id && it.available } }
                    state.copy(
                        isLoading = false,
                        spaces = spaces,
                        blocks = blocks,
                        selectedSpaceId = stillValid ?: spaces.firstOrNull { it.available }?.id
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_ESPACIOS", "No se pudo cargar el calendario", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No pudimos cargar la disponibilidad. Inténtalo de nuevo.")
                }
            }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onSelectSpace(id: String) = _uiState.update { it.copy(selectedSpaceId = id, selectedDay = null) }

    fun onModeChange(mode: CalendarMode) = _uiState.update { it.copy(mode = mode) }

    fun onPrevious() = _uiState.update {
        if (it.mode == CalendarMode.Semana) it.copy(weekOffset = it.weekOffset - 1)
        else it.copy(monthOffset = it.monthOffset - 1, selectedDay = null)
    }

    fun onNext() = _uiState.update {
        if (it.mode == CalendarMode.Semana) it.copy(weekOffset = it.weekOffset + 1)
        else it.copy(monthOffset = it.monthOffset + 1, selectedDay = null)
    }

    fun onSelectDay(isoDate: String) = _uiState.update { it.copy(selectedDay = isoDate) }
}
