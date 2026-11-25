package com.example.gaja_prueba1.ui.screen.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.ui.components.GajaCard

data class AdminMenuEntry(val label: String, val icon: ImageVector)

/** Destinos de "Más" (los mismos del menú lateral). */
val adminMenuEntries = listOf(
    AdminMenuEntry("Espacios y categorías", Icons.Outlined.Apartment),
    AdminMenuEntry("Incidencias", Icons.Outlined.WarningAmber),
    AdminMenuEntry("Reportes", Icons.Outlined.BarChart),
    AdminMenuEntry("Soporte y contacto", Icons.Outlined.Help),
    AdminMenuEntry("Registro de sesiones", Icons.Outlined.Schedule),
    AdminMenuEntry("Notificaciones", Icons.Outlined.Notifications),
    AdminMenuEntry("Reglas de reserva", Icons.Outlined.Tune)
)

@Composable
fun AdminMoreScreen(
    userName: String,
    userEmail: String,
    onSelect: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Más",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
        )

        GajaCard(Modifier.fillMaxWidth()) {
            Row(
                Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    Modifier.size(56.dp).background(MaterialTheme.colorScheme.outline, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userName.split(" ").filter { it.isNotBlank() }
                            .take(2).joinToString("") { it.first().uppercase() },
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text(userName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                    Text(
                        if (userEmail.isBlank()) "Administrador" else "Administrador · $userEmail",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        GajaCard(Modifier.fillMaxWidth()) {
            Column {
                adminMenuEntries.forEach { entry ->
                    MenuRow(entry.label, entry.icon, MaterialTheme.colorScheme.onSurfaceVariant) { onSelect(entry.label) }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
                MenuRow("Cerrar sesión", Icons.Filled.Logout, MaterialTheme.colorScheme.error, onLogout)
            }
        }
    }
}

@Composable
private fun MenuRow(label: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Text(label, Modifier.weight(1f), color = tint, style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
    }
}