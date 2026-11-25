package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.AdminSpacesRepository
import com.example.gaja_prueba1.model.RemoteAdminSpacesRepository
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.validation.AdminSpaceValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SpaceFormState(
    val editingId: String? = null,
    val name: String = "",
    val category: String = "",
    val capacity: String = "",
    val description: String = "",
    val nameError: String? = null,
    val categoryError: String? = null,
    val capacityError: String? = null,
    val isSaving: Boolean = false
) {
    val isEditing: Boolean get() = editingId != null
}

data class AdminSpacesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val spaces: List<Space> = emptyList(),
    val categories: List<String> = emptyList(),
    val showDisabled: Boolean = false,
    val form: SpaceFormState? = null,
    val pendingDisable: Space? = null,
    val pendingDelete: Space? = null,
    val message: String? = null
) {
    val active: List<Space> get() = spaces.filter { it.available }
    val disabled: List<Space> get() = spaces.filter { !it.available }
    val visible: List<Space> get() = if (showDisabled) disabled else active
}

/**
 * El repositorio entra por el constructor: la app usa el que habla con el servicio (por defecto)
 * y los tests le pasan uno falso. Lo que el admin cambia queda guardado y lo ve el usuario en el Inicio.
 */
class AdminSpacesViewModel(
    private val repository: AdminSpacesRepository = RemoteAdminSpacesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSpacesUiState())
    val uiState: StateFlow<AdminSpacesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                refresh(repository)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_ESPACIOS", "No se pudo cargar los espacios", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No pudimos cargar los espacios. Inténtalo de nuevo.")
                }
            }
        }
    }

    /** Recarga sin spinner: se usa al volver desde "Categorías", por si cambió algún nombre. */
    fun refreshQuietly() {
        viewModelScope.launch {
            try {
                refresh(repository)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Si falla, se queda lo que ya estaba en pantalla.
            }
        }
    }

    private suspend fun refresh(repo: AdminSpacesRepository) {
        val spaces = repo.getAll()
        val categories = repo.categories()
        _uiState.update { it.copy(isLoading = false, spaces = spaces, categories = categories) }
    }

    // ---------- Lista ----------

    fun onShowDisabled(show: Boolean) = _uiState.update { it.copy(showDisabled = show) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    // ---------- Formulario (Nuevo espacio / Editar espacio) ----------

    fun onNewSpace() = _uiState.update { it.copy(form = SpaceFormState()) }

    fun onEditSpace(space: Space) = _uiState.update {
        it.copy(
            form = SpaceFormState(
                editingId = space.id,
                name = space.name,
                category = space.category,
                capacity = space.capacity.toString(),
                description = space.description
            )
        )
    }

    fun onFormClose() = _uiState.update { it.copy(form = null) }

    fun onNameChange(value: String) = updateForm { it.copy(name = value, nameError = null) }
    fun onCategoryChange(value: String) = updateForm { it.copy(category = value, categoryError = null) }
    fun onCapacityChange(value: String) =
        updateForm { it.copy(capacity = value.filter(Char::isDigit).take(3), capacityError = null) }
    fun onDescriptionChange(value: String) = updateForm { it.copy(description = value) }

    private fun updateForm(change: (SpaceFormState) -> SpaceFormState) {
        _uiState.update { state -> state.form?.let { state.copy(form = change(it)) } ?: state }
    }

    fun onSave() {
        val form = _uiState.value.form ?: return
        if (form.isSaving) return

        val nameError = AdminSpaceValidator.validateName(form.name)
        val categoryError = AdminSpaceValidator.validateCategory(form.category)
        val capacityError = AdminSpaceValidator.validateCapacity(form.capacity)
        if (nameError != null || categoryError != null || capacityError != null) {
            updateForm {
                it.copy(nameError = nameError, categoryError = categoryError, capacityError = capacityError)
            }
            return
        }

        val repo = repository
        updateForm { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                if (form.editingId == null) {
                    repo.create(
                        Space(
                            id = "",
                            name = form.name.trim(),
                            category = form.category,
                            description = form.description.trim(),
                            capacity = form.capacity.trim().toInt(),
                            available = true,
                            colorHex = "#2F6B4F"
                        )
                    )
                } else {
                    val current = _uiState.value.spaces.first { it.id == form.editingId }
                    repo.update(
                        current.copy(
                            name = form.name.trim(),
                            category = form.category,
                            description = form.description.trim(),
                            capacity = form.capacity.trim().toInt()
                        )
                    )
                }
                refresh(repo)
                _uiState.update {
                    it.copy(
                        form = null,
                        message = if (form.editingId == null) "Espacio creado" else "Cambios guardados"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updateForm { it.copy(isSaving = false) }
                _uiState.update { it.copy(message = "No pudimos guardar el espacio") }
            }
        }
    }

    // ---------- Deshabilitar / habilitar ----------

    fun onDisableRequested(space: Space) = _uiState.update { it.copy(pendingDisable = space) }
    fun onDisableCancelled() = _uiState.update { it.copy(pendingDisable = null) }

    fun onDisableConfirmed() {
        val space = _uiState.value.pendingDisable ?: return
        _uiState.update { it.copy(pendingDisable = null) }
        changeAvailability(space, available = false, done = "Espacio deshabilitado")
    }

    fun onEnable(space: Space) = changeAvailability(space, available = true, done = "Espacio habilitado")

    private fun changeAvailability(space: Space, available: Boolean, done: String) {
        val repo = repository
        viewModelScope.launch {
            try {
                repo.setAvailable(space.id, available)
                refresh(repo)
                _uiState.update { it.copy(message = done) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "No pudimos actualizar el espacio") }
            }
        }
    }

    // ---------- Eliminar ----------

    fun onDeleteRequested(space: Space) = _uiState.update { it.copy(pendingDelete = space) }
    fun onDeleteCancelled() = _uiState.update { it.copy(pendingDelete = null) }

    fun onDeleteConfirmed() {
        val space = _uiState.value.pendingDelete ?: return
        val repo = repository
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            try {
                repo.delete(space.id)
                refresh(repo)
                _uiState.update { it.copy(message = "Espacio eliminado") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "No pudimos eliminar el espacio") }
            }
        }
    }
}
