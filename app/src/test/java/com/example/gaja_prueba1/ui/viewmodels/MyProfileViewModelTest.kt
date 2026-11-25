package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.model.EstadoPerfil
import com.example.gaja_prueba1.model.Usuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class MyProfileViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val cadete = Usuario(
        id = 1, email = "cadete@gaja.cl", nombre = "Cadete de Prueba", rol = "Cadete",
        estado = EstadoPerfil.Aprobado, telefono = "+56911112222"
    )
    private val externo = Usuario(
        id = 2, email = "externo@gaja.cl", nombre = "Club Externo", rol = "Institución externa",
        estado = EstadoPerfil.Aprobado, institucion = "Liceo Vecino"
    )

    private fun vm(repo: FakeUsuariosRepository = FakeUsuariosRepository(listOf(cadete, externo))) =
        MyProfileViewModel(repo)

    @Test
    fun loadTraeElPerfilDeLaSesionYArmaElFormulario() {
        val vm = vm()
        vm.load("CADETE@gaja.cl")

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1L, state.user?.id)
        assertEquals("Cadete de Prueba", state.personal.nombre)
        assertEquals("+56911112222", state.personal.telefono)
    }

    @Test
    fun siLaCuentaNoTienePerfilMuestraElMensajeDelServicio() {
        val vm = vm()
        vm.load("profe_exe@duoc.cl")

        val state = vm.uiState.value
        assertNull(state.user)
        assertEquals("Usuario no encontrado", state.errorMessage)
    }

    @Test
    fun siElServicioFallaMuestraUnErrorGenerico() {
        val vm = vm(FakeUsuariosRepository(listOf(cadete), failing = true))
        vm.load("cadete@gaja.cl")

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun alEntrarOtraPersonaNoSeMuestranLosDatosDeLaAnterior() {
        val repo = FakeUsuariosRepository(listOf(cadete, externo))
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")

        repo.failing = true
        vm.load("externo@gaja.cl")

        assertNull(vm.uiState.value.user)
    }

    @Test
    fun guardarDatosConNombreVacioMarcaErrorYNoLlamaAlServicio() {
        val repo = FakeUsuariosRepository(listOf(cadete))
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")
        vm.onNameChange("  ")
        vm.onSavePersonal()

        assertNotNull(vm.uiState.value.personal.nombreError)
        assertEquals("Cadete de Prueba", repo.users.first().nombre)
    }

    @Test
    fun guardarDatosValidosLosActualizaEnElServicio() {
        val repo = FakeUsuariosRepository(listOf(cadete))
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")
        vm.onNameChange("Nombre Nuevo")
        vm.onPhoneChange("+56 9 8888 7777")
        vm.onSavePersonal()

        val state = vm.uiState.value
        assertEquals("Datos guardados", state.message)
        assertEquals("Nombre Nuevo", repo.users.first().nombre)
        assertEquals("Nombre Nuevo", state.user?.nombre)
        assertFalse(state.personal.isSaving)
    }

    @Test
    fun unTelefonoInvalidoSeRechaza() {
        val vm = vm()
        vm.load("cadete@gaja.cl")
        vm.onPhoneChange("abc")
        vm.onSavePersonal()

        assertNotNull(vm.uiState.value.personal.telefonoError)
    }

    @Test
    fun unaInstitucionExternaNoPuedeDejarVaciaLaInstitucion() {
        val vm = vm()
        vm.load("externo@gaja.cl")
        vm.onInstitutionChange("")
        vm.onSavePersonal()

        assertNotNull(vm.uiState.value.personal.institucionError)
    }

    @Test
    fun siGuardarFallaSeMuestraElMensajeYSePuedeReintentar() {
        val repo = FakeUsuariosRepository(listOf(cadete))
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")
        vm.onNameChange("Nombre Nuevo")
        repo.actionError = "El servicio no respondió"
        vm.onSavePersonal()

        assertEquals("El servicio no respondió", vm.uiState.value.message)
        assertFalse(vm.uiState.value.personal.isSaving)
    }

    @Test
    fun descartarVuelveAMostrarLoGuardado() {
        val vm = vm()
        vm.load("cadete@gaja.cl")
        vm.onNameChange("Algo sin guardar")
        vm.onDiscardPersonal()

        assertEquals("Cadete de Prueba", vm.uiState.value.personal.nombre)
    }

    @Test
    fun elFormularioDeContrasenaMarcaLosErrores() {
        val vm = vm()
        vm.load("cadete@gaja.cl")
        vm.onSavePassword()
        assertNotNull(vm.uiState.value.password.actualError)
        assertNotNull(vm.uiState.value.password.nuevaError)

        vm.onCurrentPasswordChange("12345")
        vm.onNewPasswordChange("nueva1")
        vm.onConfirmPasswordChange("otra")
        vm.onSavePassword()
        assertNull(vm.uiState.value.password.actualError)
        assertNull(vm.uiState.value.password.nuevaError)
        assertNotNull(vm.uiState.value.password.confirmacionError)
    }

    @Test
    fun cambiarLaContrasenaLimpiaElFormularioYAvisa() {
        val repo = FakeUsuariosRepository(listOf(cadete))
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")
        vm.onCurrentPasswordChange("12345")
        vm.onNewPasswordChange("nueva1")
        vm.onConfirmPasswordChange("nueva1")
        vm.onSavePassword()

        val state = vm.uiState.value
        assertEquals("Contraseña actualizada", state.message)
        assertEquals("", state.password.actual)
        assertEquals("", state.password.nueva)
        assertEquals(listOf("12345" to "nueva1"), repo.passwordChanges)
    }

    @Test
    fun siLaContrasenaActualEstaMalElErrorQuedaEnEseCampo() {
        val repo = FakeUsuariosRepository(listOf(cadete)).apply {
            actionError = "La contraseña actual no es correcta"
        }
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")
        vm.onCurrentPasswordChange("mala")
        vm.onNewPasswordChange("nueva1")
        vm.onConfirmPasswordChange("nueva1")
        vm.onSavePassword()

        val form = vm.uiState.value.password
        assertEquals("La contraseña actual no es correcta", form.actualError)
        assertFalse(form.isSaving)
    }

    @Test
    fun apagarYEncenderLasNotificacionesSeGuardaEnElServicio() {
        val repo = FakeUsuariosRepository(listOf(cadete))
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")
        assertTrue(vm.uiState.value.user!!.notifAlertas)

        vm.onAlertsChange(false)
        assertFalse(vm.uiState.value.user!!.notifAlertas)
        assertTrue(vm.uiState.value.user!!.notifReservas)
        assertFalse(repo.users.first().notifAlertas)

        vm.onReservationAlertsChange(false)
        assertFalse(vm.uiState.value.user!!.notifReservas)
        assertFalse(vm.uiState.value.isSavingNotifications)
    }

    @Test
    fun siFallaGuardarUnaNotificacionQuedaComoEstabaYAvisa() {
        val repo = FakeUsuariosRepository(listOf(cadete))
        val vm = vm(repo)
        vm.load("cadete@gaja.cl")
        repo.actionError = "Sin conexión"
        vm.onAlertsChange(false)

        assertTrue(vm.uiState.value.user!!.notifAlertas)
        assertEquals("Sin conexión", vm.uiState.value.message)
    }
}
