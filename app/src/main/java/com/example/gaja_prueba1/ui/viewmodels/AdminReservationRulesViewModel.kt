package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.AdminReportsRepository
import com.example.gaja_prueba1.model.ReservationRules
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminReservationRulesUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val rules: ReservationRules? = null,
    val maxDaysText: String = "",
    val cancellationHoursText: String = "",
    val message: String? = null
)

class AdminReservationRulesViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AdminReportsRepository = AdminReportsRepository()
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AdminReservationRulesUiState())
    val uiState: StateFlow<AdminReservationRulesUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val rules = repository.rules()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        rules = rules,
                        maxDaysText = rules.maxDaysAhead.toString(),
                        cancellationHoursText = rules.cancellationLimitHours.toString()
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "No pudimos cargar las reglas vigentes.")
                }
            }
        }
    }

    fun onMaxDaysChange(value: String) = _uiState.update { it.copy(maxDaysText = value.filter(Char::isDigit), message = null) }
    fun onCancellationHoursChange(value: String) = _uiState.update { it.copy(cancellationHoursText = value.filter(Char::isDigit), message = null) }

    fun save() {
        val state = _uiState.value
        val days = state.maxDaysText.toIntOrNull()
        val cancellation = state.cancellationHoursText.toIntOrNull()
        if (days == null || days !in 0..365 || cancellation == null || cancellation !in 0..48) {
            _uiState.update { it.copy(error = "Revisa los valores: días 0–365 y cancelación 0–48 horas.") }
            return
        }
        val current = state.rules ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, message = null) }
            try {
                val updated = repository.updateRules(
                    current.copy(
                        maxDaysAhead = days,
                        cancellationLimitHours = cancellation
                    )
                )
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        rules = updated,
                        maxDaysText = updated.maxDaysAhead.toString(),
                        cancellationHoursText = updated.cancellationLimitHours.toString(),
                        message = "Reglas guardadas"
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update { it.copy(isSaving = false, error = "No pudimos guardar las reglas.") }
            }
        }
    }
}
