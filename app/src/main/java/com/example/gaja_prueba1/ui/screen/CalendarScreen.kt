package com.example.gaja_prueba1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.SlotStatus
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.model.SpaceAvailability
import com.example.gaja_prueba1.ui.components.GajaButton
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaFilterChip
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.CalendarMode
import com.example.gaja_prueba1.ui.viewmodels.CalendarUiState
import com.example.gaja_prueba1.ui.viewmodels.CalendarViewModel
import com.example.gaja_prueba1.validation.ReservationValidator

private val DAY_INITIALS = listOf("L", "M", "M", "J", "V", "S", "D")

private class SlotStyle(val background: Color, val content: Color, val symbol: String, val label: String)

private fun SlotStatus.style(): SlotStyle = when (this) {
    SlotStatus.Libre -> SlotStyle(EspaciosPalette.GreenSoft, EspaciosPalette.Green, "✓", "Libre")
    SlotStatus.Reservado -> SlotStyle(EspaciosPalette.SkyCard, EspaciosPalette.Steel, "R", "Reservado")
    SlotStatus.Bloqueado -> SlotStyle(EspaciosPalette.DangerSoft, EspaciosPalette.Danger, "✕", "Bloqueado")
    SlotStatus.Pasado -> SlotStyle(EspaciosPalette.Beige, EspaciosPalette.Slate, "–", "Pasado")
}

@Composable
fun CalendarScreen(
    onReserveClick: (Space) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Cada vez que se abre la pestaña se piden de nuevo los bloqueos, por si el admin bloqueó algo.
    LaunchedEffect(Unit) { viewModel.refresh() }

    CalendarContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSelectSpace = viewModel::onSelectSpace,
        onModeChange = viewModel::onModeChange,
        onPrevious = viewModel::onPrevious,
        onNext = viewModel::onNext,
        onSelectDay = viewModel::onSelectDay,
        onRetry = viewModel::refresh,
        onReserveClick = onReserveClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarContent(
    state: CalendarUiState,
    onQueryChange: (String) -> Unit,
    onSelectSpace: (String) -> Unit,
    onModeChange: (CalendarMode) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectDay: (String) -> Unit,
    onRetry: () -> Unit,
    onReserveClick: ((Space) -> Unit)?,
    modifier: Modifier = Modifier,
    // Solo la agenda del admin lo usa: al tocar un día se abre su edición.
    onDayClick: ((String) -> Unit)? = null
) {
    // Tirar hacia abajo vuelve a pedir espacios, bloqueos y reservas.
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRetry,
        modifier = modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .statusBarsPadding()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Calendario",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = EspaciosPalette.Navy
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar espacio") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EspaciosPalette.White,
                unfocusedContainerColor = EspaciosPalette.White
            )
        )

        when {
            state.isLoading -> Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            state.bookable.isEmpty() && state.errorMessage != null -> ErrorRow(state.errorMessage, onRetry)

            else -> {
                state.errorMessage?.let { ErrorRow(it, onRetry) }
                SpaceChips(state = state, onSelectSpace = onSelectSpace)

                val space = state.selectedSpace
                if (space == null) {
                    Text(
                        text = "Elige un espacio para ver su disponibilidad.",
                        color = EspaciosPalette.Slate
                    )
                } else {
                    SelectedSpaceHeader(space = space, onReserveClick = onReserveClick)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CalendarMode.entries.forEach { mode ->
                            GajaFilterChip(
                                text = mode.label,
                                selected = state.mode == mode,
                                onClick = { onModeChange(mode) }
                            )
                        }
                    }

                    if (state.mode == CalendarMode.Semana) {
                        WeekView(
                            space = space,
                            state = state,
                            onPrevious = onPrevious,
                            onNext = onNext,
                            onDayClick = onDayClick
                        )
                    } else {
                        MonthView(
                            space = space,
                            state = state,
                            onPrevious = onPrevious,
                            onNext = onNext,
                            onSelectDay = onSelectDay,
                            onDayClick = onDayClick
                        )
                    }
                    Legend()
                }
            }
        }
    }
    }
}

@Composable
private fun ErrorRow(message: String, onRetry: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    GajaCard(modifier = Modifier.fillMaxWidth(), containerColor = scheme.errorContainer) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.CloudOff, contentDescription = null, tint = scheme.onErrorContainer)
            Text(
                message,
                color = scheme.onErrorContainer,
                modifier = Modifier.weight(1f).padding(start = 9.dp),
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onRetry) {
                Text("Reintentar", color = scheme.onErrorContainer)
            }
        }
    }
}

@Composable
private fun SpaceChips(state: CalendarUiState, onSelectSpace: (String) -> Unit) {
    if (state.results.isEmpty()) {
        Text("No encontramos espacios con ese nombre.", color = EspaciosPalette.Slate)
        return
    }
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        items(state.results, key = { it.id }) { space ->
            GajaFilterChip(
                text = space.name,
                selected = space.id == state.selectedSpaceId,
                onClick = { onSelectSpace(space.id) }
            )
        }
    }
}

@Composable
private fun SelectedSpaceHeader(space: Space, onReserveClick: ((Space) -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = space.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = EspaciosPalette.Navy
            )
            Text(
                text = "${space.category} · ${space.capacity} personas",
                style = MaterialTheme.typography.bodySmall,
                color = EspaciosPalette.Slate
            )
        }
        if (onReserveClick != null) {
            GajaButton(text = "Reservar", onClick = { onReserveClick(space) }, modifier = Modifier.width(130.dp))
        }
    }
}

// ---------- Semana ----------

