package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.data.AppDatabase
import com.example.gaja_prueba1.model.AuthRepository
import com.example.gaja_prueba1.model.LoginResult
import com.example.gaja_prueba1.model.SessionRepository
import com.example.gaja_prueba1.validation.LoginValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val authRepository = AuthRepository()

    private val sessionRepository = SessionRepository(
        AppDatabase.get(application).sessionDao()
    )

    private val roles = listOf(
        "Cadete",
        "Oficial",
        "Oficial de semana",
        "Institución externa"
    )

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _selectedRole = MutableStateFlow(0)
    val selectedRole: StateFlow<Int> = _selectedRole.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isError = MutableStateFlow(false)
    val isError: StateFlow<Boolean> = _isError.asStateFlow()

    private val _loginMessage = MutableStateFlow("")
    val loginMessage: StateFlow<String> = _loginMessage.asStateFlow()

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<String?>(null)
    val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

    private val _loggedUser = MutableStateFlow<String?>(null)
    val loggedUser: StateFlow<String?> = _loggedUser.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
        _emailError.value = null
        _loginMessage.value = ""
    }

    fun onPasswordChange(value: String) {
        _password.value = value
        _passwordError.value = null
        _loginMessage.value = ""
    }

    fun onRoleChange(index: Int) {
        if (index in roles.indices) {
            _selectedRole.value = index
            _loginMessage.value = ""
        }
    }

    fun onLoginClick() {
        if (_isLoading.value) return

        val emailValue = _email.value.trim()
        val passwordValue = _password.value

        val emailValidation =
            LoginValidator.validateEmail(emailValue)

        val passwordValidation =
            LoginValidator.validatePassword(passwordValue)

        _emailError.value = emailValidation
        _passwordError.value = passwordValidation

        if (
            emailValidation != null ||
            passwordValidation != null
        ) return

        viewModelScope.launch {
            _isLoading.value = true
            _isError.value = false
            _loginMessage.value = ""

            try {
                val role = roles[_selectedRole.value]

                when (
                    val result = authRepository.loginUser(
                        emailValue,
                        passwordValue,
                        role
                    )
                ) {
                    is LoginResult.Success -> {
                        sessionRepository.saveSession(
                            emailValue,
                            result.role
                        )

                        _loginMessage.value = "¡Bienvenido!"
                        _loggedUser.value = emailValue
                    }

                    is LoginResult.Error -> {
                        _loginMessage.value =
                            result.exception.message
                                ?: "Error de autenticación"

                        _isError.value = true
                    }
                }

            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _loginMessage.value =
                    "No se pudo guardar la sesión. Inténtalo nuevamente."

                _isError.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun consumeLoginSuccess() {
        _loggedUser.value = null
    }
}