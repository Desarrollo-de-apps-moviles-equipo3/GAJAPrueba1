package com.example.gaja_prueba1.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
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
import java.util.TimeZone

@Composable
fun ReservationFormScreen(
    space: Space,
    userEmail: String,
    onBack: () -> Unit,
    onDone: () -> Unit,
    onViewReservations: () -> Unit,
    viewModel: ReservationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var detailsStep by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(space.id, userEmail) {
        viewModel.start(space, userEmail)
    }

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
                    maxDaysAhead = state.rules.maxDaysAhead,
                    error = state.dateError,
                    onSelect = viewModel::onDateSelected,
                    onNext = { if (viewModel.validateDateSelection()) detailsStep = true }
                )
            } else {
                DetailsStep(
                    state = state,
                    onHour = viewModel::onTimeHourSelected,
                    onResetHours = viewModel::onResetTimeSelection,
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

private fun utcMidnightForLocalDate(localMillis: Long): Long {
    val local = Calendar.getInstance().apply { timeInMillis = localMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

private fun localMidnightFromUtcDate(utcMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        clear()
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DateStep(
    selectedMillis: Long?,
    maxDaysAhead: Int,
    error: String?,
    onSelect: (Long) -> Unit,
    onNext: () -> Unit
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val today = remember { ReservationValidator.startOfDay(System.currentTimeMillis()).timeInMillis }
    val lastAllowed = remember(today, maxDaysAhead) {
        (ReservationValidator.startOfDay(today).clone() as Calendar).apply {
            add(Calendar.DAY_OF_YEAR, maxDaysAhead)
        }.timeInMillis
    }
    val selected = selectedMillis
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selected?.let(::utcMidnightForLocalDate) ?: utcMidnightForLocalDate(today),
        selectableDates = object : SelectableDates {
            private val firstUtcDay = utcMidnightForLocalDate(today)
            private val lastUtcDay = utcMidnightForLocalDate(lastAllowed)
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis in firstUtcDay..lastUtcDay
        }
    )

    Column(Modifier.fillMaxSize()) {
        Text("Selecciona una fecha", modifier = Modifier.padding(start = 20.dp, top = 22.dp, bottom = 12.dp), color = EspaciosPalette.Navy, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        OutlinedButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).height(58.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = EspaciosPalette.Navy)
        ) {
            Icon(Icons.Filled.CalendarMonth, contentDescription = null, tint = EspaciosPalette.Steel)
            Spacer(Modifier.width(10.dp))
            Text(
                selected?.let { ReservationValidator.formatDate(it).replaceFirstChar { c -> c.titlecase(Locale("es", "CL")) } }
                    ?: "Elegir fecha",
                modifier = Modifier.weight(1f)
            )
            Text("Cambiar", color = EspaciosPalette.Steel, fontSize = 12.sp)
        }
        Text(
            "Puedes reservar desde hoy hasta $maxDaysAhead días adelante.",
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
            color = EspaciosPalette.Slate,
            fontSize = 12.sp
        )
        Spacer(Modifier.weight(1f))
        if (error != null) ErrorLine(error)
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp).height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EspaciosPalette.Navy)
        ) { Text("Siguiente  →") }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { onSelect(localMidnightFromUtcDate(it)) }
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text("Aceptar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }
}

@Composable
private fun DetailsStep(
    state: com.example.gaja_prueba1.ui.viewmodels.ReservationUiState,
    onHour: (Int) -> Unit,
    onResetHours: () -> Unit,
    onReason: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Text("Horario disponible", color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
        Text("Atención de ${"%02d:00".format(state.rules.openingHour)} a ${"%02d:00".format(state.rules.closingHour)}", color = EspaciosPalette.Slate, fontSize = 12.sp)
        if (state.isLoadingSlots) {
            Text("Cargando horarios…", color = EspaciosPalette.Slate, fontSize = 12.sp)
        }
        state.slotsError?.let { ErrorLine(it) }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(Modifier.weight(1f)) {
                Text("Elige el rango horario", color = EspaciosPalette.Navy, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                if (state.selectedEndHour == null) {
                    Text(
                        if (state.selectedStartHour == null) "Toca la hora inicial y luego la final."
                        else "Desde las %02d:00 · elige la hora final.".format(state.selectedStartHour),
                        color = EspaciosPalette.Slate,
                        fontSize = 11.sp
                    )
                }
            }
            if (state.selectedStartHour != null) {
                FilledTonalButton(
                    onClick = onResetHours,
                    modifier = Modifier.height(36.dp),
                    shape = RoundedCornerShape(50),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = EspaciosPalette.SkyCard,
                        contentColor = EspaciosPalette.Navy
                    )
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Restablecer", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        (ReservationValidator.startHours(state.rules.openingHour, state.rules.closingHour) + state.rules.closingHour)
            .chunked(4).forEach { rowHours ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 7.dp)) {
                rowHours.forEach { hour ->
                    val startHour = state.selectedStartHour
                    val endHour = state.selectedEndHour
                    val isEndpoint = hour == startHour || hour == endHour
                    val isInsideRange = startHour != null && endHour != null && hour in (startHour + 1) until endHour
                    val enabled = when {
                        state.isLoadingSlots || state.slotsError != null -> false
                        startHour == null -> hour < state.rules.closingHour && maxAvailableDurationForUi(state, hour) > 0
                        endHour != null && hour <= startHour ->
                            hour < state.rules.closingHour && maxAvailableDurationForUi(state, hour) > 0
                        hour <= startHour -> hour < state.rules.closingHour && maxAvailableDurationForUi(state, hour) > 0
                        else -> hour - startHour <= maxAvailableDurationForUi(state, startHour)
                    }
                    OutlinedButton(
                        onClick = { onHour(hour) },
                        enabled = enabled,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            when {
                                isEndpoint -> EspaciosPalette.Navy
                                isInsideRange -> EspaciosPalette.Steel
                                enabled -> EspaciosPalette.Beige
                                else -> EspaciosPalette.Beige
                            }
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = when {
                                isEndpoint -> EspaciosPalette.Navy
                                isInsideRange -> EspaciosPalette.SkyCard
                                else -> EspaciosPalette.White
                            },
                            contentColor = if (isEndpoint) EspaciosPalette.White else EspaciosPalette.Navy,
                            disabledContainerColor = EspaciosPalette.Beige.copy(alpha = 0.65f),
                            disabledContentColor = EspaciosPalette.Slate
                        )
                    ) {
                        Text("%02d:00".format(hour), fontSize = 11.sp, fontWeight = if (isEndpoint) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1)
                    }
                }
            }
        }
        state.selectedSlot?.let { slot ->
            Text("Horario elegido: $slot", color = EspaciosPalette.Slate, fontSize = 11.sp)
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

private fun maxAvailableDurationForUi(
    state: com.example.gaja_prueba1.ui.viewmodels.ReservationUiState,
    startHour: Int
): Int {
    val selectedDate = state.dateMillis ?: return 0
    val now = Calendar.getInstance()
    val reservationDay = Calendar.getInstance().apply { timeInMillis = selectedDate }
    val today = Calendar.getInstance()
    val isToday = reservationDay.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
            reservationDay.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
    if (isToday && startHour <= now.get(Calendar.HOUR_OF_DAY)) return 0
    val closingRemaining = state.rules.closingHour - startHour
    val upperBound = closingRemaining
    return (1..upperBound).takeWhile { duration ->
        val candidate = ReservationValidator.formatHourSlot(startHour, duration)
        state.unavailableSlots.none { ReservationValidator.overlaps(candidate, it) }
    }.lastOrNull() ?: 0
}

@Composable
private fun ErrorLine(message: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
        Icon(Icons.Filled.Error, contentDescription = "Error", tint = EspaciosPalette.Danger, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(5.dp))
        Text(message, color = EspaciosPalette.Danger, fontSize = 12.sp)
    }
}
