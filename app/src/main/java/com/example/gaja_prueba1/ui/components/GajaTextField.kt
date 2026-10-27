package com.example.gaja_prueba1.ui.components

import android.graphics.drawable.Icon
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.theme.InterFamily

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

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(fontFamily = InterFamily, fontSize = 15.sp),
        label = { Text(label, fontFamily = InterFamily) },
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null) } },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = if (isPassword) {
            KeyboardOptions(keyboardType = KeyboardType.Password)
        } else {
            KeyboardOptions.Default
        },
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it, fontFamily = InterFamily) } },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = EspaciosPalette.White,
            unfocusedContainerColor = EspaciosPalette.White,
            errorContainerColor = EspaciosPalette.White,
            focusedBorderColor = EspaciosPalette.Navy,
            unfocusedBorderColor = EspaciosPalette.Beige,
            errorBorderColor = EspaciosPalette.Danger,
            focusedTextColor = EspaciosPalette.Navy,
            unfocusedTextColor = EspaciosPalette.Navy,
            errorTextColor = EspaciosPalette.Navy,
            focusedLabelColor = EspaciosPalette.Navy,
            unfocusedLabelColor = EspaciosPalette.Slate,
            errorLabelColor = EspaciosPalette.Danger,
            cursorColor = EspaciosPalette.Navy,
            focusedLeadingIconColor = EspaciosPalette.Navy,
            unfocusedLeadingIconColor = EspaciosPalette.Slate,
            errorLeadingIconColor = EspaciosPalette.Danger,
            errorTrailingIconColor = EspaciosPalette.Danger,
            errorSupportingTextColor = EspaciosPalette.Danger
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun GajaTextFieldPreview() {
    GAJAPrueba1Theme {
        var user by remember {mutableStateOf("")}
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