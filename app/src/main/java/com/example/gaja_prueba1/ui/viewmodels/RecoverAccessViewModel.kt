package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.AccessRecoveryRepository
import com.example.gaja_prueba1.model.FakeAccessRecoveryRepository
import com.example.gaja_prueba1.model.RecoverAccessResult
import com.example.gaja_prueba1.model.RecoverAccessValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecoverAccessUiState(
    val identifier: String = "",
    val fieldError: String? = null,
    val isLoading: Boolean = false,
    val sent: Boolean = false,
    val connectionError: Boolean = false
)

class RecoverAccessViewModel : ViewModel() {
    private val repository: AccessRecoveryRepository = FakeAccessRecoveryRepository()

    private val _uiState = MutableStateFlow(RecoverAccessUiState())
    val uiState: StateFlow<RecoverAccessUiState> = _uiState.asStateFlow()

    fun onIdentifierChange(value: String) {
        _uiState.update { it.copy(identifier = value, fieldError = null, connectionError = false) }
    }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isLoading) return

        val error = RecoverAccessValidator.validate(current.identifier)
        if (error != null) {
            _uiState.update { it.copy(fieldError = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, connectionError = false) }
            when (repository.requestRecovery(current.identifier)) {
                RecoverAccessResult.Success ->
                    _uiState.update { it.copy(isLoading = false, sent = true) }
                RecoverAccessResult.NoConnection ->
                    _uiState.update { it.copy(isLoading = false, connectionError = true) }
            }
        }
    }
}
