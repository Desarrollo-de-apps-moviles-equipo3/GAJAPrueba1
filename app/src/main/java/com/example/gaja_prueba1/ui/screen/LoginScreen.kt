package com.example.gaja_prueba1.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaSelector
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit = {},
    viewModel: LoginViewModel = viewModel()
) {
    val loggedUser by viewModel.loggedUser.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val selectedRole by viewModel.selectedRole.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val loginMessage by viewModel.loginMessage.collectAsState()
    val isError by viewModel.isError.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()

    LaunchedEffect(loggedUser) {
        val user = loggedUser

        if (user != null) {
            onLoginSuccess(user)
            viewModel.consumeLoginSuccess()
        }
    }

    LoginForm(
        email = email,
        password = password,
        selectedRole = selectedRole,
        isLoading = isLoading,
        loginMessage = loginMessage,
        isError = isError,
        emailError = emailError,
        passwordError = passwordError,
        onRoleChange = viewModel::onRoleChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onLoginClick = viewModel::onLoginClick
    )
}

@Composable
private fun LoginForm(
    email: String,
    password: String,
    selectedRole: Int,
    isLoading: Boolean,
    loginMessage: String,
    isError: Boolean,
    emailError: String?,
    passwordError: String?,
    onRoleChange: (Int) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ESCUELA MILITAR",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Sistema de Agendamiento de Canchas",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Ingresa con tu cuenta institucional.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )

        GajaSelector(
            label = "Ingresar como",
            options = listOf(
                "Cadete",
                "Oficial",
                "Oficial de semana",
                "Institución externa"
            ),
            icons = listOf(
                Icons.Outlined.Person,
                Icons.Outlined.Shield,
                Icons.Outlined.AssignmentTurnedIn,
                Icons.Outlined.Apartment
            ),
            selectedIndex = selectedRole,
            onSelect = onRoleChange
        )

        GajaTextField(
            value = email,
            onValueChange = onEmailChange,
            label = "Correo electrónico",
            leadingIcon = Icons.Outlined.Person,
            errorText = emailError,
            modifier = Modifier.fillMaxWidth()
        )

        GajaTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Contraseña",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            errorText = passwordError,
            modifier = Modifier.fillMaxWidth()
        )

        // Animación entre el botón normal y el estado de carga.
        Crossfade(
            targetState = isLoading,
            label = "loginButtonLoading"
        ) { loading ->

            GajaButton(
                text = if (loading) {
                    "Validando..."
                } else {
                    "Iniciar sesión"
                },
                onClick = onLoginClick,
                loading = loading,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // El mensaje de respuesta aparece y desaparece animadamente.
        AnimatedVisibility(
            visible = loginMessage.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = loginMessage,
                color = if (isError) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Text(
            text = "¿Olvidaste tu contraseña?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    GAJAPrueba1Theme {
        LoginForm(
            email = "",
            password = "",
            selectedRole = 0,
            isLoading = false,
            loginMessage = "",
            isError = false,
            emailError = null,
            passwordError = null,
            onRoleChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onLoginClick = {}
        )
    }
}