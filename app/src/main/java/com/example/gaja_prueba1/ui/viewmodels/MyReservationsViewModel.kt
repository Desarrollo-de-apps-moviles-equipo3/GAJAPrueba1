package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import retrofit2.HttpException
import java.io.IOException
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.model.RemoteReservationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class MyReservationsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val cancelError: String? = null,
    val cancellationCompleted: Boolean = false,
    val cancelledReservation: ReservationResponse? = null
)



class MyReservationsViewModel : ViewModel() {
    private val repository = RemoteReservationsRepository()

    private val _reservations =
        MutableStateFlow<List<ReservationResponse>>(emptyList())
    val reservations: StateFlow<List<ReservationResponse>> =
        _reservations.asStateFlow()

    private val _uiState = MutableStateFlow(MyReservationsUiState())
    val uiState: StateFlow<MyReservationsUiState> = _uiState.asStateFlow()

    fun loadReservations(email: String) {
        if (email.isBlank()) {
            _uiState.value = MyReservationsUiState(
                error = "No se encontró el correo de la sesión"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                _reservations.value = repository.listarPorUsuario(email.trim())
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "No se pudieron cargar tus reservas. Comprueba tu conexión e inténtalo de nuevo."
                )
            }
        }
    }

    fun cancelReservation(id: Long, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                cancelError = null,
                cancellationCompleted = false
            )

            try {
                val cancelled = repository.cancelarReserva(id, email)
                _reservations.value = _reservations.value
                    .filterNot { it.id == cancelled.id } + cancelled
                _uiState.value = MyReservationsUiState(
                    cancellationCompleted = true,
                    cancelledReservation = cancelled
                )
                // El servidor ya confirmó la cancelación. Si falla esta actualización,
                // conservamos la confirmación y el cambio local de estado.
                try {
                    _reservations.value = repository.listarPorUsuario(email.trim())
                } catch (exception: CancellationException) {
                    throw exception
                } catch (_: Exception) {
                    // Conserva los datos ya confirmados por la respuesta de cancelación.
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                val message = when (exception) {
                    is IOException -> "Sin conexión. No se pudo confirmar la cancelación; tu reserva sigue activa. Inténtalo de nuevo cuando tengas internet."
                    is HttpException -> when (exception.code()) {
                        409 -> "No se puede cancelar: la reserva está dentro del plazo límite de 2 horas."
                        else -> "El servicio no pudo cancelar la reserva. Inténtalo de nuevo."
                    }
                    else -> "No se pudo cancelar la reserva. Inténtalo de nuevo."
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    cancelError = message
                )
            }
        }
    }

    fun clearCancelError() {
        _uiState.value = _uiState.value.copy(cancelError = null)
    }

    fun clearCancellationCompleted() {
        _uiState.value = _uiState.value.copy(
            cancellationCompleted = false,
            cancelledReservation = null
        )
    }
}
