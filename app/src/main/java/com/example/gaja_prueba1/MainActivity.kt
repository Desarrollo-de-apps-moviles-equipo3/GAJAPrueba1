package com.example.gaja_prueba1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.screen.AllCourtsScreen
import com.example.gaja_prueba1.ui.screen.BottomTab
import com.example.gaja_prueba1.ui.screen.ComingSoonScreen
import com.example.gaja_prueba1.ui.screen.LoginScreen
import com.example.gaja_prueba1.ui.screen.MainShellScreen
import com.example.gaja_prueba1.ui.screen.ProfileScreen
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.viewmodels.HomeViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            GAJAPrueba1Theme {
                var loggedEmail by rememberSaveable {
                    mutableStateOf<String?>(null)
                }

                val email = loggedEmail

                if (email == null) {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            LoginScreen(onLoginSuccess = { loggedEmail = it })
                        }
                    }
                } else {
                    val userName = email.substringBefore("@")
                        .replace("_", " ")
                        .replaceFirstChar { it.uppercase() }

                    // HomeScreen y esta pestaña comparten el mismo HomeViewModel
                    // asociado a la Activity, así se usa la misma lista de canchas.
                    val homeViewModel: HomeViewModel = viewModel()
                    val homeState by homeViewModel.uiState.collectAsState()

                    MainShellScreen(
                        userName = userName,
                        otherTabs = { tab, onBackToHome, onReserveCourt ->
                            when (tab) {
                                BottomTab.Reservas -> AllCourtsScreen(
                                    spaces = homeState.allSpaces,
                                    onBack = onBackToHome,
                                    onReserveClick = onReserveCourt
                                )

                                BottomTab.Perfil -> ProfileScreen(
                                    fullName = userName,
                                    email = email,
                                    onMyReservations = { /* Conectar con la pantalla cuando esté lista. */ },
                                    onPersonalData = { /* Conectar con Datos personales cuando esté lista. */ },
                                    onChangePassword = { /* Conectar con Cambiar contraseña cuando esté listo. */ },
                                    onLogout = { loggedEmail = null }
                                )

                                else -> ComingSoonScreen(sectionName = tab.label)
                            }
                        }
                    )
                }
            }
        }
    }
}
