package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.horizontalScroll
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
import com.example.gaja_prueba1.model.EstadoPerfil
import com.example.gaja_prueba1.model.Usuario
import com.example.gaja_prueba1.model.fechaAlta
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaFilterChip
import com.example.gaja_prueba1.ui.components.GajaSelector
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.viewmodels.AdminProfilesUiState
import com.example.gaja_prueba1.ui.viewmodels.AdminProfilesViewModel
import com.example.gaja_prueba1.ui.viewmodels.ProfileFilter
import com.example.gaja_prueba1.ui.viewmodels.USER_ROLES
import com.example.gaja_prueba1.ui.viewmodels.UserFormState

class AdminProfilesActions(
    val onFilterChange: (ProfileFilter) -> Unit,
    val onRetry: () -> Unit,
    val onMessageShown: () -> Unit,
    val onOpenDetail: (Usuario) -> Unit,
    val onCloseDetail: () -> Unit,
    val onApprove: (Usuario) -> Unit,
    val onActivate: (Usuario) -> Unit,
    val onRejectRequested: (Usuario) -> Unit,
    val onRejectReasonChange: (String) -> Unit,
    val onRejectCancelled: () -> Unit,
    val onRejectConfirmed: () -> Unit,
    val onDeactivateRequested: (Usuario) -> Unit,
    val onDeactivateCancelled: () -> Unit,
    val onDeactivateConfirmed: () -> Unit,
    val onNewUser: () -> Unit,
    val onFormClose: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onRoleChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    val onInstitutionChange: (String) -> Unit,
    val onSave: () -> Unit
)

@Composable
fun AdminProfilesScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminProfilesViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.refreshQuietly() }
    val actions = AdminProfilesActions(
        onFilterChange = viewModel::onFilterChange,
        onRetry = viewModel::load,
        onMessageShown = viewModel::onMessageShown,
        onOpenDetail = viewModel::onOpenDetail,
        onCloseDetail = viewModel::onCloseDetail,
        onApprove = viewModel::onApprove,
        onActivate = viewModel::onActivate,
        onRejectRequested = viewModel::onRejectRequested,
        onRejectReasonChange = viewModel::onRejectReasonChange,
        onRejectCancelled = viewModel::onRejectCancelled,
        onRejectConfirmed = viewModel::onRejectConfirmed,
        onDeactivateRequested = viewModel::onDeactivateRequested,
        onDeactivateCancelled = viewModel::onDeactivateCancelled,
        onDeactivateConfirmed = viewModel::onDeactivateConfirmed,
        onNewUser = viewModel::onNewUser,
        onFormClose = viewModel::onFormClose,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRoleChange = viewModel::onRoleChange,
        onPhoneChange = viewModel::onPhoneChange,
        onInstitutionChange = viewModel::onInstitutionChange,
        onSave = viewModel::onSave
    )
    AdminProfilesContent(state = state, actions = actions, modifier = modifier)
}

