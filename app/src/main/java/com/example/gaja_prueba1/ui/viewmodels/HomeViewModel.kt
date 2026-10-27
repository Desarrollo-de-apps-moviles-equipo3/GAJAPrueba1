package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.FakeSpacesRepository
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.model.SpacesRepository
import com.example.gaja_prueba1.model.filterSpaces
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val allSpaces: List<Space> = emptyList(),
    val query: String = "",
    val upcomingReservations: Int = 0,
    val pastReservations: Int = 0
) {
    val visibleSpaces: List<Space> get() = filterSpaces(allSpaces, query)
    val availableSpaces: Int get() = allSpaces.count { it.available }
}

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SpacesRepository = FakeSpacesRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadSpaces()
    }

    fun loadSpaces() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val spaces = repository.getSpaces()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        allSpaces = spaces,
                        // TODO(Gabriel, tarea 14): tomar estos dos contadores de ReservationsRepository
                        upcomingReservations = 2,
                        pastReservations = 1
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "No pudimos cargar los espacios. Revisa tu conexión e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }
}
