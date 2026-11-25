package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.EstadoPerfil
import com.example.gaja_prueba1.model.RemoteUsuariosRepository
import com.example.gaja_prueba1.model.Usuario
import com.example.gaja_prueba1.model.UsuarioException
import com.example.gaja_prueba1.model.UsuariosRepository
import com.example.gaja_prueba1.validation.UserValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Los cuatro roles de la app más el administrador. Son los mismos que acepta el servicio. */
val USER_ROLES = listOf("Cadete", "Oficial", "Oficial de semana", "Institución externa", "Administrador")

/**
 * Pestañas de la lista de perfiles. Cada una muestra los usuarios de un estado.
 * Para sumar otra (por ejemplo "Rechazados") basta con agregar una línea aquí.
 */
enum class ProfileFilter(val label: String, val estado: EstadoPerfil) {
    Pendientes("Pendientes", EstadoPerfil.Pendiente),
    Aprobados("Aprobados", EstadoPerfil.Aprobado),
    Desactivados("Desactivados", EstadoPerfil.Desactivado)
}

data class UserFormState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val rol: String = "",
    val telefono: String = "",
    val institucion: String = "",
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val rolError: String? = null,
    val telefonoError: String? = null,
    val institucionError: String? = null,
    val isSaving: Boolean = false
) {
    val needsInstitution: Boolean get() = rol == UserValidator.EXTERNAL_ROLE
}

/** Diálogo "Rechazar perfil": el motivo es obligatorio y le llega al usuario. */
data class RejectState(
    val user: Usuario,
    val reason: String = "",
    val error: String? = null,
    val isSaving: Boolean = false
)

data class AdminProfilesUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val users: List<Usuario> = emptyList(),
    val filter: ProfileFilter = ProfileFilter.Pendientes,
    val selectedId: Long? = null,
    val form: UserFormState? = null,
    val reject: RejectState? = null,
    val pendingDeactivate: Usuario? = null,
    val message: String? = null
) {
    fun countOf(f: ProfileFilter): Int = users.count { it.estado == f.estado }
    val visible: List<Usuario> get() = users.filter { it.estado == filter.estado }

    /** El perfil que se está viendo en detalle; sale de la lista para que se actualice solo tras un cambio. */
    val selected: Usuario? get() = users.firstOrNull { it.id == selectedId }
}

