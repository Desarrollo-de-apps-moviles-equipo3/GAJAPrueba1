package com.example.gaja_prueba1.model

sealed class LoginResult {

    //esta clase define el resultado de la operación de login

    data class Success(
        val token: String,
        val userId: String,
        val role: String
    ) : LoginResult()

    data class Error(
        val exception: Exception
    ) : LoginResult()
}