package com.example.gaja_prueba1.model

import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay


class AuthRepository {
    suspend fun loginUser(email: String, password: String): LoginResult = withContext(Dispatchers.IO) {
        //simulación de la espera de respuesta de un servidor al ingreso del login con email y passw

        try {
            delay(2000)
            if(email == "profe_exe@duoc.cl" && password == "12345")
                LoginResult.Success(token = "fake-jwt-123", userId = "user01")
            else
                LoginResult.Error(Exception("Invalid credentials"))
        }catch(e: Exception){
            LoginResult.Error(e)
        }
    }
}