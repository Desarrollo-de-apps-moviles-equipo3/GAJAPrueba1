package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.RemoteUsuariosRepository
import com.example.gaja_prueba1.model.Usuario
import com.example.gaja_prueba1.model.UsuarioException
import com.example.gaja_prueba1.model.UsuariosRepository
import com.example.gaja_prueba1.validation.ProfileValidator
import com.example.gaja_prueba1.validation.UserValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Campos de "Datos personales". El correo y el rol no se editan: los define el administrador. */
data class PersonalDataForm(
    val nombre: String = "",
    val telefono: String = "",
    val institucion: String = "",
    val nombreError: String? = null,
    val telefonoError: String? = null,
    val institucionError: String? = null,
    val isSaving: Boolean = false
)

data class PasswordForm(
    val actual: String = "",
    val nueva: String = "",
    val confirmacion: String = "",
    val actualError: String? = null,
    val nuevaError: String? = null,
    val confirmacionError: String? = null,
    val isSaving: Boolean = false
)

data class MyProfileUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val user: Usuario? = null,
    val personal: PersonalDataForm = PersonalDataForm(),
    val password: PasswordForm = PasswordForm(),
    val isSavingNotifications: Boolean = false,
    val message: String? = null
)

/**
 * Perfil de la persona que tiene la sesión abierta: datos personales, contraseña y notificaciones.
 * Todo se guarda en el servicio de usuarios, así que sigue ahí después de cerrar la app.
 */
class MyProfileViewModel(
    private val repository: UsuariosRepository = RemoteUsuariosRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyProfileUiState())
    val uiState: StateFlow<MyProfileUiState> = _uiState.asStateFlow()

    /** Busca el perfil con el correo de la sesión. La pantalla lo llama cada vez que se abre. */
    fun load(email: String) {
        viewModelScope.launch {
            _uiState.update { state ->
                // El ViewModel sobrevive al cierre de sesión: si entra otra persona, no mostramos los datos de la anterior.
                val fresh = if (state.user != null && !state.user.email.equals(email.trim(), ignoreCase = true)) {
                    MyProfileUiState()
                } else {
                    state
                }
                fresh.copy(isLoading = fresh.user == null, errorMessage = null)
            }
            try {
                val user = repository.getByEmail(email)
                _uiState.update {
                    it.copy(isLoading = false, user = user, personal = user.toPersonalForm(), password = PasswordForm())
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("GAJA_USUARIOS", "No se pudo cargar el perfil", e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = (e as? UsuarioException)?.message
                            ?: "No pudimos cargar tu perfil. Inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun onMessageShown() = _uiState.update { it.copy(message = null) }

    private fun Usuario.toPersonalForm() =
        PersonalDataForm(nombre = nombre, telefono = telefono, institucion = institucion)

    // ---------- Datos personales ----------

    fun onNameChange(value: String) = updatePersonal { it.copy(nombre = value, nombreError = null) }
    fun onPhoneChange(value: String) = updatePersonal { it.copy(telefono = value, telefonoError = null) }
    fun onInstitutionChange(value: String) = updatePersonal { it.copy(institucion = value, institucionError = null) }

    private fun updatePersonal(change: (PersonalDataForm) -> PersonalDataForm) =
        _uiState.update { it.copy(personal = change(it.personal)) }

    /** Vuelve a mostrar lo guardado (al salir de la pantalla sin guardar). */
    fun onDiscardPersonal() = _uiState.update { state ->
        state.copy(personal = state.user?.toPersonalForm() ?: PersonalDataForm())
    }

    fun onSavePersonal() {
        val state = _uiState.value
        val user = state.user ?: return
        val form = state.personal
        if (form.isSaving) return

        val checked = form.copy(
            nombreError = UserValidator.validateName(form.nombre),
            telefonoError = UserValidator.validatePhone(form.telefono),
            institucionError = UserValidator.validateInstitution(form.institucion, user.rol)
        )
        if (checked.nombreError != null || checked.telefonoError != null || checked.institucionError != null) {
            _uiState.update { it.copy(personal = checked) }
            return
        }

        updatePersonal { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                val updated = repository.update(user.id, form.nombre, form.telefono, form.institucion)
                _uiState.update {
                    it.copy(user = updated, personal = updated.toPersonalForm(), message = "Datos guardados")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                updatePersonal { it.copy(isSaving = false) }
                _uiState.update {
                    it.copy(message = (e as? UsuarioException)?.message ?: "No pudimos guardar tus datos")
                }
            }
        }
    }

    // ---------- Cambiar contraseña ----------

    fun onCurrentPasswordChange(value: String) = updatePassword { it.copy(actual = value, actualError = null) }
    fun onNewPasswordChange(value: String) = updatePassword { it.copy(nueva = value, nuevaError = null) }
    fun onConfirmPasswordChange(value: String) =
        updatePassword { it.copy(confirmacion = value, confirmacionError = null) }

    private fun updatePassword(change: (PasswordForm) -> PasswordForm) =
        _uiState.update { it.copy(password = change(it.password)) }

    fun onClearPassword() = updatePassword { PasswordForm() }

    fun onSavePassword() {
        val state = _uiState.value
        val user = state.user ?: return
        val form = state.password
        if (form.isSaving) return

        val checked = form.copy(
            actualError = ProfileValidator.validateCurrentPassword(form.actual),
            nuevaError = ProfileValidator.validateNewPassword(form.nueva, form.actual),
            confirmacionError = ProfileValidator.validateConfirmation(form.nueva, form.confirmacion)
        )
        if (checked.actualError != null || checked.nuevaError != null || checked.confirmacionError != null) {
            _uiState.update { it.copy(password = checked) }
            return
        }

        updatePassword { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                repository.changePassword(user.id, form.actual, form.nueva)
                _uiState.update { it.copy(password = PasswordForm(), message = "Contraseña actualizada") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: UsuarioException) {
                // Lo más común: la contraseña actual no es correcta. Se marca en ese campo.
                updatePassword { it.copy(isSaving = false, actualError = e.message) }
            } catch (e: Exception) {
                updatePassword { it.copy(isSaving = false) }
                _uiState.update { it.copy(message = "No pudimos cambiar la contraseña") }
            }
        }
    }

    // ---------- Notificaciones ----------

    fun onReservationAlertsChange(enabled: Boolean) = saveNotifications(reservas = enabled, alertas = null)
    fun onAlertsChange(enabled: Boolean) = saveNotifications(reservas = null, alertas = enabled)

    private fun saveNotifications(reservas: Boolean?, alertas: Boolean?) {
        val state = _uiState.value
        val user = state.user ?: return
        if (state.isSavingNotifications) return

        _uiState.update { it.copy(isSavingNotifications = true) }
        viewModelScope.launch {
            try {
                // El servicio pide nombre en cada cambio: mandamos lo que ya está guardado.
                val updated = repository.update(
                    user.id, user.nombre, user.telefono, user.institucion,
                    notifReservas = reservas, notifAlertas = alertas
                )
                _uiState.update { it.copy(user = updated, isSavingNotifications = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingNotifications = false,
                        message = (e as? UsuarioException)?.message ?: "No pudimos guardar tus preferencias"
                    )
                }
            }
        }
    }
}
