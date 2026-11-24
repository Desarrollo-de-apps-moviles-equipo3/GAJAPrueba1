package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaFilterChip
import com.example.gaja_prueba1.ui.components.GajaSelector
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.viewmodels.AdminSpacesUiState
import com.example.gaja_prueba1.ui.viewmodels.AdminSpacesViewModel
import com.example.gaja_prueba1.ui.viewmodels.SpaceFormState

class AdminSpacesActions(
    val onShowDisabled: (Boolean) -> Unit,
    val onRetry: () -> Unit,
    val onMessageShown: () -> Unit,
    val onNewSpace: () -> Unit,
    val onEditSpace: (Space) -> Unit,
    val onFormClose: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onCategoryChange: (String) -> Unit,
    val onCapacityChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onSave: () -> Unit,
    val onDisableRequested: (Space) -> Unit,
    val onDisableCancelled: () -> Unit,
    val onDisableConfirmed: () -> Unit,
    val onEnable: (Space) -> Unit,
    val onDeleteRequested: (Space) -> Unit,
    val onDeleteCancelled: () -> Unit,
    val onDeleteConfirmed: () -> Unit
)

@Composable
fun AdminSpacesScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminSpacesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refreshQuietly() }
    val actions = AdminSpacesActions(
        onShowDisabled = viewModel::onShowDisabled,
        onRetry = viewModel::load,
        onMessageShown = viewModel::onMessageShown,
        onNewSpace = viewModel::onNewSpace,
        onEditSpace = viewModel::onEditSpace,
        onFormClose = viewModel::onFormClose,
        onNameChange = viewModel::onNameChange,
        onCategoryChange = viewModel::onCategoryChange,
        onCapacityChange = viewModel::onCapacityChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onSave = viewModel::onSave,
        onDisableRequested = viewModel::onDisableRequested,
        onDisableCancelled = viewModel::onDisableCancelled,
        onDisableConfirmed = viewModel::onDisableConfirmed,
        onEnable = viewModel::onEnable,
        onDeleteRequested = viewModel::onDeleteRequested,
        onDeleteCancelled = viewModel::onDeleteCancelled,
        onDeleteConfirmed = viewModel::onDeleteConfirmed
    )
    AdminSpacesContent(state = state, actions = actions, modifier = modifier)
}

@Composable
fun AdminSpacesContent(
    state: AdminSpacesUiState,
    actions: AdminSpacesActions,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            actions.onMessageShown()
        }
    }

    BackHandler(enabled = state.form != null) { actions.onFormClose() }

    Box(modifier = modifier.fillMaxSize()) {
        val form = state.form
        if (form != null) {
            SpaceForm(form = form, categories = state.categories, actions = actions)
        } else {
            SpaceList(state = state, actions = actions)
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    state.pendingDisable?.let { space ->
        AlertDialog(
            onDismissRequest = actions.onDisableCancelled,
            title = { Text("¿Deshabilitar ${space.name}?") },
            text = {
                Text("Los usuarios dejarán de verlo disponible para reservar. Puedes habilitarlo de nuevo cuando quieras.")
            },
            confirmButton = { TextButton(onClick = actions.onDisableConfirmed) { Text("Deshabilitar") } },
            dismissButton = { TextButton(onClick = actions.onDisableCancelled) { Text("Cancelar") } }
        )
    }

    state.pendingDelete?.let { space ->
        AlertDialog(
            onDismissRequest = actions.onDeleteCancelled,
            title = { Text("¿Eliminar ${space.name}?") },
            text = { Text("Esta acción no se puede deshacer. Si solo quieres sacarlo de circulación, mejor deshabilítalo.") },
            confirmButton = {
                TextButton(onClick = actions.onDeleteConfirmed) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = actions.onDeleteCancelled) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SpaceList(state: AdminSpacesUiState, actions: AdminSpacesActions) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Espacios",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item { GajaButton(text = "Nuevo espacio", onClick = actions.onNewSpace) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GajaFilterChip(
                    text = "Activos (${state.active.size})",
                    selected = !state.showDisabled,
                    onClick = { actions.onShowDisabled(false) }
                )
                GajaFilterChip(
                    text = "Deshabilitados (${state.disabled.size})",
                    selected = state.showDisabled,
                    onClick = { actions.onShowDisabled(true) }
                )
            }
        }

        when {
            state.isLoading -> item {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            state.errorMessage != null -> item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                    TextButton(onClick = actions.onRetry) { Text("Reintentar") }
                }
            }

            state.visible.isEmpty() -> item {
                Text(
                    text = if (state.showDisabled) "No hay espacios deshabilitados." else "No hay espacios activos.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> items(state.visible, key = { it.id }) { space ->
                SpaceCard(space = space, actions = actions)
            }
        }
    }
}

@Composable
private fun SpaceCard(space: Space, actions: AdminSpacesActions) {
    GajaCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = space.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                GajaStatusChip(
                    text = if (space.available) "Disponible" else "Deshabilitado",
                    tone = if (space.available) GajaChipTone.Success else GajaChipTone.Neutral
                )
            }
            Text(
                text = "${space.category} · ${space.capacity} personas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = { actions.onEditSpace(space) }) { Text("Editar") }
                if (space.available) {
                    TextButton(onClick = { actions.onDisableRequested(space) }) { Text("Deshabilitar") }
                } else {
                    TextButton(onClick = { actions.onEnable(space) }) { Text("Habilitar") }
                }
                TextButton(onClick = { actions.onDeleteRequested(space) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun SpaceForm(form: SpaceFormState, categories: List<String>, actions: AdminSpacesActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = actions.onFormClose) { Text("‹ Volver") }
        Text(
            text = if (form.isEditing) "Editar espacio" else "Nuevo espacio",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        GajaTextField(
            value = form.name,
            onValueChange = actions.onNameChange,
            label = "Nombre del espacio",
            errorText = form.nameError,
            modifier = Modifier.fillMaxWidth()
        )

        GajaSelector(
            options = categories,
            selectedIndex = categories.indexOf(form.category),
            onSelect = { actions.onCategoryChange(categories[it]) },
            label = "Categoría"
        )
        form.categoryError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        GajaTextField(
            value = form.capacity,
            onValueChange = actions.onCapacityChange,
            label = "Capacidad (personas)",
            errorText = form.capacityError,
            modifier = Modifier.fillMaxWidth()
        )

        GajaTextField(
            value = form.description,
            onValueChange = actions.onDescriptionChange,
            label = "Descripción",
            modifier = Modifier.fillMaxWidth()
        )

        GajaButton(
            text = if (form.isEditing) "Guardar cambios" else "Crear espacio",
            onClick = actions.onSave,
            loading = form.isSaving
        )
    }
}
