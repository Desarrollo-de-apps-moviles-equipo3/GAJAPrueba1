package com.example.gaja_prueba1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Insert
    suspend fun insert(reservation: ReservationEntity): Long

    @Query("SELECT * FROM reservation ORDER BY date, timeSlot")
    fun observeALL(): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservation WHERE date = :date")
    suspend fun gateByDate(date: String): List<ReservationEntity>

    /**Próximas: fecha de hoy en adelante (day = "yyyy-MM-dd"). */
    @Query("SELECT COUNT(*) FROM reservation WHERE date >= :today")
    fun observeUpcomingCount(today: String): Flow<Int>

    /** Pasadas: fecha anterior a hoy */
    @Query("SELECT COUNT(*) FROM reservation WHERE date < :today")
    fun observePastCount(today: String): Flow<Int>
}