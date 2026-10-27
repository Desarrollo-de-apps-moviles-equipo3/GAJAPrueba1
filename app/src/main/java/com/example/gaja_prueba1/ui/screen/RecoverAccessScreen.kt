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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.RecoverAccessUiState
import com.example.gaja_prueba1.ui.viewmodels.RecoverAccessViewModel

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
            .background(EspaciosPalette.Cream)
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver",
                tint = EspaciosPalette.Steel
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Recuperar acceso",
            color = EspaciosPalette.Navy,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Ingresa tu usuario o correo institucional y te enviaremos las instrucciones para crear una nueva contraseña.",
            color = EspaciosPalette.Slate,
            fontSize = 14.sp,
            lineHeight = 20.sp
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
    OutlinedTextField(
        value = state.identifier,
        onValueChange = onIdentifierChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Usuario o correo institucional") },
        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
        singleLine = true,
        isError = state.fieldError != null,
        supportingText = { if (state.fieldError != null) Text(state.fieldError) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = EspaciosPalette.White,
            unfocusedContainerColor = EspaciosPalette.White,
            focusedBorderColor = EspaciosPalette.Navy,
            unfocusedBorderColor = EspaciosPalette.Beige,
            errorBorderColor = EspaciosPalette.Danger,
            errorContainerColor = EspaciosPalette.White,
            focusedTextColor = EspaciosPalette.Navy,
            unfocusedTextColor = EspaciosPalette.Navy,
            focusedLabelColor = EspaciosPalette.Navy,
            unfocusedLabelColor = EspaciosPalette.Slate,
            cursorColor = EspaciosPalette.Navy,
            focusedLeadingIconColor = EspaciosPalette.Navy,
            unfocusedLeadingIconColor = EspaciosPalette.Slate
        )
    )

    if (state.connectionError) {
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Sin conexión. Revisa tu internet e inténtalo de nuevo.",
            color = EspaciosPalette.Danger,
            fontSize = 13.sp,
            modifier = Modifier
                .fillMaxWidth()
                .background(EspaciosPalette.DangerSoft, RoundedCornerShape(10.dp))
                .padding(12.dp)
        )
    }

    Spacer(Modifier.height(20.dp))

    Button(
        onClick = onSubmit,
        enabled = !state.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EspaciosPalette.Navy,
            contentColor = EspaciosPalette.White,
            disabledContainerColor = EspaciosPalette.Navy.copy(alpha = 0.6f),
            disabledContentColor = EspaciosPalette.White
        )
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = EspaciosPalette.White,
                strokeWidth = 2.dp
            )
        } else {
            Text("Enviar instrucciones", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SentMessage(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EspaciosPalette.White, RoundedCornerShape(16.dp))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(EspaciosPalette.GreenSoft, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = EspaciosPalette.Green,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = "Revisa tu correo",
            color = EspaciosPalette.Navy,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Si el usuario existe, te enviamos las instrucciones. Puede tardar unos minutos.",
            color = EspaciosPalette.Slate,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            textAlign = TextAlign.Center
        )
        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EspaciosPalette.Navy,
                contentColor = EspaciosPalette.White
            )
        ) {
            Text("Volver a iniciar sesión", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessPreview() {
    RecoverAccessContent(RecoverAccessUiState(), {}, {}, {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessErrorPreview() {
    RecoverAccessContent(
        RecoverAccessUiState(identifier = "ab", fieldError = "Debe tener al menos 3 caracteres"),
        {}, {}, {}
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessNoConnectionPreview() {
    RecoverAccessContent(
        RecoverAccessUiState(identifier = "sinconexion", connectionError = true),
        {}, {}, {}
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun RecoverAccessSentPreview() {
    RecoverAccessContent(RecoverAccessUiState(identifier = "galcala", sent = true), {}, {}, {})
}
