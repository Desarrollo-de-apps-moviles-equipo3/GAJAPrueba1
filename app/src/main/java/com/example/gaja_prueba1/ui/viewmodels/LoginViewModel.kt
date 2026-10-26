package com.example.gaja_prueba1.ui.viewmodels


import com.example.gaja_prueba1.model.AuthRepository
import com.example.gaja_prueba1.model.LoginResult


import com.example.gaja_prueba1.validation.LoginValidator

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
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    private val _loginMessage = MutableStateFlow("")

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()
    val loginMessage: StateFlow<String> = _loginMessage.asStateFlow()


    fun onEmailChange(email: String) {
        _email.value = email
        _emailError.value = null          // limpia el error al escribir
    }

    fun onPasswordChange(password: String) {
        _password.value = password
        _passwordError.value = null
    }

    fun onLoginClick() {
        // 1. Validar
        val emailErr = LoginValidator.validateEmail(_email.value)
        val passErr = LoginValidator.validatePassword(_password.value)
        _emailError.value = emailErr
        _passwordError.value = passErr

        // 2. Si hay algún error, no llamar al login
        if (emailErr != null || passErr != null) return

        // 3. Todo válido: seguir con tu lógica de siempre
        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            _loginMessage.value = ""

            when (val result = repository.loginUser(_email.value.trim(), _password.value)) {
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