package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.data.remote.AlertResponse
import com.example.gaja_prueba1.model.RemoteAlertasRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AlertasUiState(
    val alerts: List<AlertResponse> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AlertasViewModel : ViewModel() {
    private val repository = RemoteAlertasRepository()

    private val _uiState = MutableStateFlow(AlertasUiState())
    val uiState: StateFlow<AlertasUiState> = _uiState.asStateFlow()

    fun loadAlerts(email: String) {
        if (email.isBlank()) {
            _uiState.value = AlertasUiState(error = "No se encontró el correo de la sesión.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val alerts = repository.listarPorUsuario(email.trim())
                _uiState.value = AlertasUiState(alerts = alerts)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No pudimos cargar tus alertas. Revisa tu conexión e inténtalo de nuevo."
                )
            }
        }
    }
}
