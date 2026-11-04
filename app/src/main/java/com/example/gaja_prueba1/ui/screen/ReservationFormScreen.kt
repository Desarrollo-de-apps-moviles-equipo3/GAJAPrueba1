package com.example.gaja_prueba1.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.ReservationViewModel
import com.example.gaja_prueba1.validation.ReservationValidator
import java.util.Calendar
import java.util.Locale

@Composable
fun ReservationFormScreen(
    space: Space,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onViewReservations: () -> Unit,
    viewModel: ReservationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var detailsStep by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(space.id) { viewModel.start(space) }

    if (state.saved) {
        ReservationSuccessScreen(
            spaceName = space.name,
            dateMillis = state.dateMillis ?: System.currentTimeMillis(),
            slot = state.selectedSlot.orEmpty(),
            onViewReservations = onViewReservations,
            onHome = onDone
        )
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().background(EspaciosPalette.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { if (detailsStep) detailsStep = false else onBack() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = EspaciosPalette.Steel)
            }
            Text("Nueva reserva", color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold)
        }
        StepHeader(activeStep = if (detailsStep) 3 else 2)
        Text(space.name, modifier = Modifier.padding(start = 20.dp, top = 18.dp), color = EspaciosPalette.Navy, fontWeight = FontWeight.Medium)
        Text(space.category, modifier = Modifier.padding(start = 20.dp, top = 2.dp), color = EspaciosPalette.Slate, fontSize = 12.sp)

        AnimatedContent(
            modifier = Modifier.fillMaxWidth().weight(1f),
            targetState = detailsStep,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "reservationStep"
        ) { showDetails ->
            if (!showDetails) {
                DateStep(
                    selectedMillis = state.dateMillis,
                    error = state.dateError,
                    onSelect = viewModel::onDateSelected,
                    onNext = { if (viewModel.validateDateSelection()) detailsStep = true }
                )
            } else {
                DetailsStep(
                    state = state,
                    onSlot = viewModel::onSlotSelected,
                    onReason = viewModel::onReasonChanged,
                    onSubmit = viewModel::submit
                )
            }
        }
    }
}

@Composable
private fun StepHeader(activeStep: Int) {
    val labels = listOf("Cancha", "Fecha", "Horario")
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        labels.forEachIndexed { index, label ->
            val number = index + 1
            val active = number == activeStep
            val done = number < activeStep
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(
                    modifier = Modifier.size(22.dp).background(
                        if (active) EspaciosPalette.Navy else EspaciosPalette.White,
                        CircleShape
                    ), contentAlignment = Alignment.Center
                ) {
                    if (done) Icon(Icons.Filled.Check, null, tint = EspaciosPalette.Steel, modifier = Modifier.size(15.dp))
                    else Text(number.toString(), color = if (active) EspaciosPalette.White else EspaciosPalette.Slate, fontSize = 10.sp)
                }
                Text(label, color = if (active) EspaciosPalette.Navy else EspaciosPalette.Slate, fontSize = 11.sp)
            }
        }
    }
}

private fun allowedDates(): List<Long> = (0..ReservationValidator.MAX_DAYS_AHEAD).map { offset ->
    Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, offset)
    }.timeInMillis
}

