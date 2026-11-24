package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.gaja_prueba1.model.Category
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.viewmodels.AdminCategoriesUiState
import com.example.gaja_prueba1.ui.viewmodels.AdminCategoriesViewModel
import com.example.gaja_prueba1.ui.viewmodels.CategoryFormState

class AdminCategoriesActions(
    val onRetry: () -> Unit,
    val onMessageShown: () -> Unit,
    val onNewCategory: () -> Unit,
    val onEditCategory: (Category) -> Unit,
    val onFormClose: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onSave: () -> Unit,
    val onDeleteRequested: (Category) -> Unit,
    val onDeleteCancelled: () -> Unit,
    val onDeleteConfirmed: () -> Unit
)

@Composable
fun AdminCategoriesScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminCategoriesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val actions = AdminCategoriesActions(
        onRetry = viewModel::load,
        onMessageShown = viewModel::onMessageShown,
        onNewCategory = viewModel::onNewCategory,
        onEditCategory = viewModel::onEditCategory,
        onFormClose = viewModel::onFormClose,
        onNameChange = viewModel::onNameChange,
        onSave = viewModel::onSave,
        onDeleteRequested = viewModel::onDeleteRequested,
        onDeleteCancelled = viewModel::onDeleteCancelled,
        onDeleteConfirmed = viewModel::onDeleteConfirmed
    )
    AdminCategoriesContent(state = state, actions = actions, modifier = modifier)
}

@Composable
fun AdminCategoriesContent(
    state: AdminCategoriesUiState,
    actions: AdminCategoriesActions,
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
            CategoryForm(form = form, actions = actions)
        } else {
            CategoryList(state = state, actions = actions)
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    state.pendingDelete?.let { category ->
        AlertDialog(
            onDismissRequest = actions.onDeleteCancelled,
            title = { Text("¿Eliminar ${category.name}?") },
            text = {
                Text("Solo se puede eliminar si no tiene espacios. Si los tiene, primero cámbialos de categoría.")
            },
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
private fun CategoryList(state: AdminCategoriesUiState, actions: AdminCategoriesActions) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Categorías",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item { GajaButton(text = "Nueva categoría", onClick = actions.onNewCategory) }

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

            state.categories.isEmpty() -> item {
                Text(
                    text = "Aún no hay categorías.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> items(state.categories, key = { it.id }) { category ->
                CategoryCard(category = category, actions = actions)
            }
        }
    }
}

@Composable
private fun CategoryCard(category: Category, actions: AdminCategoriesActions) {
    GajaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = { actions.onEditCategory(category) }) { Text("Editar") }
            TextButton(onClick = { actions.onDeleteRequested(category) }) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CategoryForm(form: CategoryFormState, actions: AdminCategoriesActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = actions.onFormClose) { Text("‹ Volver") }
        Text(
            text = if (form.isEditing) "Editar categoría" else "Nueva categoría",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        GajaTextField(
            value = form.name,
            onValueChange = actions.onNameChange,
            label = "Nombre de la categoría",
            errorText = form.nameError,
            modifier = Modifier.fillMaxWidth()
        )
        GajaButton(
            text = if (form.isEditing) "Guardar cambios" else "Crear categoría",
            onClick = actions.onSave,
            loading = form.isSaving
        )
    }
}
