package com.example.gaja_prueba1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.gaja_prueba1.data.AppDatabase
import com.example.gaja_prueba1.data.SessionEntity
import com.example.gaja_prueba1.model.SessionRepository
import com.example.gaja_prueba1.ui.screen.BottomTab
import com.example.gaja_prueba1.ui.screen.ComingSoonScreen
import com.example.gaja_prueba1.ui.screen.LoginScreen
import com.example.gaja_prueba1.ui.screen.MainShellScreen
import com.example.gaja_prueba1.ui.screen.profile.ProfileFlow
import com.example.gaja_prueba1.ui.screen.MyReservationsScreen
import com.example.gaja_prueba1.ui.screen.AlertasScreen
import com.example.gaja_prueba1.ui.screen.ProfileScreen
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.theme.SharedPrefsThemeStorage
import com.example.gaja_prueba1.ui.theme.ThemeSettings
import kotlinx.coroutines.launch
import com.example.gaja_prueba1.ui.screen.RecoverAccessScreen
import com.example.gaja_prueba1.ui.screen.RegisterProfileScreen
import com.example.gaja_prueba1.ui.screen.AdminShell

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch {
            com.example.gaja_prueba1.data.remote.ConnectionCheck.ping(
                com.example.gaja_prueba1.data.remote.ApiConfig.RESERVAS_URL
            )
        }
        val sessionRepository = SessionRepository(
            AppDatabase.get(applicationContext).sessionDao()
        )

        // El modo claro/oscuro se lee antes de dibujar para que la app no parpadee al abrir.
        ThemeSettings.init(SharedPrefsThemeStorage(applicationContext))

        setContent {
            val dark = ThemeSettings.isDark

            // Los íconos de la barra de estado y de navegación se ajustan al modo elegido.
            DisposableEffect(dark) {
                val bars = if (dark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
                onDispose { }
            }

            GAJAPrueba1Theme(darkTheme = dark) {

                var activeSession by remember {
                    mutableStateOf<SessionEntity?>(null)
                }

                var isCheckingSession by remember {
                    mutableStateOf(true)
                }

                var showRecover by remember {
                    mutableStateOf(false)
                }

                var showRegister by remember {
                    mutableStateOf(false)
                }

                // Comprueba si hay una sesión guardada.
                LaunchedEffect(Unit) {
                    activeSession = try {
                        sessionRepository.currentSession()
                    } catch (exception: Exception) {
                        null
                    }

                    isCheckingSession = false
                }

                if (isCheckingSession) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {

                    AnimatedContent(
                        targetState = activeSession,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith
                                    fadeOut(tween(200))
                        },
                        label = "loginToHomeTransition"
                    ) { session ->

                        if (session == null) {

                            Scaffold(
                                modifier = Modifier.fillMaxSize()
                            ) { innerPadding ->

                                Surface(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding),
                                    color = MaterialTheme.colorScheme.background
                                ) {
                                    if (showRecover) {
                                        RecoverAccessScreen(
                                            onBack = { showRecover = false }
                                        )
                                    } else if (showRegister) {
                                        RegisterProfileScreen(
                                            onBack = { showRegister = false }
                                        )
                                    } else {
                                        LoginScreen(
                                            onLoginSuccess = {
                                                lifecycleScope.launch {
                                                    activeSession =
                                                        sessionRepository.currentSession()
                                                }
                                            },
                                            onForgotPassword = { showRecover = true },
                                            onCreateProfile = { showRegister = true }
                                        )
                                    }
                                }
                            }

                        } else {

                            val userName = session.email
                                .substringBefore("@")
                                .replace("_", " ")
                                .replace(".", " ")
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .joinToString(" ") { part ->
                                    part.replaceFirstChar {
                                        it.uppercase()
                                    }
                                }


                            val onLogout: () -> Unit = {
                                lifecycleScope.launch {
                                    try {
                                        sessionRepository.closeSession()
                                        activeSession = null
                                    } catch (exception: Exception) {
                                        android.util.Log.e(
                                            "GAJA_SESSION",
                                            "Error al cerrar la sesión",
                                            exception
                                        )
                                    }
                                }
                            }

                            if (session.role == "Administrador") {

                                AdminShell(
                                    userName = userName,
                                    userEmail = session.email,
                                    onLogout = onLogout
                                )

                            } else {

                                MainShellScreen(
                                    userName = userName,
                                    userEmail = session.email,
                                    otherTabs = { tab, onOpenReservations, onBackHome, _ ->
                                        when (tab) {
                                            BottomTab.Reservas -> {
                                                MyReservationsScreen(
                                                    email = session.email,
                                                    onBack = onBackHome
                                                )
                                            }

                                            BottomTab.Perfil -> {
                                                ProfileFlow(
                                                    fullName = userName,
                                                    email = session.email,
                                                    role = session.role,
                                                    onMyReservations = onOpenReservations,
                                                    onLogout = onLogout
                                                )
                                            }

                                            BottomTab.Alertas -> {
                                                AlertasScreen(
                                                    email = session.email,
                                                    onBack = onBackHome
                                                )
                                            }

                                            else -> {
                                                ComingSoonScreen(sectionName = tab.label)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}