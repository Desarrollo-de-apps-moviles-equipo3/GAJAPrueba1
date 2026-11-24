package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.model.RemoteReservationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MyReservationsUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class MyReservationsViewModel : ViewModel() {
    private val repository = RemoteReservationsRepository()

    private val _reservations =
        MutableStateFlow<List<ReservationResponse>>(emptyList())
    val reservations: StateFlow<List<ReservationResponse>> =
        _reservations.asStateFlow()

    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState.asStateFlow()

    fun loadReservations(email: String) {
        if (email.isBlank()) {
            _uiState.value = MyReservationsUiState(
                error = "No se encontró el correo de la sesión"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = MyReservationsUiState(isLoading = true)

            try {
                _reservations.value = repository.listarPorUsuario(email.trim())
                _uiState.value = MyReservationsUiState()
            } catch (_: Exception) {
                _uiState.value = MyReservationsUiState(
                    error = "No se pudieron cargar tus reservas. Revisa tu conexión."
                )
            }
        }
    }

    fun cancelReservation(id: Long, email: String) {
        viewModelScope.launch {
            _uiState.value = MyReservationsUiState(isLoading = true)

            try {
                repository.cancelarReserva(id, email)
                _reservations.value = repository.listarPorUsuario(email.trim())
                _uiState.value = MyReservationsUiState()
            } catch (_: Exception) {
                _uiState.value = MyReservationsUiState(
                    error = "No se pudo cancelar la reserva. Inténtalo de nuevo."
                )
            }
        }
    }
}