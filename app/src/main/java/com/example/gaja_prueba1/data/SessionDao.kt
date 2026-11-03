package com.example.gaja_prueba1.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE id = 1")
    suspend fun get(): SessionEntity?

    @Upsert
    suspend fun save(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clear()
}