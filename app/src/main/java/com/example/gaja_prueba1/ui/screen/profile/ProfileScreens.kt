package com.example.gaja_prueba1.ui.screen.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.model.SUPPORT_CATEGORIES
import com.example.gaja_prueba1.model.SupportRequest
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaSelector
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.MyProfileUiState
import com.example.gaja_prueba1.ui.viewmodels.SupportUiState
import com.example.gaja_prueba1.validation.ProfileValidator
import com.example.gaja_prueba1.validation.UserValidator

/** Marco común de las pantallas del perfil: fondo, botón "‹ Volver", título y avisos (snackbar). */
@Composable
private fun ProfileScaffold(
    title: String,
    onBack: () -> Unit,
    message: String?,
    onMessageShown: () -> Unit,
    content: @Composable () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            onMessageShown()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(onClick = onBack) { Text("‹ Volver") }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = EspaciosPalette.Navy
            )
            content()
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

/** Mientras carga el perfil muestra un spinner; si falla, el error con "Reintentar". Si no, el contenido. */
@Composable
private fun ProfileLoadState(state: MyProfileUiState, onRetry: () -> Unit, content: @Composable () -> Unit) {
    when {
        state.isLoading -> Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }

        state.user == null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = state.errorMessage ?: "No pudimos cargar tu perfil.",
                color = MaterialTheme.colorScheme.error
            )
            TextButton(onClick = onRetry) { Text("Reintentar") }
        }

        else -> content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ---------------------------------------------------------------------------------------------
// Datos personales
// ---------------------------------------------------------------------------------------------

class PersonalDataActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onMessageShown: () -> Unit,
    val onNameChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    val onInstitutionChange: (String) -> Unit,
    val onSave: () -> Unit
)

