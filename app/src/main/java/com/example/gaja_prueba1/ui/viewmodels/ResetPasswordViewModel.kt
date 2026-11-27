package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.PasswordResetRepository
import com.example.gaja_prueba1.model.RemotePasswordResetRepository
import com.example.gaja_prueba1.model.ResetPasswordResult
import com.example.gaja_prueba1.model.ResetPasswordValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResetPasswordUiState(
    val email: String = "",
    val code: String = "",
    val password: String = "",
    val confirm: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val done: Boolean = false
)

class ResetPasswordViewModel(
    private val repository: PasswordResetRepository = RemotePasswordResetRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResetPasswordUiState())
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onCodeChange(value: String) = _uiState.update { it.copy(code = value, error = null) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value, error = null) }
    fun onConfirmChange(value: String) = _uiState.update { it.copy(confirm = value, error = null) }

    fun onSubmit() {
        val current = _uiState.value
        if (current.isLoading) return

        val error = ResetPasswordValidator.validate(current.email, current.code, current.password, current.confirm)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.reset(current.email, current.code, current.password)) {
                ResetPasswordResult.Success ->
                    _uiState.update { it.copy(isLoading = false, done = true, password = "", confirm = "") }
                is ResetPasswordResult.Error ->
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
            }
        }
    }
}