@Composable
private fun DateStep(
    selectedMillis: Long?,
    error: String?,
    onSelect: (Long) -> Unit,
    onNext: () -> Unit
) {
    val dates = remember { allowedDates() }
    val selected = selectedMillis ?: dates.first()
    val monthLabel = remember(selected) { ReservationValidator.formatDate(selected, "MMMM yyyy").replaceFirstChar { it.titlecase(Locale("es", "CL")) } }

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.CalendarMonth, null, tint = EspaciosPalette.Steel)
            Spacer(Modifier.width(8.dp))
            Text(monthLabel, color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold)
        }

        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            dates.take(7).forEach { millis ->
                val cal = Calendar.getInstance().apply {
                    timeInMillis = millis
                }

                val isSelected =
                    ReservationValidator.startOfDay(millis).timeInMillis ==
                            ReservationValidator.startOfDay(selected).timeInMillis

                Column(
                    modifier = Modifier
                        .clickable {
                            onSelect(millis)
                        }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        ReservationValidator
                            .formatDate(millis, "EEEEE")
                            .uppercase(Locale("es", "CL")),
                        color = EspaciosPalette.Slate,
                        fontSize = 10.sp
                    )

                    Spacer(Modifier.height(7.dp))

                    Box(
                        Modifier
                            .size(30.dp)
                            .background(
                                if (isSelected) {
                                    EspaciosPalette.Navy
                                } else {
                                    EspaciosPalette.White
                                },
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            cal.get(Calendar.DAY_OF_MONTH).toString(),
                            color = if (isSelected) {
                                EspaciosPalette.White
                            } else {
                                EspaciosPalette.Navy
                            },
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Text("Selecciona una fecha", modifier = Modifier.padding(start = 20.dp, top = 18.dp, bottom = 8.dp), color = EspaciosPalette.Navy, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(dates) { millis ->
                val isSelected = ReservationValidator.startOfDay(millis).timeInMillis == ReservationValidator.startOfDay(selected).timeInMillis
                Row(
                    Modifier.fillMaxWidth().background(if (isSelected) EspaciosPalette.SkyCard else EspaciosPalette.White, RoundedCornerShape(10.dp))
                        .clickable { onSelect(millis) }.padding(horizontal = 12.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ReservationValidator.formatDate(millis), modifier = Modifier.weight(1f), color = EspaciosPalette.Navy, fontSize = 13.sp)
                    if (millis == dates.first()) Text("Hoy", color = EspaciosPalette.Steel, fontSize = 11.sp)
                    else RadioButton(selected = isSelected, onClick = { onSelect(millis) }, colors = RadioButtonDefaults.colors(selectedColor = EspaciosPalette.Navy))
                }
            }
        }
        if (error != null) ErrorLine(error)
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EspaciosPalette.Navy)
        ) { Text("Siguiente  →") }
    }
}

@Composable
private fun DetailsStep(
    state: com.example.gaja_prueba1.ui.viewmodels.ReservationUiState,
    onSlot: (String) -> Unit,
    onReason: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Horario disponible", color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
        Text("Selecciona un bloque de 2 horas", color = EspaciosPalette.Slate, fontSize = 12.sp)
        if (state.isLoadingSlots) {
            Text("Cargando horarios…", color = EspaciosPalette.Slate, fontSize = 12.sp)
        }
        state.slotsError?.let { ErrorLine(it) }
        Spacer(Modifier.height(10.dp))
        ReservationValidator.SLOTS.chunked(2).forEach { rowSlots ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                rowSlots.forEach { slot ->
                    val selected = state.selectedSlot == slot
                    val unavailable = slot in state.unavailableSlots
                    OutlinedButton(
                        onClick = { onSlot(slot) },
                        enabled = !unavailable && !state.isLoadingSlots && state.slotsError == null,
                        modifier = Modifier.weight(1f).height(52.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (selected) EspaciosPalette.Navy else EspaciosPalette.White,
                            contentColor = if (selected) EspaciosPalette.White else EspaciosPalette.Navy,
                            disabledContainerColor = EspaciosPalette.Beige,
                            disabledContentColor = EspaciosPalette.Slate
                        )
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(slot, fontSize = 12.sp)
                            if (unavailable) Text("Ocupado", fontSize = 9.sp)
                        }
                    }
                }
            }
        }
        if (state.slotError != null) ErrorLine(state.slotError)
        Spacer(Modifier.height(12.dp))
        Text("Motivo de la reserva", color = EspaciosPalette.Navy, fontWeight = FontWeight.Medium)
        OutlinedTextField(
            value = state.reason,
            onValueChange = onReason,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            placeholder = { Text("Ej. Entrenamiento, reunión o actividad") },
            trailingIcon = {
                if (state.reasonError != null) {
                    Icon(
                        Icons.Filled.Error,
                        contentDescription = "Error",
                        tint = EspaciosPalette.Danger
                    )
                }
            },
            minLines = 2,
            isError = state.reasonError != null,
            supportingText = state.reasonError?.let { message -> { Text(message) } },
            shape = RoundedCornerShape(10.dp)
        )
        Spacer(Modifier.height(10.dp))
        state.saveError?.let { ErrorLine(it) }
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onSubmit,
            enabled = !state.isSaving && !state.isLoadingSlots && state.slotsError == null,
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(bottom = 4.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EspaciosPalette.Navy)
        ) {
            if (state.isSaving) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = EspaciosPalette.White, strokeWidth = 2.dp)
            else Text("Confirmar reserva")
        }
    }
}

@Composable
private fun ErrorLine(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Icon(Icons.Filled.Error, contentDescription = "Error", tint = EspaciosPalette.Danger, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(message, color = EspaciosPalette.Danger, fontSize = 12.sp)
    }
}