@Composable
fun PersonalDataScreen(state: MyProfileUiState, actions: PersonalDataActions) {
    ProfileScaffold("Datos personales", actions.onBack, state.message, actions.onMessageShown) {
        ProfileLoadState(state, actions.onRetry) {
            val user = state.user!!
            val form = state.personal
            GajaCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoRow("Correo", user.email)
                    InfoRow("Rol", user.rol)
                    Text(
                        "El correo y el rol los define el administrador.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            GajaTextField(
                value = form.nombre,
                onValueChange = actions.onNameChange,
                label = "Nombre completo",
                errorText = form.nombreError,
                modifier = Modifier.fillMaxWidth()
            )
            GajaTextField(
                value = form.telefono,
                onValueChange = actions.onPhoneChange,
                label = "Teléfono (opcional)",
                errorText = form.telefonoError,
                modifier = Modifier.fillMaxWidth()
            )
            if (user.rol == UserValidator.EXTERNAL_ROLE) {
                GajaTextField(
                    value = form.institucion,
                    onValueChange = actions.onInstitutionChange,
                    label = "Institución",
                    errorText = form.institucionError,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            GajaButton(text = "Guardar cambios", onClick = actions.onSave, loading = form.isSaving)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Cambiar contraseña
// ---------------------------------------------------------------------------------------------

class ChangePasswordActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onMessageShown: () -> Unit,
    val onCurrentChange: (String) -> Unit,
    val onNewChange: (String) -> Unit,
    val onConfirmChange: (String) -> Unit,
    val onSave: () -> Unit
)

@Composable
fun ChangePasswordScreen(state: MyProfileUiState, actions: ChangePasswordActions) {
    ProfileScaffold("Cambiar contraseña", actions.onBack, state.message, actions.onMessageShown) {
        ProfileLoadState(state, actions.onRetry) {
            val form = state.password
            Text(
                "Usa al menos ${UserValidator.MIN_PASSWORD_LENGTH} caracteres. La usarás la próxima vez que inicies sesión.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            GajaTextField(
                value = form.actual,
                onValueChange = actions.onCurrentChange,
                label = "Contraseña actual",
                errorText = form.actualError,
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )
            GajaTextField(
                value = form.nueva,
                onValueChange = actions.onNewChange,
                label = "Contraseña nueva",
                errorText = form.nuevaError,
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )
            GajaTextField(
                value = form.confirmacion,
                onValueChange = actions.onConfirmChange,
                label = "Repite la contraseña nueva",
                errorText = form.confirmacionError,
                isPassword = true,
                modifier = Modifier.fillMaxWidth()
            )
            GajaButton(text = "Cambiar contraseña", onClick = actions.onSave, loading = form.isSaving)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Notificaciones
// ---------------------------------------------------------------------------------------------

class NotificationsActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onMessageShown: () -> Unit,
    val onReservationAlertsChange: (Boolean) -> Unit,
    val onAlertsChange: (Boolean) -> Unit
)

@Composable
fun NotificationsScreen(state: MyProfileUiState, actions: NotificationsActions) {
    ProfileScaffold("Notificaciones", actions.onBack, state.message, actions.onMessageShown) {
        ProfileLoadState(state, actions.onRetry) {
            val user = state.user!!
            GajaCard(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SwitchRow(
                        title = "Estado de mis reservas",
                        description = "Avisos cuando una reserva se aprueba, se rechaza o se cancela.",
                        checked = user.notifReservas,
                        enabled = !state.isSavingNotifications,
                        onCheckedChange = actions.onReservationAlertsChange
                    )
                    SwitchRow(
                        title = "Alertas generales",
                        description = "Avisos de cambios en espacios y horarios.",
                        checked = user.notifAlertas,
                        enabled = !state.isSavingNotifications,
                        onCheckedChange = actions.onAlertsChange
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

// ---------------------------------------------------------------------------------------------
// Ayuda y soporte
// ---------------------------------------------------------------------------------------------

private val faqs = listOf(
    "¿Con cuántos días de anticipación puedo reservar?" to
            "Hasta 7 días hacia adelante. No se pueden reservar fechas pasadas.",
    "¿Cuántas reservas puedo tener por día?" to
            "Hasta 3 reservas activas en el mismo día. Un horario ocupado por otra reserva no se puede tomar.",
    "¿Puedo cancelar una reserva?" to
            "Sí, hasta 2 horas antes del horario reservado, desde Reservas.",
    "¿Por qué me rechazaron una reserva?" to
            "El administrador escribe un motivo. Lo ves en Reservas, tocando la reserva rechazada.",
    "¿Por qué un horario aparece bloqueado?" to
            "El administrador lo bloquea, por ejemplo por mantención. Elige otro horario u otro espacio."
)

class HelpActions(
    val onBack: () -> Unit,
    val onRetry: () -> Unit,
    val onMessageShown: () -> Unit,
    val onNewRequest: () -> Unit
)

@Composable
fun HelpScreen(state: SupportUiState, actions: HelpActions) {
    ProfileScaffold("Ayuda y soporte", actions.onBack, state.message, actions.onMessageShown) {
        Text(
            "Preguntas frecuentes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = EspaciosPalette.Navy
        )
        faqs.forEach { (question, answer) -> FaqItem(question, answer) }

        GajaButton(text = "Nueva solicitud", onClick = actions.onNewRequest)

        Text(
            "Mis solicitudes",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = EspaciosPalette.Navy
        )
        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.errorMessage != null -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.errorMessage, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = actions.onRetry) { Text("Reintentar") }
            }

            state.requests.isEmpty() -> Text(
                "Aún no has enviado solicitudes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            else -> state.requests.forEach { RequestCard(it) }
        }
    }
}

@Composable
private fun FaqItem(question: String, answer: String) {
    var open by rememberSaveable(question) { mutableStateOf(false) }
    GajaCard(modifier = Modifier.fillMaxWidth(), onClick = { open = !open }) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    question,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(if (open) "−" else "+", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (open) {
                Text(answer, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RequestCard(request: SupportRequest) {
    GajaCard(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    request.category,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                GajaStatusChip(
                    text = if (request.resolved) "Resuelta" else "Pendiente",
                    tone = if (request.resolved) GajaChipTone.Success else GajaChipTone.Info
                )
            }
            Text(request.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            if (request.date.isNotBlank()) {
                Text(request.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Ayuda: nueva solicitud
// ---------------------------------------------------------------------------------------------

class NewRequestActions(
    val onBack: () -> Unit,
    val onMessageShown: () -> Unit,
    val onCategoryChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onSend: () -> Unit
)

@Composable
fun NewRequestScreen(state: SupportUiState, actions: NewRequestActions) {
    val form = state.form
    ProfileScaffold("Nueva solicitud", actions.onBack, state.message, actions.onMessageShown) {
        Text(
            "Cuéntanos qué necesitas. El administrador la revisará.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        GajaSelector(
            options = SUPPORT_CATEGORIES,
            selectedIndex = SUPPORT_CATEGORIES.indexOf(form.category),
            onSelect = { actions.onCategoryChange(SUPPORT_CATEGORIES[it]) },
            label = "Tema"
        )
        form.categoryError?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
            value = form.description,
            onValueChange = actions.onDescriptionChange,
            label = { Text("Descripción") },
            isError = form.descriptionError != null,
            supportingText = {
                Text(form.descriptionError ?: "${form.description.length}/${ProfileValidator.MAX_REQUEST_LENGTH}")
            },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        GajaButton(text = "Enviar solicitud", onClick = actions.onSend, loading = form.isSaving)
    }
}