@Composable
fun AdminProfilesContent(
    state: AdminProfilesUiState,
    actions: AdminProfilesActions,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(it)
            actions.onMessageShown()
        }
    }

    // El botón "atrás" del teléfono cierra el formulario o el detalle antes de salir de la pestaña.
    BackHandler(enabled = state.form != null) { actions.onFormClose() }
    BackHandler(enabled = state.form == null && state.selected != null) { actions.onCloseDetail() }

    Box(modifier = modifier.fillMaxSize()) {
        val form = state.form
        val selected = state.selected
        when {
            form != null -> UserForm(form = form, actions = actions)
            selected != null -> ProfileDetail(user = selected, actions = actions)
            else -> ProfileList(state = state, actions = actions)
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    state.reject?.let { reject ->
        AlertDialog(
            onDismissRequest = actions.onRejectCancelled,
            title = { Text("Rechazar a ${reject.user.nombre}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Escribe el motivo. La persona lo verá al intentar iniciar sesión.")
                    GajaTextField(
                        value = reject.reason,
                        onValueChange = actions.onRejectReasonChange,
                        label = "Motivo del rechazo",
                        errorText = reject.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = actions.onRejectConfirmed, enabled = !reject.isSaving) {
                    Text("Rechazar perfil", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = actions.onRejectCancelled) { Text("Cancelar") } }
        )
    }

    state.pendingDeactivate?.let { user ->
        AlertDialog(
            onDismissRequest = actions.onDeactivateCancelled,
            title = { Text("¿Desactivar a ${user.nombre}?") },
            text = {
                Text("No podrá iniciar sesión hasta que lo actives de nuevo. Sus reservas y datos se conservan.")
            },
            confirmButton = {
                TextButton(onClick = actions.onDeactivateConfirmed) {
                    Text("Desactivar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = actions.onDeactivateCancelled) { Text("Cancelar") } }
        )
    }
}

private fun EstadoPerfil.tone(): GajaChipTone = when (this) {
    EstadoPerfil.Pendiente -> GajaChipTone.Info
    EstadoPerfil.Aprobado -> GajaChipTone.Success
    EstadoPerfil.Rechazado -> GajaChipTone.Danger
    EstadoPerfil.Desactivado -> GajaChipTone.Neutral
}

@Composable
private fun ProfileList(state: AdminProfilesUiState, actions: AdminProfilesActions) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Perfiles",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        item { GajaButton(text = "Crear usuario", onClick = actions.onNewUser) }
        item {
            // Con scroll horizontal para que la tercera pestaña no se corte en pantallas angostas.
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProfileFilter.entries.forEach { filter ->
                    GajaFilterChip(
                        text = "${filter.label} (${state.countOf(filter)})",
                        selected = state.filter == filter,
                        onClick = { actions.onFilterChange(filter) }
                    )
                }
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
                    text = "No hay perfiles ${state.filter.label.lowercase()}.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> items(state.visible, key = { it.id }) { user ->
                ProfileCard(user = user, actions = actions)
            }
        }
    }
}

@Composable
private fun ProfileCard(user: Usuario, actions: AdminProfilesActions) {
    GajaCard(modifier = Modifier.fillMaxWidth(), onClick = { actions.onOpenDetail(user) }) {
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
                    text = user.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                GajaStatusChip(text = user.estado.label, tone = user.estado.tone())
            }
            Text(
                text = "${user.rol} · ${user.email}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProfileDetail(user: Usuario, actions: AdminProfilesActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = actions.onCloseDetail) { Text("‹ Volver") }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = user.nombre,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.weight(1f)
            )
            GajaStatusChip(text = user.estado.label, tone = user.estado.tone())
        }

        GajaCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                DetailRow("Rol", user.rol)
                DetailRow("Correo", user.email)
                DetailRow("Teléfono", user.telefono.ifBlank { "No informado" })
                if (user.institucion.isNotBlank()) DetailRow("Institución", user.institucion)
                DetailRow("Fecha de registro", user.fechaAlta.ifBlank { "—" })
                if (user.estado == EstadoPerfil.Rechazado) {
                    DetailRow("Motivo del rechazo", user.motivoRechazo ?: "Sin motivo")
                }
            }
        }

        when (user.estado) {
            EstadoPerfil.Pendiente -> {
                GajaButton(text = "Aprobar perfil", onClick = { actions.onApprove(user) })
                TextButton(
                    onClick = { actions.onRejectRequested(user) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Rechazar perfil", color = MaterialTheme.colorScheme.error)
                }
            }

            EstadoPerfil.Aprobado -> TextButton(
                onClick = { actions.onDeactivateRequested(user) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Desactivar usuario", color = MaterialTheme.colorScheme.error)
            }

            EstadoPerfil.Desactivado -> GajaButton(text = "Activar usuario", onClick = { actions.onActivate(user) })

            EstadoPerfil.Rechazado -> Unit
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun UserForm(form: UserFormState, actions: AdminProfilesActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = actions.onFormClose) { Text("‹ Volver") }
        Text(
            text = "Crear usuario",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "El usuario queda aprobado y puede iniciar sesión de inmediato.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        GajaTextField(
            value = form.nombre,
            onValueChange = actions.onNameChange,
            label = "Nombre completo",
            errorText = form.nombreError,
            modifier = Modifier.fillMaxWidth()
        )
        GajaTextField(
            value = form.email,
            onValueChange = actions.onEmailChange,
            label = "Correo",
            errorText = form.emailError,
            modifier = Modifier.fillMaxWidth()
        )
        GajaTextField(
            value = form.password,
            onValueChange = actions.onPasswordChange,
            label = "Contraseña",
            errorText = form.passwordError,
            isPassword = true,
            modifier = Modifier.fillMaxWidth()
        )

        GajaSelector(
            options = USER_ROLES,
            selectedIndex = USER_ROLES.indexOf(form.rol),
            onSelect = { actions.onRoleChange(USER_ROLES[it]) },
            label = "Rol"
        )
        form.rolError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }

        if (form.needsInstitution) {
            GajaTextField(
                value = form.institucion,
                onValueChange = actions.onInstitutionChange,
                label = "Institución",
                errorText = form.institucionError,
                modifier = Modifier.fillMaxWidth()
            )
        }
        GajaTextField(
            value = form.telefono,
            onValueChange = actions.onPhoneChange,
            label = "Teléfono (opcional)",
            errorText = form.telefonoError,
            modifier = Modifier.fillMaxWidth()
        )

        GajaButton(text = "Crear usuario", onClick = actions.onSave, loading = form.isSaving)
    }
}
