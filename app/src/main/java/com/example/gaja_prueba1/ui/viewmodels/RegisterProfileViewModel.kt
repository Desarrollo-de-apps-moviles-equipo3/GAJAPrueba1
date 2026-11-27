package com.example.gaja_prueba1.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gaja_prueba1.model.RemoteUsuariosRepository
import com.example.gaja_prueba1.model.UsuarioException
import com.example.gaja_prueba1.validation.UserValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Roles que una persona puede pedir al crear su perfil. "Administrador" solo lo asigna el administrador. */
val REGISTER_ROLES = listOf("Cadete", "Oficial", "Oficial de semana", "Institución externa")

data class RegisterProfileState(
    val nombre: String = "",
    val email: String = "",
    val rol: String = REGISTER_ROLES.first(),
    val telefono: String = "",
    val institucion: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val nombreError: String? = null,
    val emailError: String? = null,
    val telefonoError: String? = null,
    val institucionError: String? = null,
    val passwordError: String? = null,
    val confirmError: String? = null,
    val isSaving: Boolean = false,
    val submitError: String? = null,
    /** Cuando no es null, la solicitud se envió y se muestra la pantalla de confirmación. */
    val sent: SentProfile? = null
)

data class SentProfile(val nombre: String, val rol: String, val email: String)

/** Formulario "Crear perfil": envía la solicitud y el perfil queda pendiente de aprobación. */
class RegisterProfileViewModel(
    private val repository: RemoteUsuariosRepository = RemoteUsuariosRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterProfileState())
    val state: StateFlow<RegisterProfileState> = _state.asStateFlow()

    fun onNombre(value: String) = _state.update { it.copy(nombre = value, nombreError = null, submitError = null) }
    fun onEmail(value: String) = _state.update { it.copy(email = value, emailError = null, submitError = null) }
    fun onRol(value: String) = _state.update { it.copy(rol = value, institucionError = null, submitError = null) }
    fun onTelefono(value: String) = _state.update { it.copy(telefono = value, telefonoError = null, submitError = null) }
    fun onInstitucion(value: String) = _state.update { it.copy(institucion = value, institucionError = null, submitError = null) }
    fun onPassword(value: String) = _state.update { it.copy(password = value, passwordError = null, submitError = null) }
    fun onConfirm(value: String) = _state.update { it.copy(confirmPassword = value, confirmError = null, submitError = null) }

    /** Vuelve a un formulario vacío (al salir de la pantalla de confirmación). */
    fun reset() {
        _state.value = RegisterProfileState()
    }

    fun submit() {
        val current = _state.value
        if (current.isSaving) return

        val nombreError = UserValidator.validateName(current.nombre)
        val emailError = UserValidator.validateEmail(current.email)
        val telefonoError = UserValidator.validatePhone(current.telefono)
        val institucionError = UserValidator.validateInstitution(current.institucion, current.rol)
        val passwordError = UserValidator.validatePassword(current.password)
        val confirmError = if (current.password != current.confirmPassword) "Las contraseñas no coinciden" else null

        if (listOf(nombreError, emailError, telefonoError, institucionError, passwordError, confirmError).any { it != null }) {
            _state.update {
                it.copy(
                    nombreError = nombreError,
                    emailError = emailError,
                    telefonoError = telefonoError,
                    institucionError = institucionError,
                    passwordError = passwordError,
                    confirmError = confirmError
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, submitError = null) }
            try {
                repository.register(
                    nombre = current.nombre,
                    email = current.email,
                    password = current.password,
                    rol = current.rol,
                    telefono = current.telefono,
                    institucion = if (current.rol == UserValidator.EXTERNAL_ROLE) current.institucion else ""
                )
                _state.update {
                    it.copy(
                        isSaving = false,
                        password = "",
                        confirmPassword = "",
                        sent = SentProfile(current.nombre.trim(), current.rol, current.email.trim())
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: UsuarioException) {
                _state.update { it.copy(isSaving = false, submitError = e.message) }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isSaving = false,
                        submitError = "No se pudo enviar la solicitud. Revisa tu conexión e inténtalo otra vez."
                    )
                }
            }
        }
    }
}