@Composable
private fun WeekView(
    space: Space,
    state: CalendarUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDayClick: ((String) -> Unit)?
) {
    val days = SpaceAvailability.weekDays(state.nowMillis, state.weekOffset)
    val title = "${ReservationValidator.formatDate(SpaceAvailability.parseIso(days.first()), "d MMM")} – " +
            ReservationValidator.formatDate(SpaceAvailability.parseIso(days.last()), "d MMM")

    Navigator(title = title, onPrevious = onPrevious, onNext = onNext)

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Spacer(Modifier.width(TIME_COLUMN_WIDTH))
            days.forEachIndexed { index, day ->
                val isToday = day == state.todayIso
                Column(
                    modifier = Modifier.weight(1f).clickableIf(onDayClick, day),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(DAY_INITIALS[index], fontSize = 11.sp, color = EspaciosPalette.Slate)
                    Text(
                        text = SpaceAvailability.dayOfMonth(day).toString(),
                        fontSize = 13.sp,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                        color = if (isToday) EspaciosPalette.Orange else EspaciosPalette.Navy
                    )
                }
            }
        }
        ReservationValidator.SLOTS.forEach { slot ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = slot.substringBefore(" - "),
                    fontSize = 11.sp,
                    color = EspaciosPalette.Slate,
                    modifier = Modifier.width(TIME_COLUMN_WIDTH)
                )
                days.forEach { day ->
                    val status = SpaceAvailability.slotStatus(
                        space, day, slot, state.todayIso, state.blocks, state.reservations
                    )
                    val style = status.style()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp)
                            .height(34.dp)
                            .background(style.background, RoundedCornerShape(8.dp))
                            .clickableIf(onDayClick, day),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(style.symbol, color = style.content, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

private val TIME_COLUMN_WIDTH = 44.dp

// ---------- Mes ----------

@Composable
private fun MonthView(
    space: Space,
    state: CalendarUiState,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelectDay: (String) -> Unit,
    onDayClick: ((String) -> Unit)?
) {
    val grid = SpaceAvailability.monthGrid(state.nowMillis, state.monthOffset)
    Navigator(title = SpaceAvailability.monthLabel(grid), onPrevious = onPrevious, onNext = onNext)

    Row(modifier = Modifier.fillMaxWidth()) {
        DAY_INITIALS.forEach {
            Text(
                text = it,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = EspaciosPalette.Slate
            )
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        grid.cells.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                week.forEach { day ->
                    if (day == null) {
                        Spacer(Modifier.weight(1f).height(46.dp))
                    } else {
                        MonthDayCell(
                            day = day,
                            space = space,
                            state = state,
                            selected = day == state.selectedDay,
                            onClick = { onSelectDay(day) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    state.selectedDay?.let { DayDetail(day = it, space = space, state = state, onEdit = onDayClick) }
}

@Composable
private fun MonthDayCell(
    day: String,
    space: Space,
    state: CalendarUiState,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPast = day < state.todayIso
    val free = SpaceAvailability.freeCount(space, day, state.todayIso, state.blocks, state.reservations)
    val style = when {
        isPast -> SlotStatus.Pasado.style()
        free == 0 -> SlotStatus.Bloqueado.style()
        else -> SlotStatus.Libre.style()
    }
    Column(
        modifier = modifier
            .height(46.dp)
            .background(style.background, RoundedCornerShape(8.dp))
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) EspaciosPalette.Navy else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = SpaceAvailability.dayOfMonth(day).toString(),
            fontSize = 13.sp,
            fontWeight = if (day == state.todayIso) FontWeight.Bold else FontWeight.Medium,
            color = if (day == state.todayIso) EspaciosPalette.Orange else style.content
        )
        if (!isPast) {
            Text(text = "$free/${ReservationValidator.SLOTS.size}", fontSize = 10.sp, color = style.content)
        }
    }
}

@Composable
private fun DayDetail(day: String, space: Space, state: CalendarUiState, onEdit: ((String) -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(EspaciosPalette.White, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = ReservationValidator.formatDate(SpaceAvailability.parseIso(day)).replaceFirstChar { it.uppercase() },
            fontWeight = FontWeight.SemiBold,
            color = EspaciosPalette.Navy
        )
        SpaceAvailability.daySlots(space, day, state.todayIso, state.blocks, state.reservations)
            .forEach { (slot, status) ->
                val style = status.style()
                val reason = if (status == SlotStatus.Bloqueado) {
                    SpaceAvailability.blockFor(space, day, slot, state.blocks)?.reason?.takeIf { it.isNotBlank() }
                } else null
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(slot, modifier = Modifier.width(110.dp), color = EspaciosPalette.Navy, fontSize = 13.sp)
                    Text(
                        text = style.label + (reason?.let { " · $it" } ?: ""),
                        color = style.content,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .background(style.background, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    )
                }
            }
        if (onEdit != null && day >= state.todayIso) {
            TextButton(onClick = { onEdit(day) }) { Text("Editar disponibilidad") }
        }
    }
}

// ---------- Piezas compartidas ----------

@Composable
private fun Navigator(title: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onPrevious) { Text("‹ Anterior") }
        Text(title, fontWeight = FontWeight.SemiBold, color = EspaciosPalette.Navy)
        TextButton(onClick = onNext) { Text("Siguiente ›") }
    }
}

@Composable
private fun Legend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(SlotStatus.Libre, SlotStatus.Reservado, SlotStatus.Bloqueado, SlotStatus.Pasado).forEach { status ->
            val style = status.style()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(style.background, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(style.symbol, color = style.content, fontSize = 9.sp)
                }
                Text(style.label, fontSize = 11.sp, color = EspaciosPalette.Slate)
            }
        }
    }
}

/** Hace clicable el elemento solo si hay una acción de día (agenda del admin). */
private fun Modifier.clickableIf(action: ((String) -> Unit)?, day: String): Modifier =
    if (action == null) this else this.clickable { action(day) }
