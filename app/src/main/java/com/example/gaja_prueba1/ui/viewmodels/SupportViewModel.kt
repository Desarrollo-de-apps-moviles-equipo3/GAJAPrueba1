package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.RemoteSupportRepository
import com.example.gaja_prueba1.model.SupportException
import com.example.gaja_prueba1.model.SupportRepository
import com.example.gaja_prueba1.model.SupportRequest
import com.example.gaja_prueba1.validation.ProfileValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewRequestForm(
    val category: String = "",
    val description: String = "",
    val categoryError: String? = null,
    val descriptionError: String? = null,
    val isSaving: Boolean = false
)

data class SupportUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val requests: List<SupportRequest> = emptyList(),
    val form: NewRequestForm = NewRequestForm(),
    /** Se enciende cuando la solicitud se envió, para abrir el seguimiento. */
    val sent: Boolean = false,
    val message: String? = null
)

/** "Ayuda y soporte": las solicitudes que la persona envió y el formulario para crear una nueva. */
class SupportViewModel(
    private val repository: SupportRepository = RemoteSupportRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SupportUiState())
    val uiState: StateFlow<SupportUiState> = _uiState.asStateFlow()

    /** Correo de la persona a la que pertenecen las solicitudes que hay en pantalla. */
    private var loadedFor: String? = null

    fun load(email: String) {
        viewModelScope.launch {
            // El ViewModel sobrevive al cierre de sesión: si entra otra persona, partimos de cero.
            if (loadedFor != null && !loadedFor.equals(email, ignoreCase = true)) {
                _uiState.value = SupportUiState()
            }
            loadedFor = email
            _uiState.update { it.copy(isLoading = it.requests.isEmpty(), errorMessage = null) }
            try {
                val requests = repository.mine(email)
                _uiState.update { it.copy(isLoading = false, requests = requests) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_AYUDA", "No se pudo cargar las solicitudes", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No pudimos cargar tus solicitudes. Inténtalo de nuevo.")
                }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    fun onSentHandled() = _uiState.update { it.copy(sent = false) }

    // ---------- Nueva solicitud ----------

    fun onNewRequest() = _uiState.update { it.copy(form = NewRequestForm(), sent = false) }

    fun onCategoryChange(value: String) = updateForm { it.copy(category = value, categoryError = null) }

    fun onDescriptionChange(value: String) = updateForm {
        it.copy(description = value.take(ProfileValidator.MAX_REQUEST_LENGTH), descriptionError = null)
    }

    private fun updateForm(change: (NewRequestForm) -> NewRequestForm) =
        _uiState.update { it.copy(form = change(it.form)) }

    fun onSend(email: String) {
        val form = _uiState.value.form
        if (form.isSaving) return

        val checked = form.copy(
            categoryError = ProfileValidator.validateRequestCategory(form.category),
            descriptionError = ProfileValidator.validateRequestDescription(form.description)
        )
        if (checked.categoryError != null || checked.descriptionError != null) {
            _uiState.update { it.copy(form = checked) }
            return
        }

        updateForm { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val request = repository.create(email, form.category, form.description)
                _uiState.update {
                    it.copy(
                        requests = listOf(request) + it.requests.filterNot { prior -> prior.id == request.id },
                        form = NewRequestForm(),
                        sent = true,
                        message = "Solicitud enviada"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateForm { it.copy(isSaving = false) }
                _uiState.update {
                    it.copy(message = (e as? SupportException)?.message ?: "No pudimos enviar la solicitud")
                }
            }
        }
    }
}
