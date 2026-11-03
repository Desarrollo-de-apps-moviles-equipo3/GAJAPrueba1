package com.example.gaja_prueba1.model

import com.example.gaja_prueba1.data.ReservationDao
import com.example.gaja_prueba1.data.ReservationEntity
import kotlinx.coroutines.flow.Flow

class ReservationRepository(private val dao: ReservationDao) {

    fun observeAll(): Flow<List<ReservationEntity>> =
        dao.observeALL()

    suspend fun reservationsOn(isoDate: String): List<ReservationEntity> =
        dao.gateByDate(isoDate)

    fun observeReservationsOn(isoDate: String): Flow<List<ReservationEntity>> =
        dao.observeByDate(isoDate)

    suspend fun save(reservation: ReservationEntity) {
        dao.insert(reservation)
    }

    suspend fun delete(reservation: ReservationEntity) {
        dao.delete(reservation)
    }
}