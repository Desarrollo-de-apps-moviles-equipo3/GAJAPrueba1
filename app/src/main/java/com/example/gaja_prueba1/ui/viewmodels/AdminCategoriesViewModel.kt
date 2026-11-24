package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.CategoriesRepository
import com.example.gaja_prueba1.model.Category
import com.example.gaja_prueba1.model.CategoryException
import com.example.gaja_prueba1.model.RemoteCategoriesRepository
import com.example.gaja_prueba1.validation.CategoryValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryFormState(
    val editingId: Long? = null,
    val name: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false
) {
    val isEditing: Boolean get() = editingId != null
}

data class AdminCategoriesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val categories: List<Category> = emptyList(),
    val form: CategoryFormState? = null,
    val pendingDelete: Category? = null,
    val message: String? = null
)

class AdminCategoriesViewModel(
    private val repository: CategoriesRepository = RemoteCategoriesRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCategoriesUiState())
    val uiState: StateFlow<AdminCategoriesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val categories = repository.getAll()
                _uiState.update { it.copy(isLoading = false, categories = categories) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_ESPACIOS", "No se pudo cargar las categorías", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No pudimos cargar las categorías. Inténtalo de nuevo.")
                }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    // ---------- Formulario (Nueva categoría / Editar categoría) ----------

    fun onNewCategory() = _uiState.update { it.copy(form = CategoryFormState()) }

    fun onEditCategory(category: Category) = _uiState.update {
        it.copy(form = CategoryFormState(editingId = category.id, name = category.name))
    }

    fun onFormClose() = _uiState.update { it.copy(form = null) }

    fun onNameChange(value: String) = _uiState.update { state ->
        state.form?.let { state.copy(form = it.copy(name = value, nameError = null)) } ?: state
    }

    fun onSave() {
        val state = _uiState.value
        val form = state.form ?: return
        if (form.isSaving) return

        val currentName = state.categories.firstOrNull { it.id == form.editingId }?.name
        val error = CategoryValidator.validateName(form.name, state.categories.map { it.name }, currentName)
        if (error != null) {
            _uiState.update { it.copy(form = form.copy(nameError = error)) }
            return
        }

        _uiState.update { it.copy(form = form.copy(isSaving = true)) }
        viewModelScope.launch {
            try {
                if (form.editingId == null) {
                    repository.create(form.name.trim())
                } else {
                    repository.rename(form.editingId, form.name.trim())
                }
                val categories = repository.getAll()
                _uiState.update {
                    it.copy(
                        categories = categories,
                        form = null,
                        message = if (form.editingId == null) "Categoría creada" else "Cambios guardados"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { current ->
                    current.copy(
                        form = current.form?.copy(isSaving = false),
                        message = (e as? CategoryException)?.message ?: "No pudimos guardar la categoría"
                    )
                }
            }
        }
    }

    // ---------- Eliminar ----------

    fun onDeleteRequested(category: Category) = _uiState.update { it.copy(pendingDelete = category) }
    fun onDeleteCancelled() = _uiState.update { it.copy(pendingDelete = null) }

    fun onDeleteConfirmed() {
        val category = _uiState.value.pendingDelete ?: return
        _uiState.update { it.copy(pendingDelete = null) }
        viewModelScope.launch {
            try {
                repository.delete(category.id)
                val categories = repository.getAll()
                _uiState.update { it.copy(categories = categories, message = "Categoría eliminada") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Si la categoría tiene espacios, el servicio responde 409 y el mensaje llega aquí.
                _uiState.update {
                    it.copy(message = (e as? CategoryException)?.message ?: "No pudimos eliminar la categoría")
                }
            }
        }
    }
}
