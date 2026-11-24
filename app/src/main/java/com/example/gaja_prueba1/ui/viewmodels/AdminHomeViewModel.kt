package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.AdminHomeRepository
import com.example.gaja_prueba1.model.AdminHomeSummary
import com.example.gaja_prueba1.model.RemoteAdminHomeRepository
import com.example.gaja_prueba1.validation.ReservationValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminHomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val summary: AdminHomeSummary? = null,
    val dateLabel: String = ""
)

class AdminHomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AdminHomeRepository = RemoteAdminHomeRepository()

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** La pantalla lo llama al abrirse, para que los contadores estén al día. */
    fun load() {
        val now = System.currentTimeMillis()
        val dateLabel = ReservationValidator.formatDate(now, "EEEE, d 'de' MMMM 'de' yyyy")
            .replaceFirstChar { it.uppercase() }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, dateLabel = dateLabel) }
            try {
                val summary = repository.load(ReservationValidator.toIsoDate(now))
                _uiState.update { it.copy(isLoading = false, summary = summary) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_RESERVAS", "No se pudo cargar el Inicio del administrador", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No pudimos cargar el resumen. Inténtalo de nuevo.")
                }
            }
        }
    }
}