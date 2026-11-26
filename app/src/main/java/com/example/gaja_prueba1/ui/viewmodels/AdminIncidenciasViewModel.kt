package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.AdminIncidencia
import com.example.gaja_prueba1.model.AdminIncidenciasRepository
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.AdminReservationsRepository
import com.example.gaja_prueba1.model.AdminSpacesRepository
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.model.RemoteAdminIncidenciasRepository
import com.example.gaja_prueba1.model.RemoteAdminReservationsRepository
import com.example.gaja_prueba1.model.RemoteAdminSpacesRepository
import com.example.gaja_prueba1.model.Space
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class IncidenciaFilter(val label: String) {
    PENDIENTES("Pendientes"),
    RESUELTAS("Resueltas"),
    TODAS("Todas")
}

enum class ReservationReferenceMode(val label: String) {
    REQUEST("Solicitud de reserva"),
    DATE("Día en particular")
}

data class AdminIncidenciasUiState(
    val incidencias: List<AdminIncidencia> = emptyList(),
    val filter: IncidenciaFilter = IncidenciaFilter.PENDIENTES,
    val selectedId: Long? = null,
    val creating: Boolean = false,
    val email: String = "",
    val category: String? = null,
    val spaces: List<Space> = emptyList(),
    val selectedSpaceId: String? = null,
    val reservations: List<AdminReservation> = emptyList(),
    val selectedReservationId: Long? = null,
    val reservationReferenceMode: ReservationReferenceMode = ReservationReferenceMode.REQUEST,
    val reservationDate: String? = null,
    val loadingReferences: Boolean = false,
    val spacesError: String? = null,
    val reservationsError: String? = null,
    val descripcion: String = "",
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val resolvingId: Long? = null,
    val error: String? = null,
    val message: String? = null
) {
    val visible: List<AdminIncidencia>
        get() = when (filter) {
            IncidenciaFilter.PENDIENTES -> incidencias.filterNot { it.resuelta }
            IncidenciaFilter.RESUELTAS -> incidencias.filter { it.resuelta }
            IncidenciaFilter.TODAS -> incidencias
        }

    val selected: AdminIncidencia?
        get() = incidencias.firstOrNull { it.id == selectedId }
}

