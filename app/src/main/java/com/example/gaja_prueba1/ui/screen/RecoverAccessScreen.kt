package com.example.gaja_prueba1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.theme.extendedColors
import com.example.gaja_prueba1.ui.viewmodels.RecoverAccessUiState
import com.example.gaja_prueba1.ui.viewmodels.RecoverAccessViewModel
import androidx.compose.material.icons.filled.Error

@Composable
fun RecoverAccessScreen(
    onBack: () -> Unit,
    viewModel: RecoverAccessViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    RecoverAccessContent(
        state = state,
        onIdentifierChange = viewModel::onIdentifierChange,
        onSubmit = viewModel::onSubmit,
        onBack = onBack
    )
}

@Composable
fun RecoverAccessContent(
    state: RecoverAccessUiState,
    onIdentifierChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Recuperar acceso",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Ingresa tu usuario o correo institucional y te enviaremos las instrucciones para crear una nueva contraseña.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(28.dp))

        if (state.sent) {
            SentMessage(onBack = onBack)
        } else {
            RecoverForm(state, onIdentifierChange, onSubmit)
        }
    }
}

@Composable
private fun RecoverForm(
    state: RecoverAccessUiState,
    onIdentifierChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLowest

    OutlinedTextField(
        value = state.identifier,
        onValueChange = onIdentifierChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Usuario o correo institucional") },
        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
        trailingIcon = {
            if (state.fieldError != null) {
                Icon(
                    Icons.Filled.Error,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        singleLine = true,
        isError = state.fieldError != null,
        supportingText = { if (state.fieldError != null) Text(state.fieldError) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            errorContainerColor = containerColor
        )
    )

    if (state.connectionError) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Sin conexión. Revisa tu internet e inténtalo de nuevo.",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium)
                .padding(12.dp)
        )
    }

    Spacer(Modifier.height(20.dp))

    GajaButton(
        text = "Enviar instrucciones",
        onClick = onSubmit,
        loading = state.isLoading
    )
}

@Composable
private fun SentMessage(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest, MaterialTheme.shapes.large)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(MaterialTheme.extendedColors.successContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.extendedColors.success,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "Revisa tu correo",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = "Si el usuario existe, te enviamos las instrucciones. Puede tardar unos minutos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        GajaButton(
            text = "Volver a iniciar sesión",
            onClick = onBack
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessPreview() {
    GAJAPrueba1Theme {
        RecoverAccessContent(RecoverAccessUiState(), {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessErrorPreview() {
    GAJAPrueba1Theme {
        RecoverAccessContent(
            RecoverAccessUiState(identifier = "ab", fieldError = "Debe tener al menos 3 caracteres"),
            {}, {}, {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessNoConnectionPreview() {
    GAJAPrueba1Theme {
        RecoverAccessContent(
            RecoverAccessUiState(identifier = "sinconexion", connectionError = true),
            {}, {}, {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessSentPreview() {
    GAJAPrueba1Theme {
        RecoverAccessContent(RecoverAccessUiState(identifier = "galcala", sent = true), {}, {}, {})
    }
}