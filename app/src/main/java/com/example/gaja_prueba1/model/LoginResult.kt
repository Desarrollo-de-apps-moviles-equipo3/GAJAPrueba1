package com.example.gaja_prueba1.model

//esta clase define el resultado de la operación de login
sealed class LoginResult {
    data class Success(val token: String, val userId: String) : LoginResult()
    data class Error(val exception: Exception) : LoginResult()
}
