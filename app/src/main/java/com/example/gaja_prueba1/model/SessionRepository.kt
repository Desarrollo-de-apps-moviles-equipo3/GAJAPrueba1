package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.SessionDao
import com.example.gaja_prueba1.data.SessionEntity

class SessionRepository(
    private val sessionDao: SessionDao
) {

    suspend fun saveSession(
        email: String,
        role: String
    ) {
        sessionDao.save(
            SessionEntity(
                id = 1,
                email = email.trim(),
                role = role
            )
        )
    }

    suspend fun currentSession(): SessionEntity? {
        return sessionDao.get()
    }

    suspend fun closeSession() {
        sessionDao.clear()
    }
}
