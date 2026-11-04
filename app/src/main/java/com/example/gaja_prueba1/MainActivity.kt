package com.example.gaja_prueba1

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import com.example.gaja_prueba1.ui.screen.MyReservationsScreen
import com.example.gaja_prueba1.ui.screen.ProfileScreen
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sessionRepository = SessionRepository(
            AppDatabase.get(applicationContext).sessionDao()
        )

        setContent {
            GAJAPrueba1Theme {

                var activeSession by remember {
                    mutableStateOf<SessionEntity?>(null)
                }

                var isCheckingSession by remember {
                    mutableStateOf(true)
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
                                    LoginScreen(
                                        onLoginSuccess = {
                                            lifecycleScope.launch {
                                                activeSession =
                                                    sessionRepository.currentSession()
                                            }
                                        }
                                    )
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


                            MainShellScreen(
                                userName = userName,
                                otherTabs = { tab, onOpenReservations, onReserveClick ->
                                    when (tab) {
                                        BottomTab.Reservas -> {
                                            MyReservationsScreen(
                                                onBack = onOpenReservations

                                            )
                                        }

                                        BottomTab.Perfil -> {
                                            ProfileScreen(
                                                fullName = userName,
                                                email = session.email,
                                                onMyReservations = onOpenReservations,
                                                onPersonalData = {},
                                                onChangePassword = {},
                                                onLogout = {
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
                                            )
                                        }

                                        else -> {
                                            ComingSoonScreen(
                                                sectionName = tab.label
                                            )
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