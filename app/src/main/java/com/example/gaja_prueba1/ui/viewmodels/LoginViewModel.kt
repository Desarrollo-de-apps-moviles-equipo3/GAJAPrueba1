package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.model.AuthRepository
import com.example.gaja_prueba1.model.LoginResult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class LoginViewModel : ViewModel() {
    private val repository = AuthRepository()
    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password:StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    private val _loginMessage = MutableStateFlow("")
    val loginMessage: StateFlow<String> = _loginMessage.asStateFlow()


    fun onEmailChange(email: String) {
        _email.value = email
    }

    fun onPasswordChange(password: String) {
        _password.value = password
    }

    fun onLoginClick() {
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false

            when (val result = repository.loginUser(_email.value, _password.value)) {
                is LoginResult.Success -> {
                    _loginMessage.value = "BIENVENIDO!!"
                    _isError.value = false
                }
                is LoginResult.Error -> {
                    _loginMessage.value = "Error de autenticación"
                    _isError.value = true
                }
            }
            _isLoading.value = false
        }
    }
}