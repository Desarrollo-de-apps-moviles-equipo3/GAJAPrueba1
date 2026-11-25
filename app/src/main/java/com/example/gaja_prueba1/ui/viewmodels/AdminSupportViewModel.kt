package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class TicketStatus(val label: String, val plural: String) {
    NUEVA("Nueva", "Nuevas"),
    EN_REVISION("En revisión", "En revisión"),
    RESUELTA("Resuelta", "Resueltas")
}

data class SupportTicket(
    val code: String,
    val title: String,
    val author: String,
    val whenText: String,
    val message: String,
    val status: TicketStatus,
    val lastReply: String? = null
) {
    val summary: String
        get() = when (status) {
            TicketStatus.NUEVA -> "Sin respuesta"
            TicketStatus.EN_REVISION -> message
            TicketStatus.RESUELTA -> "Respondida por el administrador"
        }
}

data class ContactInfo(val email: String, val phone: String, val hours: String)

object ContactValidator {
    fun validate(c: ContactInfo): String? = when {
        !c.email.contains("@") || c.email.trim().length < 5 -> "Ingresa un correo válido"
        c.phone.isBlank() -> "Ingresa un teléfono"
        c.hours.isBlank() -> "Ingresa el horario de atención"
        else -> null
    }
}

data class SupportUiState(
    val filter: TicketStatus = TicketStatus.NUEVA,
    val tickets: List<SupportTicket> = sampleTickets(),
    val selectedCode: String? = null,
    val reply: String = "",
    val contact: ContactInfo = ContactInfo(
        "reservas@escuelamilitar.cl", "+56 2 2345 6789", "Lunes a viernes · 08:00–18:00"
    ),
    val editingContact: Boolean = false,
    val contactDraft: ContactInfo = contact,
    val contactError: String? = null,
    val message: String? = null
) {
    val visible: List<SupportTicket> get() = tickets.filter { it.status == filter }
    val selected: SupportTicket? get() = tickets.firstOrNull { it.code == selectedCode }
}

private fun sampleTickets() = listOf(
    SupportTicket(
        "SOP-004", "No puedo iniciar sesión", "Camila Rojas · Oficial", "Hoy 09:05",
        "No puedo ingresar con mi usuario institucional.", TicketStatus.NUEVA
    ),
    SupportTicket(
        "SOP-003", "Cambio de horario RSV-2026-002", "Gabriel Alcalá · Cadete", "Hoy 08:12",
        "¿Se puede mover la reserva del domingo al sábado?", TicketStatus.EN_REVISION
    ),
    SupportTicket(
        "SOP-002", "No recibo notificaciones push", "Gabriel Alcalá · Cadete", "Hace 3 días",
        "No me llegan las notificaciones.", TicketStatus.RESUELTA
    )
)

/** Soporte del administrador. Por ahora trabaja con datos locales (no hay servicio de soporte aún). */
class AdminSupportViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    fun onFilter(status: TicketStatus) = _uiState.update { it.copy(filter = status) }

    fun onOpen(code: String) = _uiState.update { it.copy(selectedCode = code, reply = "") }

    fun onBack() = _uiState.update {
        it.copy(selectedCode = null, editingContact = false, contactError = null)
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    // ---- Detalle de la solicitud ----
    fun onReplyChange(value: String) = _uiState.update { it.copy(reply = value.take(500)) }

    fun onSendReply() {
        val s = _uiState.value
        val code = s.selectedCode ?: return
        val text = s.reply.trim()
        if (text.isEmpty()) return
        _uiState.update { st ->
            st.copy(
                tickets = st.tickets.map {
                    if (it.code == code) it.copy(status = TicketStatus.EN_REVISION, lastReply = text) else it
                },
                reply = "",
                message = "Respuesta enviada"
            )
        }
    }

    fun onResolve() {
        val code = _uiState.value.selectedCode ?: return
        _uiState.update { st ->
            st.copy(
                tickets = st.tickets.map {
                    if (it.code == code) it.copy(status = TicketStatus.RESUELTA) else it
                },
                selectedCode = null,
                filter = TicketStatus.RESUELTA,
                message = "Solicitud marcada como resuelta"
            )
        }
    }

    // ---- Contacto visible ----
    fun onEditContact() = _uiState.update {
        it.copy(editingContact = true, contactDraft = it.contact, contactError = null)
    }

    fun onContactChange(draft: ContactInfo) = _uiState.update {
        it.copy(contactDraft = draft, contactError = null)
    }

    fun onSaveContact() {
        val draft = _uiState.value.contactDraft
        val error = ContactValidator.validate(draft)
        if (error != null) {
            _uiState.update { it.copy(contactError = error) }
            return
        }
        _uiState.update {
            it.copy(
                contact = draft.copy(email = draft.email.trim(), phone = draft.phone.trim(), hours = draft.hours.trim()),
                editingContact = false,
                message = "Contacto actualizado"
            )
        }
    }
}