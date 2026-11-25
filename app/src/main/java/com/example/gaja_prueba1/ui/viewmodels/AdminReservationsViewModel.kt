package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.AdminReservationsRepository
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.model.RejectionValidator
import com.example.gaja_prueba1.model.RemoteAdminReservationsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Pestañas internas de la sección Reservas del administrador. */
enum class ReservationsTab(val label: String, val status: EstadoReserva) {
    Pendientes("Pendientes", EstadoReserva.PENDIENTE),
    Aprobadas("Aprobadas", EstadoReserva.APROBADA),
    Rechazadas("Rechazadas", EstadoReserva.RECHAZADA)
}

data class RejectFormState(
    val reason: String = "",
    val error: String? = null,
    val isSaving: Boolean = false
)

/** Pantalla de resultado: "Solicitud aprobada" o "Solicitud rechazada". */
data class DecisionResult(val reservation: AdminReservation)

data class AdminReservationsUiState(
    val tab: ReservationsTab = ReservationsTab.Pendientes,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val items: Map<ReservationsTab, List<AdminReservation>> = emptyMap(),
    val pendingCount: Int = 0,
    val selected: AdminReservation? = null,
    val rejectForm: RejectFormState? = null,
    val isApproving: Boolean = false,
    val result: DecisionResult? = null,
    val message: String? = null
) {
    val current: List<AdminReservation> get() = items[tab].orEmpty()
}

class AdminReservationsViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AdminReservationsRepository = RemoteAdminReservationsRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AdminReservationsUiState())
    val uiState: StateFlow<AdminReservationsUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    /** Carga las tres listas, para que cambiar de pestaña sea inmediato. */
    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val lists = ReservationsTab.entries.associateWith { repository.getByStatus(it.status) }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        items = lists,
                        pendingCount = lists[ReservationsTab.Pendientes].orEmpty().size
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_RESERVAS", "No se pudieron cargar las reservas", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No pudimos cargar las solicitudes. Inténtalo de nuevo.")
                }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    fun onTabSelected(tab: ReservationsTab) = _uiState.update { it.copy(tab = tab) }

    // ---------- Detalle de solicitud ----------

    fun onOpenDetail(reservation: AdminReservation) =
        _uiState.update { it.copy(selected = reservation, rejectForm = null) }

    fun onCloseDetail() = _uiState.update { it.copy(selected = null, rejectForm = null) }

    // ---------- Aprobar ----------

    fun onApprove() {
        val state = _uiState.value
        val reservation = state.selected ?: return
        if (state.isApproving) return
        _uiState.update { it.copy(isApproving = true) }
        viewModelScope.launch {
            try {
                val updated = repository.approve(reservation.id)
                finishDecision(updated)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_RESERVAS", "No se pudo aprobar la solicitud", e)
                _uiState.update { it.copy(isApproving = false, message = "No pudimos aprobar la solicitud") }
            }
        }
    }

    // ---------- Rechazar (con motivo) ----------

    fun onStartReject() = _uiState.update { it.copy(rejectForm = RejectFormState()) }

    fun onCancelReject() = _uiState.update { it.copy(rejectForm = null) }

    fun onReasonChange(value: String) = _uiState.update { state ->
        state.rejectForm?.let {
            state.copy(rejectForm = it.copy(reason = value.take(RejectionValidator.MAX), error = null))
        } ?: state
    }

    fun onConfirmReject() {
        val state = _uiState.value
        val reservation = state.selected ?: return
        val form = state.rejectForm ?: return
        if (form.isSaving) return

        val error = RejectionValidator.validate(form.reason)
        if (error != null) {
            _uiState.update { it.copy(rejectForm = form.copy(error = error)) }
            return
        }

        _uiState.update { it.copy(rejectForm = form.copy(isSaving = true)) }
        viewModelScope.launch {
            try {
                val updated = repository.reject(reservation.id, form.reason)
                finishDecision(updated)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_RESERVAS", "No se pudo rechazar la solicitud", e)
                _uiState.update {
                    it.copy(
                        rejectForm = it.rejectForm?.copy(isSaving = false),
                        message = "No pudimos rechazar la solicitud"
                    )
                }
            }
        }
    }

    /** Muestra la pantalla de resultado y recarga las listas para reflejar el cambio. */
    private fun finishDecision(updated: AdminReservation) {
        _uiState.update {
            it.copy(
                selected = null,
                rejectForm = null,
                isApproving = false,
                result = DecisionResult(updated)
            )
        }
        load()
    }

    fun onResultClose() = _uiState.update { it.copy(result = null) }
}