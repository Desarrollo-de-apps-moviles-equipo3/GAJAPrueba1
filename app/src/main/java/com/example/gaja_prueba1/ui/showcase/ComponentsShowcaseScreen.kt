package com.example.gaja_prueba1.ui.showcase

import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaSelector
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme

@Composable
fun ComponentsShowcaseScreen(modifier: Modifier = Modifier) {
    var role by rememberSaveable { mutableStateOf(0) }
    var user by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ESCUELA MILITAR",
            style = MaterialTheme.typography.titleMedium,
            color = EspaciosPalette.Navy
        )
        Text(
            text = "Sistema de Agendamiento de Canchas",
            style = MaterialTheme.typography.bodyMedium,
            color = EspaciosPalette.Steel
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Bienvenido",
            style = MaterialTheme.typography.headlineMedium,
            color = EspaciosPalette.Navy
        )
        Text(
            text = "Ingresa con tu cuenta institucional.",
            style = MaterialTheme.typography.bodyLarge,
            color = EspaciosPalette.Steel
        )

        GajaSelector(
            label = "Ingresar como",
            options = listOf("Cadete", "Oficial", "Oficial de semana", "Institución externa"),
            icons = listOf(
                Icons.Outlined.Person,
                Icons.Outlined.Shield,
                Icons.Outlined.AssignmentTurnedIn,
                Icons.Outlined.Apartment
            ),
            selectedIndex = role,
            onSelect = { role = it }
        )

        GajaTextField(
            value = user,
            onValueChange = { user = it },
            label = "Usuario",
            leadingIcon = Icons.Outlined.Person,
            modifier = Modifier.fillMaxWidth()
        )
        GajaTextField(
            value = password,
            onValueChange = { password = it },
            label = "Contraseña",
            leadingIcon = Icons.Outlined.Lock,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        GajaButton(
            text = "Iniciar sesión",
            onClick = { },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = "¿Olvidaste tu contraseña?",
            style = MaterialTheme.typography.bodyMedium,
            color = EspaciosPalette.Steel,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ComponentsShowcaseScreenPreview() {
    GAJAPrueba1Theme {
        ComponentsShowcaseScreen()
    }
}