package com.example.gaja_prueba1.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.ui.screen.admin.*
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import kotlinx.coroutines.launch

/** Pestañas de la barra inferior del administrador (según el Figma). */
enum class AdminTab(val label: String, val icon: ImageVector) {
    Inicio("Inicio", Icons.Filled.Home),
    Agenda("Agenda", Icons.Filled.DateRange),
    Reservas("Reservas", Icons.AutoMirrored.Filled.List),
    Perfiles("Perfiles", Icons.Filled.Person),
    Mas("Más", Icons.Filled.Menu)
}

@Composable
fun AdminShell(
    userName: String,
    onLogout: () -> Unit,
    userEmail: String = ""
) {
    val tabs = AdminTab.entries
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var menuDestination by rememberSaveable { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    fun openDestination(name: String) {
        menuDestination = name
        selectedIndex = AdminTab.Mas.ordinal
    }

    // Atrás desde un destino de "Más" vuelve a la pantalla "Más".
    BackHandler(enabled = menuDestination != null) { menuDestination = null }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(drawerContainerColor = EspaciosPalette.Navy) {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "ESCUELA MILITAR",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Panel de administración",
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        IconButton(onClick = { scope.launch { drawerState.close() } }) {
                            Icon(Icons.Filled.Close, contentDescription = "Cerrar menú", tint = Color.White)
                        }
                    }
                    Spacer(Modifier.height(24.dp))

                    adminMenuEntries.forEach { entry ->
                        NavigationDrawerItem(
                            label = { Text(entry.label) },
                            icon = { Icon(entry.icon, contentDescription = null) },
                            selected = menuDestination == entry.label,
                            onClick = {
                                openDestination(entry.label)
                                scope.launch { drawerState.close() }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedContainerColor = Color.Transparent,
                                selectedContainerColor = EspaciosPalette.NavyTop,
                                unselectedTextColor = Color.White,
                                selectedTextColor = Color.White,
                                unselectedIconColor = Color.White,
                                selectedIconColor = Color.White
                            )
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Filled.Logout, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Cerrar sesión")
                    }
                }
            }
        }
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedIndex == index,
                            onClick = {
                                menuDestination = null
                                selectedIndex = index
                                if (tab == AdminTab.Mas) scope.launch { drawerState.open() }
                            },
                            icon = {
                                if (tab == AdminTab.Reservas) {
                                    // El número saldrá del servicio más adelante.
                                    BadgedBox(badge = { Badge { Text("3") } }) {
                                        Icon(tab.icon, contentDescription = tab.label)
                                    }
                                } else {
                                    Icon(tab.icon, contentDescription = tab.label)
                                }
                            },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            val content = Modifier.padding(innerPadding)
            when {
                menuDestination == "Espacios y categorías" ->
                    AdminSpacesAndCategoriesScreen(modifier = content)

                menuDestination == "Alertas" ->
                    AdminAlertsScreen(
                        onOpenIncidents = { openDestination("Incidencias") },
                        modifier = content
                    )

                menuDestination == "Incidencias" ->
                    AdminIncidenciasScreen(userEmail = userEmail, modifier = content)

                menuDestination == "Reportes" ->
                    AdminReportsScreen(modifier = content)

                menuDestination == "Reglas de reserva" ->
                    AdminReservationRulesScreen(modifier = content)

                menuDestination == "Soporte y contacto" ->
                    AdminSupportScreen(modifier = content)

                menuDestination == "Registro de sesiones" ->
                    AdminSessionHistoryScreen(onBack = { menuDestination = null }, modifier = content)

                menuDestination == "Notificaciones" ->
                    AdminNotificationsScreen(onBack = { menuDestination = null }, modifier = content)

                menuDestination != null ->
                    AdminPlaceholder(userName, menuDestination.orEmpty(), content)

                else -> when (tabs[selectedIndex]) {
                    AdminTab.Inicio -> AdminHomeScreen(
                        userName = userName,
                        modifier = content,
                        onSeeAllReservations = { selectedIndex = AdminTab.Reservas.ordinal },
                        onBlockSchedule = { selectedIndex = AdminTab.Agenda.ordinal },
                        onCreateUser = { selectedIndex = AdminTab.Perfiles.ordinal },
                        onReports = { openDestination("Reportes") }
                    )
                    AdminTab.Agenda -> AdminAgendaScreen(modifier = content)
                    AdminTab.Reservas -> AdminReservationsScreen(modifier = content)
                    AdminTab.Perfiles -> AdminProfilesScreen(modifier = content)
                    AdminTab.Mas -> AdminMoreScreen(
                        userName = userName,
                        userEmail = userEmail,
                        onSelect = ::openDestination,
                        onLogout = onLogout,
                        modifier = content
                    )
                }
            }
        }
    }
}

/** Pantalla temporal para secciones que hacen otros integrantes (Perfiles, Reportes, etc.). */
@Composable
private fun AdminPlaceholder(userName: String, title: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Hola, $userName")
            Text(title)
        }
    }
}
