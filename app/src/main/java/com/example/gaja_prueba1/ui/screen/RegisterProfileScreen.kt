package com.example.gaja_prueba1.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaFilterChip
import com.example.gaja_prueba1.ui.components.GajaTextField
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.REGISTER_ROLES
import com.example.gaja_prueba1.ui.viewmodels.RegisterProfileViewModel
import com.example.gaja_prueba1.ui.viewmodels.SentProfile
import com.example.gaja_prueba1.validation.UserValidator

/**
 * Flujo "Crear perfil" del prototipo: formulario y, al enviarlo, la pantalla "Solicitud de perfil enviada".
 * [onBack] vuelve al inicio de sesión desde cualquiera de las dos.
 */
@Composable
fun RegisterProfileScreen(
    onBack: () -> Unit,
    viewModel: RegisterProfileViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val sent = state.sent

    val leave = {
        viewModel.reset()
        onBack()
    }
    BackHandler(onBack = leave)

    if (sent != null) {
        ProfileRequestSentScreen(sent = sent, onBackToLogin = leave)
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = leave) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Volver", tint = EspaciosPalette.Steel)
            }
            Text(
                text = "Crear perfil",
                modifier = Modifier.padding(start = 6.dp),
                color = EspaciosPalette.Navy,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GajaTextField(
                value = state.nombre,
                onValueChange = viewModel::onNombre,
                label = "Nombre completo",
                leadingIcon = Icons.Outlined.Person,
                errorText = state.nombreError
            )
            GajaTextField(
                value = state.email,
                onValueChange = viewModel::onEmail,
                label = "Correo institucional",
                leadingIcon = Icons.Outlined.Email,
                errorText = state.emailError
            )

            Text("Rol", color = EspaciosPalette.Navy, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            RoleChips(selected = state.rol, onSelect = viewModel::onRol)

            if (state.rol == UserValidator.EXTERNAL_ROLE) {
                GajaTextField(
                    value = state.institucion,
                    onValueChange = viewModel::onInstitucion,
                    label = "Institución",
                    leadingIcon = Icons.Outlined.School,
                    errorText = state.institucionError
                )
            }

            GajaTextField(
                value = state.telefono,
                onValueChange = viewModel::onTelefono,
                label = "Teléfono (opcional)",
                leadingIcon = Icons.Outlined.Phone,
                errorText = state.telefonoError
            )
            GajaTextField(
                value = state.password,
                onValueChange = viewModel::onPassword,
                label = "Contraseña",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                errorText = state.passwordError
            )
            GajaTextField(
                value = state.confirmPassword,
                onValueChange = viewModel::onConfirm,
                label = "Confirmar contraseña",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                errorText = state.confirmError
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(EspaciosPalette.SkyCard.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    "Aprobación del administrador",
                    color = EspaciosPalette.Navy,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "Podrás ingresar cuando tu perfil sea aprobado.",
                    color = EspaciosPalette.Slate,
                    fontSize = 11.sp
                )
            }

            state.submitError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }

        GajaButton(
            text = "Solicitar acceso",
            onClick = viewModel::submit,
            loading = state.isSaving,
            modifier = Modifier.padding(horizontal = 28.dp, vertical = 20.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RoleChips(selected: String, onSelect: (String) -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        REGISTER_ROLES.forEach { role ->
            GajaFilterChip(text = role, selected = role == selected, onClick = { onSelect(role) })
        }
    }
}

/** "Solicitud de perfil enviada": confirmación con el resumen de lo que se pidió. */
@Composable
fun ProfileRequestSentScreen(sent: SentProfile, onBackToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(top = 72.dp)
                .size(104.dp)
                .clip(CircleShape)
                .background(EspaciosPalette.Orange.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(69.dp)
                    .clip(CircleShape)
                    .background(EspaciosPalette.Orange),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = EspaciosPalette.White, modifier = Modifier.size(37.dp))
            }
        }

        Text(
            "¡Perfil enviado!",
            modifier = Modifier.padding(top = 25.dp),
            color = EspaciosPalette.Steel,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Tu perfil quedó pendiente de aprobación. Te avisaremos por correo.",
            modifier = Modifier.padding(top = 12.dp),
            color = EspaciosPalette.Slate,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .padding(top = 46.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(EspaciosPalette.White.copy(alpha = 0.55f))
                .padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(21.dp)
        ) {
            SummaryRow(Icons.Outlined.Person, "${sent.nombre} · ${sent.rol}")
            SummaryRow(Icons.Outlined.Email, sent.email)
            SummaryRow(Icons.Outlined.Schedule, "Pendiente de aprobación")
        }

        GajaButton(
            text = "Volver al inicio de sesión",
            onClick = onBackToLogin,
            modifier = Modifier.padding(top = 28.dp, bottom = 32.dp)
        )
    }
}

@Composable
private fun SummaryRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        Icon(icon, contentDescription = null, tint = EspaciosPalette.Navy, modifier = Modifier.size(22.dp))
        Text(text, color = EspaciosPalette.Navy, fontSize = 13.sp, modifier = Modifier.weight(1f))
    }
}