class AdminProfilesViewModel(
    private val repository: UsuariosRepository = RemoteUsuariosRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProfilesUiState())
    val uiState: StateFlow<AdminProfilesUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val users = repository.getAll()
                _uiState.update { it.copy(isLoading = false, users = users) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_USUARIOS", "No se pudo cargar los perfiles", e)
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "No pudimos cargar los perfiles. Inténtalo de nuevo.")
                }
            }
        }
    }

    /** Recarga sin spinner: se usa al volver a la pestaña, por si llegó un registro nuevo. */
    fun refreshQuietly() {
        viewModelScope.launch {
            try {
                val users = repository.getAll()
                _uiState.update { it.copy(users = users) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Si falla, se queda lo que ya estaba en pantalla.
            }
        }
    }

    fun onFilterChange(filter: ProfileFilter) = _uiState.update { it.copy(filter = filter) }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    // ---------- Detalle ----------

    fun onOpenDetail(user: Usuario) = _uiState.update { it.copy(selectedId = user.id) }

    fun onCloseDetail() = _uiState.update { it.copy(selectedId = null) }

    // ---------- Aprobar / activar ----------

    fun onApprove(user: Usuario) = changeState(
        user = user,
        done = "Perfil aprobado",
        failed = "No pudimos aprobar el perfil"
    ) { repository.approve(user.id) }

    fun onActivate(user: Usuario) = changeState(
        user = user,
        done = "Perfil activado",
        failed = "No pudimos activar el perfil"
    ) { repository.activate(user.id) }

    // ---------- Rechazar (pide motivo) ----------

    fun onRejectRequested(user: Usuario) = _uiState.update { it.copy(reject = RejectState(user)) }

    fun onRejectReasonChange(value: String) = _uiState.update { state ->
        state.reject?.let { state.copy(reject = it.copy(reason = value.take(200), error = null)) } ?: state
    }

    fun onRejectCancelled() = _uiState.update { it.copy(reject = null) }

    fun onRejectConfirmed() {
        val reject = _uiState.value.reject ?: return
        if (reject.isSaving) return
        val error = UserValidator.validateRejectReason(reject.reason)
        if (error != null) {
            _uiState.update { it.copy(reject = reject.copy(error = error)) }
            return
        }
        _uiState.update { it.copy(reject = reject.copy(isSaving = true)) }
        viewModelScope.launch {
            try {
                repository.reject(reject.user.id, reject.reason)
                val users = repository.getAll()
                _uiState.update {
                    it.copy(users = users, reject = null, selectedId = null, message = "Perfil rechazado")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        reject = it.reject?.copy(isSaving = false),
                        message = (e as? UsuarioException)?.message ?: "No pudimos rechazar el perfil"
                    )
                }
            }
        }
    }

    // ---------- Desactivar (pide confirmación) ----------

    fun onDeactivateRequested(user: Usuario) = _uiState.update { it.copy(pendingDeactivate = user) }

    fun onDeactivateCancelled() = _uiState.update { it.copy(pendingDeactivate = null) }

    fun onDeactivateConfirmed() {
        val user = _uiState.value.pendingDeactivate ?: return
        _uiState.update { it.copy(pendingDeactivate = null) }
        changeState(
            user = user,
            done = "Usuario desactivado",
            failed = "No pudimos desactivar al usuario"
        ) { repository.deactivate(user.id) }
    }

    private fun changeState(user: Usuario, done: String, failed: String, action: suspend () -> Usuario) {
        viewModelScope.launch {
            try {
                action()
                val users = repository.getAll()
                // Volvemos a la lista: el perfil cambió de pestaña y el detalle viejo ya no corresponde.
                _uiState.update { it.copy(users = users, selectedId = null, message = done) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_USUARIOS", "Falló el cambio de estado de ${user.email}", e)
                _uiState.update { it.copy(message = (e as? UsuarioException)?.message ?: failed) }
            }
        }
    }

    // ---------- Crear usuario ----------

    fun onNewUser() = _uiState.update { it.copy(form = UserFormState()) }

    fun onFormClose() = _uiState.update { it.copy(form = null) }

    fun onNameChange(value: String) = updateForm { it.copy(nombre = value, nombreError = null) }
    fun onEmailChange(value: String) = updateForm { it.copy(email = value, emailError = null) }
    fun onPasswordChange(value: String) = updateForm { it.copy(password = value, passwordError = null) }
    fun onRoleChange(value: String) = updateForm { it.copy(rol = value, rolError = null, institucionError = null) }
    fun onPhoneChange(value: String) = updateForm { it.copy(telefono = value, telefonoError = null) }
    fun onInstitutionChange(value: String) = updateForm { it.copy(institucion = value, institucionError = null) }

    private fun updateForm(change: (UserFormState) -> UserFormState) {
        _uiState.update { state -> state.form?.let { state.copy(form = change(it)) } ?: state }
    }

    fun onSave() {
        val form = _uiState.value.form ?: return
        if (form.isSaving) return

        val checked = form.copy(
            nombreError = UserValidator.validateName(form.nombre),
            emailError = UserValidator.validateEmail(form.email),
            passwordError = UserValidator.validatePassword(form.password),
            rolError = UserValidator.validateRole(form.rol),
            telefonoError = UserValidator.validatePhone(form.telefono),
            institucionError = UserValidator.validateInstitution(form.institucion, form.rol)
        )
        if (listOf(
                checked.nombreError, checked.emailError, checked.passwordError,
                checked.rolError, checked.telefonoError, checked.institucionError
            ).any { it != null }
        ) {
            _uiState.update { it.copy(form = checked) }
            return
        }

        updateForm { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.create(
                    nombre = form.nombre,
                    email = form.email,
                    password = form.password,
                    rol = form.rol,
                    telefono = form.telefono,
                    institucion = form.institucion
                )
                val users = repository.getAll()
                _uiState.update {
                    it.copy(
                        users = users,
                        form = null,
                        // El usuario creado por el admin nace aprobado: lo mostramos en esa pestaña.
                        filter = ProfileFilter.Aprobados,
                        message = "Usuario creado"
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Lo más común: el correo ya existe (409). El mensaje lo manda el servicio.
                _uiState.update {
                    it.copy(
                        form = it.form?.copy(isSaving = false),
                        message = (e as? UsuarioException)?.message ?: "No pudimos crear el usuario"
                    )
                }
            }
        }
    }
}
