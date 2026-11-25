package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.model.EstadoPerfil
import com.example.gaja_prueba1.model.Sesion
import com.example.gaja_prueba1.model.Usuario
import com.example.gaja_prueba1.model.UsuarioException
import com.example.gaja_prueba1.model.UsuariosRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Repositorio de usuarios en memoria que respeta las reglas del servicio. */
class FakeUsuariosRepository(
    initial: List<Usuario> = emptyList(),
    var failing: Boolean = false,
    var actionError: String? = null
) : UsuariosRepository {
    val users = initial.toMutableList()
    var lastRejectReason: String? = null
    var createCalls = 0

    private fun check() {
        if (failing) throw RuntimeException("sin conexión")
    }

    private fun action(id: Long, change: (Usuario) -> Usuario): Usuario {
        actionError?.let { throw UsuarioException(it) }
        val i = users.indexOfFirst { it.id == id }
        users[i] = change(users[i])
        return users[i]
    }

    override suspend fun getAll(): List<Usuario> { check(); return users.toList() }
    override suspend fun get(id: Long): Usuario = users.first { it.id == id }

    override suspend fun create(
        nombre: String, email: String, password: String, rol: String, telefono: String, institucion: String
    ): Usuario {
        createCalls++
        actionError?.let { throw UsuarioException(it) }
        return Usuario(50L + users.size, email, nombre, rol, EstadoPerfil.Aprobado, telefono, institucion)
            .also { users.add(it) }
    }

    override suspend fun update(
        id: Long, nombre: String, telefono: String, institucion: String,
        notifReservas: Boolean?, notifAlertas: Boolean?
    ): Usuario = action(id) { it.copy(nombre = nombre, telefono = telefono, institucion = institucion) }

    override suspend fun changePassword(id: Long, actual: String, nueva: String) = Unit
    override suspend fun approve(id: Long) = action(id) { it.copy(estado = EstadoPerfil.Aprobado) }
    override suspend fun reject(id: Long, motivo: String): Usuario {
        lastRejectReason = motivo
        return action(id) { it.copy(estado = EstadoPerfil.Rechazado, motivoRechazo = motivo) }
    }
    override suspend fun deactivate(id: Long) = action(id) { it.copy(estado = EstadoPerfil.Desactivado) }
    override suspend fun activate(id: Long) = action(id) { it.copy(estado = EstadoPerfil.Aprobado) }
    override suspend fun sessions(id: Long): List<Sesion> = emptyList()
}

class AdminProfilesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun user(id: Long, nombre: String, estado: EstadoPerfil, rol: String = "Cadete") =
        Usuario(id, "u$id@gaja.cl", nombre, rol, estado)

    private fun repo() = FakeUsuariosRepository(
        listOf(
            user(1, "Ana Admin", EstadoPerfil.Aprobado, "Administrador"),
            user(2, "Pedro Pendiente", EstadoPerfil.Pendiente),
            user(3, "Paula Pendiente", EstadoPerfil.Pendiente),
            user(4, "Dario Desactivado", EstadoPerfil.Desactivado)
        )
    )

    private fun fillValidForm(vm: AdminProfilesViewModel) {
        vm.onNewUser()
        vm.onNameChange("Nuevo Usuario")
        vm.onEmailChange("nuevo@gaja.cl")
        vm.onPasswordChange("abcde")
        vm.onRoleChange("Oficial")
    }

    @Test
    fun alCrearCargaLosPerfilesYMuestraLosPendientes() {
        val state = AdminProfilesViewModel(repo()).uiState.value

        assertFalse(state.isLoading)
        assertEquals(ProfileFilter.Pendientes, state.filter)
        assertEquals(listOf(2L, 3L), state.visible.map { it.id })
        assertEquals(2, state.countOf(ProfileFilter.Pendientes))
        assertEquals(1, state.countOf(ProfileFilter.Aprobados))
        assertEquals(1, state.countOf(ProfileFilter.Desactivados))
    }

    @Test
    fun siElServicioFallaMuestraError() {
        val state = AdminProfilesViewModel(FakeUsuariosRepository(failing = true)).uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
    }

    @Test
    fun cambiarDePestanaFiltraLaLista() {
        val vm = AdminProfilesViewModel(repo())
        vm.onFilterChange(ProfileFilter.Aprobados)
        assertEquals(listOf(1L), vm.uiState.value.visible.map { it.id })

        vm.onFilterChange(ProfileFilter.Desactivados)
        assertEquals(listOf(4L), vm.uiState.value.visible.map { it.id })
    }

    @Test
    fun abrirYCerrarElDetalle() {
        val vm = AdminProfilesViewModel(repo())
        vm.onOpenDetail(vm.uiState.value.users.first { it.id == 2L })
        assertEquals(2L, vm.uiState.value.selected?.id)

        vm.onCloseDetail()
        assertNull(vm.uiState.value.selected)
    }

    @Test
    fun aprobarPasaElPerfilAAprobadosYVuelveALaLista() {
        val vm = AdminProfilesViewModel(repo())
        val pedro = vm.uiState.value.users.first { it.id == 2L }
        vm.onOpenDetail(pedro)
        vm.onApprove(pedro)

        val state = vm.uiState.value
        assertEquals("Perfil aprobado", state.message)
        assertNull(state.selected)
        assertEquals(1, state.countOf(ProfileFilter.Pendientes))
        assertEquals(2, state.countOf(ProfileFilter.Aprobados))
    }

    @Test
    fun rechazarSinMotivoMarcaErrorYNoLlamaAlServicio() {
        val fake = repo()
        val vm = AdminProfilesViewModel(fake)
        vm.onRejectRequested(vm.uiState.value.users.first { it.id == 2L })
        vm.onRejectConfirmed()

        assertNotNull(vm.uiState.value.reject?.error)
        assertNull(fake.lastRejectReason)
        assertEquals(EstadoPerfil.Pendiente, fake.users.first { it.id == 2L }.estado)
    }

    @Test
    fun rechazarConMotivoGuardaElMotivoYCierraElDialogo() {
        val fake = repo()
        val vm = AdminProfilesViewModel(fake)
        vm.onRejectRequested(vm.uiState.value.users.first { it.id == 2L })
        vm.onRejectReasonChange("Datos incompletos")
        vm.onRejectConfirmed()

        val state = vm.uiState.value
        assertNull(state.reject)
        assertEquals("Perfil rechazado", state.message)
        assertEquals("Datos incompletos", fake.lastRejectReason)
        assertEquals(EstadoPerfil.Rechazado, fake.users.first { it.id == 2L }.estado)
        assertEquals(1, state.countOf(ProfileFilter.Pendientes))
    }

    @Test
    fun cancelarElRechazoNoCambiaNada() {
        val vm = AdminProfilesViewModel(repo())
        vm.onRejectRequested(vm.uiState.value.users.first { it.id == 2L })
        vm.onRejectCancelled()

        assertNull(vm.uiState.value.reject)
        assertEquals(2, vm.uiState.value.countOf(ProfileFilter.Pendientes))
    }

    @Test
    fun desactivarPideConfirmacionYLuegoCambiaElEstado() {
        val vm = AdminProfilesViewModel(repo())
        val ana = vm.uiState.value.users.first { it.id == 1L }

        vm.onDeactivateRequested(ana)
        assertEquals(ana, vm.uiState.value.pendingDeactivate)

        vm.onDeactivateConfirmed()
        val state = vm.uiState.value
        assertNull(state.pendingDeactivate)
        assertEquals("Usuario desactivado", state.message)
        assertEquals(2, state.countOf(ProfileFilter.Desactivados))
        assertEquals(0, state.countOf(ProfileFilter.Aprobados))
    }

    @Test
    fun cancelarLaDesactivacionNoCambiaNada() {
        val vm = AdminProfilesViewModel(repo())
        vm.onDeactivateRequested(vm.uiState.value.users.first { it.id == 1L })
        vm.onDeactivateCancelled()

        assertNull(vm.uiState.value.pendingDeactivate)
        assertEquals(1, vm.uiState.value.countOf(ProfileFilter.Aprobados))
    }

    @Test
    fun activarUnDesactivadoLoVuelveAAprobado() {
        val vm = AdminProfilesViewModel(repo())
        vm.onActivate(vm.uiState.value.users.first { it.id == 4L })

        assertEquals("Perfil activado", vm.uiState.value.message)
        assertEquals(2, vm.uiState.value.countOf(ProfileFilter.Aprobados))
    }

    @Test
    fun siElServicioRechazaElCambioSeMuestraSuMensaje() {
        val fake = repo().apply { actionError = "Solo se puede aprobar un perfil pendiente" }
        val vm = AdminProfilesViewModel(fake)
        vm.onApprove(vm.uiState.value.users.first { it.id == 2L })

        assertEquals("Solo se puede aprobar un perfil pendiente", vm.uiState.value.message)
    }

    @Test
    fun elFormularioVacioMarcaTodosLosErroresYNoCreaNada() {
        val fake = repo()
        val vm = AdminProfilesViewModel(fake)
        vm.onNewUser()
        vm.onSave()

        val form = vm.uiState.value.form!!
        assertNotNull(form.nombreError)
        assertNotNull(form.emailError)
        assertNotNull(form.passwordError)
        assertNotNull(form.rolError)
        assertEquals(0, fake.createCalls)
    }

    @Test
    fun unaInstitucionExternaExigeElNombreDeLaInstitucion() {
        val fake = repo()
        val vm = AdminProfilesViewModel(fake)
        fillValidForm(vm)
        vm.onRoleChange("Institución externa")
        assertTrue(vm.uiState.value.form!!.needsInstitution)

        vm.onSave()
        assertNotNull(vm.uiState.value.form?.institucionError)
        assertEquals(0, fake.createCalls)

        vm.onInstitutionChange("Liceo Vecino")
        vm.onSave()
        assertNull(vm.uiState.value.form)
        assertEquals(1, fake.createCalls)
    }

    @Test
    fun crearUnUsuarioLoAgregaAAprobadosYCierraElFormulario() {
        val vm = AdminProfilesViewModel(repo())
        fillValidForm(vm)
        vm.onSave()

        val state = vm.uiState.value
        assertNull(state.form)
        assertEquals("Usuario creado", state.message)
        assertEquals(ProfileFilter.Aprobados, state.filter)
        assertTrue(state.visible.any { it.email == "nuevo@gaja.cl" })
    }

    @Test
    fun siElCorreoYaExisteElFormularioQuedaAbiertoConElMensajeDelServicio() {
        val fake = repo().apply { actionError = "Ya existe un usuario con ese correo" }
        val vm = AdminProfilesViewModel(fake)
        fillValidForm(vm)
        vm.onSave()

        val state = vm.uiState.value
        assertEquals("Ya existe un usuario con ese correo", state.message)
        assertNotNull(state.form)
        assertFalse(state.form!!.isSaving)
    }
}
