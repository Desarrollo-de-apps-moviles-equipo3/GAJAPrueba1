package com.example.gaja_prueba1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme

@Composable
fun GajaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    errorText: String? = null,
    isPassword: Boolean = false
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLowest

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        trailingIcon = if (isPassword || errorText != null) {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (errorText != null) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    if (isPassword) {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                            )
                        }
                    }
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            androidx.compose.ui.text.input.VisualTransformation.None
        },
        keyboardOptions = if (isPassword) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions.Default
        },
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            errorContainerColor = containerColor
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun GajaTextFieldPreview() {
    GAJAPrueba1Theme {
        var user by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            GajaTextField(
                value = user,
                onValueChange = { user = it },
                label = "Usuario",
                leadingIcon = Icons.Filled.Person
            )
            GajaTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña",
                isPassword = true
            )
            GajaTextField(
                value = email,
                onValueChange = { email = it },
                label = "Correo",
                errorText = if (email.contains("@") && email.contains(".")) null else "Correo no válido"
            )
        }
    }
}