class AdminIncidenciasViewModel(
    private val repository: AdminIncidenciasRepository = RemoteAdminIncidenciasRepository(),
    private val spacesRepository: AdminSpacesRepository = RemoteAdminSpacesRepository(),
    private val reservationsRepository: AdminReservationsRepository = RemoteAdminReservationsRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminIncidenciasUiState())
    val uiState: StateFlow<AdminIncidenciasUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val incidencias = repository.listar()
                _uiState.update { it.copy(incidencias = incidencias, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = "No pudimos cargar las incidencias. Revisa la conexión e inténtalo de nuevo.")
                }
            }
        }
    }

    fun onFilterChanged(filter: IncidenciaFilter) = _uiState.update { it.copy(filter = filter) }
    fun onEmailChanged(value: String) = _uiState.update { it.copy(email = value, error = null) }
    fun onCategoryChanged(value: String) = _uiState.update {
        it.copy(
            category = value,
            selectedSpaceId = null,
            selectedReservationId = null,
            reservationDate = null,
            reservationReferenceMode = ReservationReferenceMode.REQUEST,
            error = null
        )
    }

    fun onSpaceSelected(id: String) = _uiState.update { it.copy(selectedSpaceId = id, error = null) }

    fun onReservationSelected(id: Long) = _uiState.update { state ->
        val reservation = state.reservations.firstOrNull { it.id == id }
        state.copy(
            selectedReservationId = id,
            email = reservation?.userEmail ?: state.email,
            error = null
        )
    }

    fun onReservationReferenceModeChanged(mode: ReservationReferenceMode) = _uiState.update {
        it.copy(
            reservationReferenceMode = mode,
            selectedReservationId = null,
            reservationDate = null,
            error = null
        )
    }

    fun onReservationDateSelected(value: String) = _uiState.update {
        it.copy(reservationDate = value, error = null)
    }

    fun onDescriptionChanged(value: String) = _uiState.update {
        it.copy(descripcion = value.take(1000), error = null)
    }

    fun open(id: Long) = _uiState.update { it.copy(selectedId = id, creating = false, error = null) }
    fun openCreate(defaultEmail: String) {
        _uiState.update {
            it.copy(
                selectedId = null,
                creating = true,
                email = defaultEmail,
                category = null,
                selectedSpaceId = null,
                selectedReservationId = null,
                spaces = emptyList(),
                reservations = emptyList(),
                reservationReferenceMode = ReservationReferenceMode.REQUEST,
                reservationDate = null,
                descripcion = "",
                loadingReferences = true,
                spacesError = null,
                reservationsError = null,
                error = null
            )
        }
        retryReferenceOptions()
    }

    fun retryReferenceOptions() {
        _uiState.update {
            it.copy(loadingReferences = true, spacesError = null, reservationsError = null)
        }
        viewModelScope.launch {
            var spaces: List<com.example.gaja_prueba1.model.Space> = emptyList()
            var reservations: List<AdminReservation> = emptyList()
            var spacesError: String? = null
            var reservationsError: String? = null

            try {
                spaces = spacesRepository.getAll()
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                spacesError = "No pudimos cargar las canchas. Puedes reintentar."
            }
            try {
                reservations = EstadoReserva.entries.flatMap { status ->
                    reservationsRepository.getByStatus(status)
                }.sortedWith(compareBy({ it.date }, { it.slot }))
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                reservationsError = "No pudimos cargar las solicitudes. Puedes reintentar."
            }

            _uiState.update { state ->
                state.copy(
                    spaces = spaces,
                    reservations = reservations,
                    loadingReferences = false,
                    spacesError = spacesError,
                    reservationsError = reservationsError
                )
            }
        }
    }
    fun back() = _uiState.update { it.copy(selectedId = null, creating = false, error = null) }
    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    fun create() {
        val state = _uiState.value
        val email = state.email.trim()
        val category = state.category
        val description = state.descripcion.trim()
        val selectedSpace = state.spaces.firstOrNull { it.id == state.selectedSpaceId }
        val selectedReservation = state.reservations.firstOrNull { it.id == state.selectedReservationId }
        val reference = when (category) {
            "Canchas" -> selectedSpace?.let { "Canchas: ${it.name}" }
            "Reservas" -> when (state.reservationReferenceMode) {
                ReservationReferenceMode.REQUEST -> selectedReservation?.let {
                    "Reservas: solicitud #${it.id} · ${it.space} · ${it.date} · ${it.slot}"
                }
                ReservationReferenceMode.DATE -> state.reservationDate?.let { "Reservas: día $it" }
            }
            "Cuenta", "Otro" -> category
            else -> null
        }
        val error = when {
            email.isBlank() -> "Indica el correo relacionado con la incidencia"
            !email.contains('@') -> "Revisa el correo ingresado"
            category == null -> "Selecciona una categoría"
            category == "Canchas" && selectedSpace == null -> "Selecciona la cancha relacionada"
            category == "Reservas" && state.reservationReferenceMode == ReservationReferenceMode.REQUEST && selectedReservation == null ->
                "Selecciona la solicitud de reserva"
            category == "Reservas" && state.reservationReferenceMode == ReservationReferenceMode.DATE && state.reservationDate == null ->
                "Selecciona el día relacionado"
            description.length < 10 -> "Describe el problema con al menos 10 caracteres"
            else -> null
        }
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        if (state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                val created = repository.crear(email, reference.orEmpty(), description)
                _uiState.update {
                    it.copy(
                        incidencias = listOf(created) + it.incidencias.filterNot { item -> item.id == created.id },
                        filter = IncidenciaFilter.TODAS,
                        selectedId = created.id,
                        creating = false,
                        isSaving = false,
                        message = "Incidencia registrada"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(isSaving = false, error = "No se pudo guardar la incidencia. Inténtalo de nuevo.")
                }
            }
        }
    }

    fun resolve(id: Long) {
        if (_uiState.value.resolvingId != null) return
        viewModelScope.launch {
            _uiState.update { it.copy(resolvingId = id, error = null) }
            try {
                val updated = repository.resolver(id)
                _uiState.update { state ->
                    state.copy(
                        incidencias = state.incidencias.map { if (it.id == id) updated else it },
                        selectedId = null,
                        filter = IncidenciaFilter.RESUELTAS,
                        resolvingId = null,
                        message = "Incidencia marcada como resuelta"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _uiState.update {
                    it.copy(resolvingId = null, error = "No se pudo actualizar la incidencia. Inténtalo de nuevo.")
                }
            }
        }
    }
}
