package com.example.gaja_prueba1.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.gaja_prueba1.ui.screen.admin.AdminAgendaScreen
import com.example.gaja_prueba1.ui.screen.admin.AdminSpacesAndCategoriesScreen

/** Pestañas de la barra inferior del administrador (según el Figma). */
enum class AdminTab(val label: String, val icon: ImageVector) {
    Inicio("Inicio", Icons.Filled.Home),
    Agenda("Agenda", Icons.Filled.DateRange),
    Reservas("Reservas", Icons.AutoMirrored.Filled.List),
    Perfiles("Perfiles", Icons.Filled.Person),
    Mas("Más", Icons.Filled.Menu)
}

/** Destinos del menú lateral que abre la pestaña "Más". */
private val adminMenuItems = listOf(
    "Espacios y categorías",
    "Incidencias",
    "Reportes",
    "Soporte y contacto",
    "Registro de sesiones",
    "Notificaciones",
    "Reglas de reserva"
)

@Composable
fun AdminShell(
    userName: String,
    onLogout: () -> Unit
) {
    val tabs = AdminTab.entries
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    var menuDestination by rememberSaveable { mutableStateOf<String?>(null) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text("ESCUELA MILITAR")
                    Text("Panel de administración")
                    Spacer(Modifier.height(24.dp))

                    adminMenuItems.forEach { item ->
                        NavigationDrawerItem(
                            label = { Text(item) },
                            selected = menuDestination == item,
                            onClick = {
                                menuDestination = item
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                    Spacer(Modifier.weight(1f))

                    OutlinedButton(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                            selected = selectedIndex == index &&
                                    menuDestination == null,
                            onClick = {
                                if (tab == AdminTab.Mas) {
                                    scope.launch { drawerState.open() }
                                } else {
                                    menuDestination = null
                                    selectedIndex = index
                                }
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
            if (menuDestination == "Espacios y categorías") {
                AdminSpacesAndCategoriesScreen(modifier = Modifier.padding(innerPadding))
            } else if (menuDestination == null && tabs[selectedIndex] == AdminTab.Agenda) {
                AdminAgendaScreen(modifier = Modifier.padding(innerPadding))
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Hola, $userName")
                        Text(menuDestination ?: tabs[selectedIndex].label)
                    }
                }
            }
        }
    }
}