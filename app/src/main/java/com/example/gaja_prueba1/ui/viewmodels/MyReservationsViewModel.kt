package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.data.AppDatabase
import com.example.gaja_prueba1.data.ReservationEntity
import com.example.gaja_prueba1.model.ReservationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyReservationsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = ReservationRepository(
        AppDatabase.get(application).reservationDao()
    )

    val reservations: StateFlow<List<ReservationEntity>> =
        repository.observeAll().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun deleteReservation(
        reservation: ReservationEntity
    ) {
        viewModelScope.launch {
            repository.delete(reservation)
        }
    }
}