package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.model.SupportException
import com.example.gaja_prueba1.model.SupportRepository
import com.example.gaja_prueba1.model.SupportRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class FakeSupportRepository(
    var failing: Boolean = false,
    var createError: String? = null
) : SupportRepository {
    val items = mutableListOf(
        SupportRequest(1, "Reservas", "No puedo reservar", resolved = true, date = "2026-11-20")
    )
    var createCalls = 0
    var lastEmail: String? = null

    override suspend fun create(email: String, category: String, description: String): SupportRequest {
        createCalls++
        createError?.let { throw SupportException(it) }
        return SupportRequest(items.size + 1L, category, description, resolved = false, date = "2026-11-24")
            .also { items.add(it) }
    }

    override suspend fun mine(email: String): List<SupportRequest> {
        if (failing) throw RuntimeException("sin conexión")
        lastEmail = email
        return items.toList()
    }
}

class SupportViewModelTest {

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Test
    fun loadTraeLasSolicitudesDeLaPersona() {
        val repo = FakeSupportRepository()
        val vm = SupportViewModel(repo)
        vm.load("cadete@gaja.cl")

        val state = vm.uiState.value
        assertFalse(state.isLoading)
        assertEquals(1, state.requests.size)
        assertEquals("cadete@gaja.cl", repo.lastEmail)
    }

    @Test
    fun siElServicioFallaMuestraError() {
        val vm = SupportViewModel(FakeSupportRepository(failing = true))
        vm.load("cadete@gaja.cl")

        assertNotNull(vm.uiState.value.errorMessage)
        assertFalse(vm.uiState.value.isLoading)
    }

    @Test
    fun alEntrarOtraPersonaSeEmpiezaDeCero() {
        val repo = FakeSupportRepository()
        val vm = SupportViewModel(repo)
        vm.load("cadete@gaja.cl")

        repo.failing = true
        vm.load("oficial@gaja.cl")

        assertTrue(vm.uiState.value.requests.isEmpty())
    }

    @Test
    fun enviarSinTemaNiDescripcionMarcaErroresYNoLlamaAlServicio() {
        val repo = FakeSupportRepository()
        val vm = SupportViewModel(repo)
        vm.onNewRequest()
        vm.onSend("cadete@gaja.cl")

        assertNotNull(vm.uiState.value.form.categoryError)
        assertNotNull(vm.uiState.value.form.descriptionError)
        assertEquals(0, repo.createCalls)
    }

    @Test
    fun enviarUnaSolicitudValidaLaAgregaALaListaYAvisa() {
        val repo = FakeSupportRepository()
        val vm = SupportViewModel(repo)
        vm.load("cadete@gaja.cl")
        vm.onNewRequest()
        vm.onCategoryChange("Mi cuenta")
        vm.onDescriptionChange("No puedo cambiar mi teléfono")
        vm.onSend("cadete@gaja.cl")

        val state = vm.uiState.value
        assertTrue(state.sent)
        assertEquals("Solicitud enviada", state.message)
        assertEquals(2, state.requests.size)
        assertEquals("", state.form.description)
        assertFalse(state.form.isSaving)
    }

    @Test
    fun enviarMuestraLaSolicitudAunqueElListadoEsteCaido() {
        val repo = FakeSupportRepository(failing = true)
        val vm = SupportViewModel(repo)
        vm.onNewRequest()
        vm.onCategoryChange("Otro")
        vm.onDescriptionChange("Necesito ayuda con mi cuenta")

        vm.onSend("cadete@gaja.cl")

        val state = vm.uiState.value
        assertTrue(state.sent)
        assertEquals("Solicitud enviada", state.message)
        assertEquals("Necesito ayuda con mi cuenta", state.requests.single().description)
        assertEquals(1, repo.createCalls)
    }

    @Test
    fun laDescripcionSeCortaEn1000Caracteres() {
        val vm = SupportViewModel(FakeSupportRepository())
        vm.onNewRequest()
        vm.onDescriptionChange("x".repeat(1500))

        assertEquals(1000, vm.uiState.value.form.description.length)
    }

    @Test
    fun siEnviarFallaElFormularioQuedaConLoEscrito() {
        val repo = FakeSupportRepository(createError = "Describe el problema")
        val vm = SupportViewModel(repo)
        vm.onNewRequest()
        vm.onCategoryChange("Otro")
        vm.onDescriptionChange("Hola")
        vm.onSend("cadete@gaja.cl")

        val state = vm.uiState.value
        assertFalse(state.sent)
        assertEquals("Describe el problema", state.message)
        assertEquals("Hola", state.form.description)
        assertFalse(state.form.isSaving)
    }

    @Test
    fun sentHandledApagaLaSenal() {
        val vm = SupportViewModel(FakeSupportRepository())
        vm.onNewRequest()
        vm.onCategoryChange("Otro")
        vm.onDescriptionChange("Hola")
        vm.onSend("cadete@gaja.cl")
        assertTrue(vm.uiState.value.sent)

        vm.onSentHandled()
        assertFalse(vm.uiState.value.sent)
    }
}
