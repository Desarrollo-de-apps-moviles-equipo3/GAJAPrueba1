package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AdminCategoriesViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun repo() = FakeCategoriesRepository(listOf(Category(1, "Fútbol"), Category(2, "Tenis")))

    @Test
    fun alCrearCargaLasCategorias() {
        val vm = AdminCategoriesViewModel(repo())
        val state = vm.uiState.value

        assertFalse(state.isLoading)
        assertEquals(listOf("Fútbol", "Tenis"), state.categories.map { it.name })
    }

    @Test
    fun siElServicioFallaMuestraError() {
        val vm = AdminCategoriesViewModel(FakeCategoriesRepository(failing = true))

        assertFalse(vm.uiState.value.isLoading)
        assertNotNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun unNombreRepetidoSeRechazaSinLlamarAlServicio() {
        val fake = repo()
        val vm = AdminCategoriesViewModel(fake)

        vm.onNewCategory()
        vm.onNameChange("fútbol")
        vm.onSave()

        assertNotNull(vm.uiState.value.form?.nameError)
        assertEquals(2, fake.items.size)
    }

    @Test
    fun crearUnaCategoriaValidaLaAgregaYCierraElFormulario() {
        val vm = AdminCategoriesViewModel(repo())

        vm.onNewCategory()
        vm.onNameChange("  Pádel  ")
        vm.onSave()

        val state = vm.uiState.value
        assertNull(state.form)
        assertEquals("Categoría creada", state.message)
        assertTrue(state.categories.any { it.name == "Pádel" })
    }

    @Test
    fun renombrarUnaCategoriaGuardaElNuevoNombre() {
        val vm = AdminCategoriesViewModel(repo())

        vm.onEditCategory(vm.uiState.value.categories.first { it.id == 2L })
        vm.onNameChange("Tenis de mesa")
        vm.onSave()

        val state = vm.uiState.value
        assertEquals("Cambios guardados", state.message)
        assertEquals("Tenis de mesa", state.categories.first { it.id == 2L }.name)
    }

    @Test
    fun eliminarUnaCategoriaLaQuitaDeLaLista() {
        val vm = AdminCategoriesViewModel(repo())

        vm.onDeleteRequested(vm.uiState.value.categories.first { it.id == 1L })
        vm.onDeleteConfirmed()

        val state = vm.uiState.value
        assertEquals("Categoría eliminada", state.message)
        assertEquals(listOf(2L), state.categories.map { it.id })
    }

    @Test
    fun siLaCategoriaTieneEspaciosMuestraElMensajeDelServicio() {
        val fake = repo().apply { deleteError = "No se puede eliminar: tiene espacios asociados" }
        val vm = AdminCategoriesViewModel(fake)

        vm.onDeleteRequested(vm.uiState.value.categories.first())
        vm.onDeleteConfirmed()

        assertEquals("No se puede eliminar: tiene espacios asociados", vm.uiState.value.message)
        assertEquals(2, vm.uiState.value.categories.size)
    }
}
