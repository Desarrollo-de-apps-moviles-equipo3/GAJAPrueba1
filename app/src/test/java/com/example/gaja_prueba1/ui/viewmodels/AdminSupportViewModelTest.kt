package com.example.gaja_prueba1.ui.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminSupportViewModelTest {

    private fun vm() = AdminSupportViewModel()

    private fun ticket(code: String, status: TicketStatus) = SupportTicket(
        code = code, title = "Titulo", author = "Autor", whenText = "Hoy",
        message = "Mensaje", status = status
    )

    // ---------- Filtro y detalle ----------

    @Test
    fun `al iniciar muestra solo las solicitudes nuevas`() {
        val state = vm().uiState.value

        assertEquals(TicketStatus.NUEVA, state.filter)
        assertTrue(state.visible.isNotEmpty())
        assertTrue(state.visible.all { it.status == TicketStatus.NUEVA })
    }

    @Test
    fun `cambiar el filtro muestra solo ese estado`() {
        val vm = vm()
        vm.onFilter(TicketStatus.RESUELTA)

        val state = vm.uiState.value
        assertEquals(TicketStatus.RESUELTA, state.filter)
        assertTrue(state.visible.all { it.status == TicketStatus.RESUELTA })
    }

    @Test
    fun `abrir una solicitud la selecciona y limpia la respuesta`() {
        val vm = vm()
        val code = vm.uiState.value.tickets.first().code
        vm.onReplyChange("borrador")
        vm.onOpen(code)

        val state = vm.uiState.value
        assertEquals(code, state.selected?.code)
        assertEquals("", state.reply)
    }

    @Test
    fun `volver cierra el detalle y la edicion del contacto`() {
        val vm = vm()
        vm.onOpen(vm.uiState.value.tickets.first().code)
        vm.onEditContact()
        vm.onBack()

        val state = vm.uiState.value
        assertNull(state.selected)
        assertFalse(state.editingContact)
        assertNull(state.contactError)
    }

    @Test
    fun `la respuesta se limita a 500 caracteres`() {
        val vm = vm()
        vm.onReplyChange("a".repeat(600))

        assertEquals(500, vm.uiState.value.reply.length)
    }

    // ---------- Responder y resolver ----------

    @Test
    fun `responder pasa la solicitud a en revision y guarda la respuesta`() {
        val vm = vm()
        val code = vm.uiState.value.visible.first().code
        vm.onOpen(code)
        vm.onReplyChange("  Revisaremos tu caso  ")
        vm.onSendReply()

        val state = vm.uiState.value
        val updated = state.tickets.first { it.code == code }
        assertEquals(TicketStatus.EN_REVISION, updated.status)
        assertEquals("Revisaremos tu caso", updated.lastReply)
        assertEquals("", state.reply)
        assertEquals("Respuesta enviada", state.message)
    }

    @Test
    fun `responder con texto vacio no cambia nada`() {
        val vm = vm()
        val code = vm.uiState.value.visible.first().code
        vm.onOpen(code)
        vm.onReplyChange("   ")
        vm.onSendReply()

        val state = vm.uiState.value
        assertEquals(TicketStatus.NUEVA, state.tickets.first { it.code == code }.status)
        assertNull(state.message)
    }

    @Test
    fun `responder sin solicitud abierta no hace nada`() {
        val vm = vm()
        val before = vm.uiState.value.tickets
        vm.onReplyChange("hola")
        vm.onSendReply()

        assertEquals(before, vm.uiState.value.tickets)
        assertNull(vm.uiState.value.message)
    }

    @Test
    fun `resolver marca la solicitud como resuelta y muestra esa pestana`() {
        val vm = vm()
        val code = vm.uiState.value.visible.first().code
        vm.onOpen(code)
        vm.onResolve()

        val state = vm.uiState.value
        assertEquals(TicketStatus.RESUELTA, state.tickets.first { it.code == code }.status)
        assertEquals(TicketStatus.RESUELTA, state.filter)
        assertNull(state.selectedCode)
        assertEquals("Solicitud marcada como resuelta", state.message)
    }

    @Test
    fun `resolver sin solicitud abierta no hace nada`() {
        val vm = vm()
        val before = vm.uiState.value
        vm.onResolve()

        assertEquals(before.tickets, vm.uiState.value.tickets)
        assertEquals(before.filter, vm.uiState.value.filter)
    }

    // ---------- Contacto visible ----------

    @Test
    fun `editar contacto copia el valor actual al borrador`() {
        val vm = vm()
        vm.onEditContact()

        val state = vm.uiState.value
        assertTrue(state.editingContact)
        assertEquals(state.contact, state.contactDraft)
    }

    @Test
    fun `guardar un contacto valido lo actualiza y recorta los espacios`() {
        val vm = vm()
        vm.onEditContact()
        vm.onContactChange(ContactInfo("  nuevo@escuelamilitar.cl ", " +56 9 1111 2222 ", " 09:00–17:00 "))
        vm.onSaveContact()

        val state = vm.uiState.value
        assertEquals(ContactInfo("nuevo@escuelamilitar.cl", "+56 9 1111 2222", "09:00–17:00"), state.contact)
        assertFalse(state.editingContact)
        assertEquals("Contacto actualizado", state.message)
    }

    @Test
    fun `guardar un contacto invalido muestra el error y no lo cambia`() {
        val vm = vm()
        val original = vm.uiState.value.contact
        vm.onEditContact()
        vm.onContactChange(ContactInfo("sin-arroba", "123", "08:00"))
        vm.onSaveContact()

        val state = vm.uiState.value
        assertNotNull(state.contactError)
        assertEquals(original, state.contact)
        assertTrue(state.editingContact)
    }

    @Test
    fun `cambiar el borrador limpia el error y cerrar el aviso lo borra`() {
        val vm = vm()
        vm.onEditContact()
        vm.onContactChange(ContactInfo("x", "", ""))
        vm.onSaveContact()
        assertNotNull(vm.uiState.value.contactError)

        vm.onContactChange(ContactInfo("ok@correo.cl", "123", "08:00"))
        assertNull(vm.uiState.value.contactError)

        vm.onSaveContact()
        vm.onMessageShown()
        assertNull(vm.uiState.value.message)
    }

    // ---------- Validador y resumen ----------

    @Test
    fun `ContactValidator valida correo, telefono y horario`() {
        assertEquals("Ingresa un correo válido", ContactValidator.validate(ContactInfo("abc", "1", "1")))
        assertEquals("Ingresa un teléfono", ContactValidator.validate(ContactInfo("a@b.cl", " ", "1")))
        assertEquals("Ingresa el horario de atención", ContactValidator.validate(ContactInfo("a@b.cl", "1", "")))
        assertNull(ContactValidator.validate(ContactInfo("a@b.cl", "1", "08:00")))
    }

    @Test
    fun `el resumen depende del estado de la solicitud`() {
        assertEquals("Sin respuesta", ticket("1", TicketStatus.NUEVA).summary)
        assertEquals("Mensaje", ticket("2", TicketStatus.EN_REVISION).summary)
        assertEquals("Respondida por el administrador", ticket("3", TicketStatus.RESUELTA).summary)
    }
}