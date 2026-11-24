package com.example.gaja_prueba1.ui.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminSpacesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun repo(failing: Boolean = false) = FakeAdminSpacesRepository(
        listOf(space("futbol-a", "Cancha Fútbol A"), space("tenis-1", "Tenis 1", "Tenis", available = false)),
        failing
    )

    @Test
    fun alCrearCargaLosEspaciosYLasCategorias() {
        val vm = AdminSpacesViewModel(repo())
        val state = vm.uiState.value

        assertFalse(state.isLoading)
        assertEquals(2, state.spaces.size)
        assertEquals(listOf("Fútbol", "Tenis"), state.categories)
        assertNull(state.errorMessage)
    }

    @Test
    fun siElServicioFallaMuestraUnMensajeDeError() {
        val vm = AdminSpacesViewModel(repo(failing = true))
        val state = vm.uiState.value

        assertFalse(state.isLoading)
        assertNotNull(state.errorMessage)
        assertTrue(state.spaces.isEmpty())
    }

    @Test
    fun laListaSeparaHabilitadosYDeshabilitados() {
        val vm = AdminSpacesViewModel(repo())
        assertEquals(listOf("futbol-a"), vm.uiState.value.visible.map { it.id })

        vm.onShowDisabled(true)
        assertEquals(listOf("tenis-1"), vm.uiState.value.visible.map { it.id })
    }

    @Test
    fun guardarUnFormularioVacioMarcaErroresYNoLlamaAlServicio() {
        val fake = repo()
        val vm = AdminSpacesViewModel(fake)

        vm.onNewSpace()
        vm.onSave()

        val form = vm.uiState.value.form!!
        assertNotNull(form.nameError)
        assertNotNull(form.categoryError)
        assertNotNull(form.capacityError)
        assertEquals(0, fake.createCalls)
    }

    @Test
    fun crearUnEspacioLoAgregaCierraElFormularioYAvisa() {
        val fake = repo()
        val vm = AdminSpacesViewModel(fake)

        vm.onNewSpace()
        vm.onNameChange("Cancha Pádel")
        vm.onCategoryChange("Pádel")
        vm.onCapacityChange("4")
        vm.onSave()

        val state = vm.uiState.value
        assertNull(state.form)
        assertEquals("Espacio creado", state.message)
        assertEquals(3, state.spaces.size)
        assertTrue(state.spaces.any { it.name == "Cancha Pádel" && it.capacity == 4 })
    }

    @Test
    fun editarUnEspacioGuardaLosCambios() {
        val vm = AdminSpacesViewModel(repo())

        vm.onEditSpace(vm.uiState.value.spaces.first { it.id == "futbol-a" })
        vm.onNameChange("Cancha Principal")
        vm.onSave()

        val state = vm.uiState.value
        assertEquals("Cambios guardados", state.message)
        assertEquals("Cancha Principal", state.spaces.first { it.id == "futbol-a" }.name)
    }

    @Test
    fun siGuardarFallaElFormularioQuedaAbierto() {
        val fake = repo()
        val vm = AdminSpacesViewModel(fake)

        vm.onNewSpace()
        vm.onNameChange("Cancha Pádel")
        vm.onCategoryChange("Pádel")
        vm.onCapacityChange("4")
        fake.failing = true
        vm.onSave()

        val state = vm.uiState.value
        assertEquals("No pudimos guardar el espacio", state.message)
        assertNotNull(state.form)
        assertFalse(state.form!!.isSaving)
    }

    @Test
    fun deshabilitarPideConfirmacionYLuegoCambiaElEstado() {
        val vm = AdminSpacesViewModel(repo())
        val cancha = vm.uiState.value.spaces.first { it.id == "futbol-a" }

        vm.onDisableRequested(cancha)
        assertEquals(cancha, vm.uiState.value.pendingDisable)

        vm.onDisableConfirmed()
        val state = vm.uiState.value
        assertNull(state.pendingDisable)
        assertEquals("Espacio deshabilitado", state.message)
        assertFalse(state.spaces.first { it.id == "futbol-a" }.available)
    }

    @Test
    fun cancelarLaConfirmacionNoCambiaNada() {
        val vm = AdminSpacesViewModel(repo())
        vm.onDisableRequested(vm.uiState.value.spaces.first())
        vm.onDisableCancelled()

        assertNull(vm.uiState.value.pendingDisable)
        assertTrue(vm.uiState.value.spaces.first { it.id == "futbol-a" }.available)
    }

    @Test
    fun habilitarUnEspacioDeshabilitadoLoVuelveAReservable() {
        val vm = AdminSpacesViewModel(repo())
        vm.onEnable(vm.uiState.value.spaces.first { it.id == "tenis-1" })

        val state = vm.uiState.value
        assertEquals("Espacio habilitado", state.message)
        assertTrue(state.spaces.first { it.id == "tenis-1" }.available)
    }

    @Test
    fun eliminarQuitaElEspacioDeLaLista() {
        val vm = AdminSpacesViewModel(repo())
        vm.onDeleteRequested(vm.uiState.value.spaces.first { it.id == "tenis-1" })
        vm.onDeleteConfirmed()

        val state = vm.uiState.value
        assertEquals("Espacio eliminado", state.message)
        assertEquals(listOf("futbol-a"), state.spaces.map { it.id })
    }
}
