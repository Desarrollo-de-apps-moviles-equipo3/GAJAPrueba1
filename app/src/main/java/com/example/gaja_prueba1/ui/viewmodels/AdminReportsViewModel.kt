package com.example.gaja_prueba1.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.AdminReportsRepository
import com.example.gaja_prueba1.model.AdminReservation
import com.example.gaja_prueba1.model.EstadoReserva
import com.example.gaja_prueba1.model.UsageReportRow
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class UsageReportType(val label: String, val groupByUser: Boolean) {
    USUARIOS("Por usuario", true),
    ESPACIOS("Por espacio", false)
}

data class AdminReportsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val type: UsageReportType = UsageReportType.USUARIOS,
    val rows: List<UsageReportRow> = emptyList(),
    val reservations: List<AdminReservation> = emptyList()
) {
    val totalApproved: Int get() = rows.sumOf { it.approvedReservations }
    val totalRejected: Int get() = rows.sumOf { it.rejectedReservations }
}

class AdminReportsViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: AdminReportsRepository = AdminReportsRepository()
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(AdminReportsUiState())
    val uiState: StateFlow<AdminReportsUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val reservations = repository.reservations()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = null,
                        reservations = reservations,
                        rows = buildRows(reservations, it.type)
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "No pudimos cargar los reportes. Comprueba la conexión e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun selectType(type: UsageReportType) = _uiState.update {
        it.copy(type = type, rows = buildRows(it.reservations, type))
    }

    private fun buildRows(
        reservations: List<AdminReservation>,
        type: UsageReportType
    ): List<UsageReportRow> {
        val decidedReservations = reservations.filter {
            it.status == EstadoReserva.APROBADA || it.status == EstadoReserva.RECHAZADA
        }
        val grouped = decidedReservations.groupBy {
            if (type.groupByUser) it.userEmail else it.space
        }
        return grouped.map { (label, records) ->
            UsageReportRow(
                label = label,
                approvedReservations = records.count {
                    it.status == EstadoReserva.APROBADA
                },
                rejectedReservations = records.count {
                    it.status == EstadoReserva.RECHAZADA
                }
            )
        }.sortedWith(
            compareByDescending<UsageReportRow> { it.approvedReservations + it.rejectedReservations }
                .thenBy { it.label.lowercase() }
        )
    }
}
