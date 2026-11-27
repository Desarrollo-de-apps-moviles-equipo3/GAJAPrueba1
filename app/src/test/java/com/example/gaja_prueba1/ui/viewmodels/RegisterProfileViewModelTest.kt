package com.example.gaja_prueba1.ui.viewmodels

import com.example.gaja_prueba1.model.EstadoPerfil
import com.example.gaja_prueba1.model.RemoteUsuariosRepository
import com.example.gaja_prueba1.model.Usuario
import com.example.gaja_prueba1.model.UsuarioException
import com.example.gaja_prueba1.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RegisterProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun repo(): RemoteUsuariosRepository = mockk<RemoteUsuariosRepository>().also {
        coEvery { it.register(any(), any(), any(), any(), any(), any()) } returns
                Usuario(10, "valentina.diaz@escuelamilitar.cl", "Valentina Díaz", "Cadete", EstadoPerfil.Pendiente)
    }

    private fun fill(viewModel: RegisterProfileViewModel) {
        viewModel.onNombre("Valentina Díaz")
        viewModel.onEmail("valentina.diaz@escuelamilitar.cl")
        viewModel.onPassword("secreta123")
        viewModel.onConfirm("secreta123")
    }

    @Test
    fun formulario_vacio_muestra_errores_y_no_envia() = runTest {
        val repository = repo()
        val viewModel = RegisterProfileViewModel(repository)

        viewModel.submit()
        advanceUntilIdle()

        val state = viewModel.state.value
        assertNotNull(state.nombreError)
        assertNotNull(state.emailError)
        assertNotNull(state.passwordError)
        assertNull(state.sent)
        coVerify(exactly = 0) { repository.register(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun las_contrasenas_deben_coincidir() = runTest {
        val viewModel = RegisterProfileViewModel(repo())
        fill(viewModel)
        viewModel.onConfirm("otra-clave")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Las contraseñas no coinciden", viewModel.state.value.confirmError)
        assertNull(viewModel.state.value.sent)
    }

    @Test
    fun institucion_externa_exige_institucion() = runTest {
        val viewModel = RegisterProfileViewModel(repo())
        fill(viewModel)
        viewModel.onRol("Institución externa")

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Escribe la institución", viewModel.state.value.institucionError)
    }

    @Test
    fun solicitud_valida_se_envia_y_muestra_la_confirmacion() = runTest {
        val repository = repo()
        val viewModel = RegisterProfileViewModel(repository)
        fill(viewModel)

        viewModel.submit()
        advanceUntilIdle()

        val sent = viewModel.state.value.sent
        assertNotNull(sent)
        assertEquals("Valentina Díaz", sent?.nombre)
        assertEquals("Cadete", sent?.rol)
        coVerify { repository.register("Valentina Díaz", "valentina.diaz@escuelamilitar.cl", "secreta123", "Cadete", "", "") }
        // La contraseña no se queda guardada en el estado.
        assertEquals("", viewModel.state.value.password)
    }

    @Test
    fun correo_repetido_muestra_el_mensaje_del_servicio() = runTest {
        val repository = mockk<RemoteUsuariosRepository>()
        coEvery { repository.register(any(), any(), any(), any(), any(), any()) } throws
                UsuarioException("Ya existe un usuario con ese correo")
        val viewModel = RegisterProfileViewModel(repository)
        fill(viewModel)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals("Ya existe un usuario con ese correo", viewModel.state.value.submitError)
        assertNull(viewModel.state.value.sent)
    }
}