package com.example.gaja_prueba1.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.viewmodels.AdminReservationRulesViewModel

@Composable
fun AdminReservationRulesScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminReservationRulesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.load() }

    Column(
        modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Reglas de reserva", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "Condiciones que se deben cumplir para reservar y cancelar.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
            state.rules == null && state.error != null -> Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(state.error.orEmpty(), textAlign = TextAlign.Center)
                OutlinedButton(onClick = viewModel::load) { Text("Reintentar") }
            }
            state.rules != null -> RulesList(
                state = state,
                onMaxDaysChange = viewModel::onMaxDaysChange,
                onCancellationHoursChange = viewModel::onCancellationHoursChange,
                onSave = viewModel::save
            )
        }
    }
}

@Composable
private fun RulesList(
    state: com.example.gaja_prueba1.ui.viewmodels.AdminReservationRulesUiState,
    onMaxDaysChange: (String) -> Unit,
    onCancellationHoursChange: (String) -> Unit,
    onSave: () -> Unit
) {
    val rules = state.rules ?: return
    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            RuleCard(
                title = "Horario de atención",
                value = "%02d:00–%02d:00".format(rules.openingHour, rules.closingHour),
                description = ""
            )
        }
        item {
            OutlinedTextField(
                value = state.maxDaysText,
                onValueChange = onMaxDaysChange,
                label = { Text("Anticipación máxima (días)") },
                supportingText = { Text("Desde hoy hasta este número de días adelante.") },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = state.cancellationHoursText,
                onValueChange = onCancellationHoursChange,
                label = { Text("Anticipación para cancelar (horas)") },
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            state.message?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            Button(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
                Text(if (state.isSaving) "Guardando…" else "Guardar reglas")
            }
        }
    }
}

@Composable
private fun RuleCard(title: String, value: String, description: String) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(value, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            if (description.isNotBlank()) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
