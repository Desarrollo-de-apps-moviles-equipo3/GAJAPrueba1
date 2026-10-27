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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.gaja_prueba1.ui.screen.LoginScreen
import com.example.gaja_prueba1.ui.screen.MainShellScreen
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GAJAPrueba1Theme {
                var loggedEmail by rememberSaveable { mutableStateOf<String?>(null) }
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
                    MainShellScreen(
                        userName = email.substringBefore("@")
                            .replace("_", " ")
                            .replaceFirstChar { it.uppercase() }
                    )
                }
            }
        }
    }
}