package com.example.gaja_prueba1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.MyReservationsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class ReservationTab(val label: String) {
    PENDIENTES("Pendientes"),
    PROXIMAS("Próximas"),
    PASADAS("Pasadas"),
    CANCELADAS("Canceladas")
}

private data class ReservationDetailStatus(
    val label: String,
    val message: String? = null
)

@Composable
fun MyReservationsScreen(
    email: String,
    onBack: () -> Unit,
    viewModel: MyReservationsViewModel = viewModel()
) {
    val reservations by viewModel.reservations.collectAsState()
    val state by viewModel.uiState.collectAsState()
    var reservationToCancel by remember {
        mutableStateOf<ReservationResponse?>(null)
    }
    var reservationToHide by remember {
        mutableStateOf<ReservationResponse?>(null)
    }
    var reservationDetail by remember {
        mutableStateOf<ReservationResponse?>(null) }

    LaunchedEffect(email) {
        viewModel.loadReservations(email)
    }

    var selectedTabName by rememberSaveable {
        mutableStateOf(ReservationTab.PENDIENTES.name)
    }
    val selectedTab = ReservationTab.valueOf(selectedTabName)

    if (state.cancellationCompleted) {
        ReservationCancelledScreen(
            reservation = state.cancelledReservation,
            onViewCancelled = {
                selectedTabName = ReservationTab.CANCELADAS.name
                viewModel.clearCancellationCompleted()
            },
            onHome = {
                viewModel.clearCancellationCompleted()
                onBack()
            }
        )
        return
    }

    reservationDetail?.let { reservation ->
        val detalleEstado = reservationDetailStatus(reservation)
        val estado = reservation.estado.uppercase(Locale.ROOT)
        val late = detalleEstado.label == "Fuera de plazo"
        val chip = when {
            late && estado == "PENDIENTE" -> "Pendiente"
            late -> "Confirmada"
            else -> detalleEstado.label
        }
        ReservationDetailScreen(
            reservation = reservation,
            statusLabel = chip,
            notice = detalleEstado.message,
            cancelEnabled = !late,
            showCancel = (estado == "PENDIENTE" || estado == "APROBADA") && detalleEstado.label != "Pasada",
            reservedBy = displayNameFromEmail(reservation.usuarioEmail),
            onBack = { reservationDetail = null },
            onCancel = {
                reservationToCancel = reservation
                reservationDetail = null
            }
        )
        return
    }

    val today = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    val pendientes = reservations.filter {
        it.fecha >= today && it.estado.equals("PENDIENTE", ignoreCase = true)
    }

    val proximas = reservations.filter {
        it.fecha >= today && it.estado.equals("APROBADA", ignoreCase = true)
    }

    val pasadas = reservations.filter {
        it.fecha < today &&
                !it.estado.equals("CANCELADA", ignoreCase = true) &&
                !it.estado.equals("RECHAZADA", ignoreCase = true)
    }

    val canceladas = reservations.filter {
        it.estado.equals("CANCELADA", ignoreCase = true) ||
                it.estado.equals("RECHAZADA", ignoreCase = true)
    }

    val visibleReservations = when (selectedTab) {
        ReservationTab.PENDIENTES -> pendientes
        ReservationTab.PROXIMAS -> proximas
        ReservationTab.PASADAS -> pasadas
        ReservationTab.CANCELADAS -> canceladas
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, bottom = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ChevronLeft,
                    contentDescription = "Volver",
                    tint = EspaciosPalette.Steel
                )
            }

            Text(
                text = "Mis reservas",
                modifier = Modifier.padding(start = 10.dp),
                color = EspaciosPalette.Navy,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(EspaciosPalette.SkyCard)
                .horizontalScroll(rememberScrollState())
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ReservationTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val count = when (tab) {
                    ReservationTab.PENDIENTES -> pendientes.size
                    ReservationTab.PROXIMAS -> proximas.size
                    ReservationTab.PASADAS -> pasadas.size
                    ReservationTab.CANCELADAS -> canceladas.size
                }

                Surface(
                    onClick = { selectedTabName = tab.name },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) {
                        EspaciosPalette.Navy
                    } else {
                        EspaciosPalette.SkyCard
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 8.dp,
                            vertical = 10.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = tab.label,
                            fontSize = 10.sp,
                            color = if (selected) {
                                EspaciosPalette.White
                            } else {
                                EspaciosPalette.Slate
                            },
                            fontWeight = if (selected) {
                                FontWeight.SemiBold
                            } else {
                                FontWeight.Normal
                            }
                        )
                        Text(
                            text = " $count",
                            fontSize = 10.sp,
                            color = if (selected) {
                                EspaciosPalette.White
                            } else {
                                EspaciosPalette.Slate
                            }
                        )
                    }
                }
            }
        }





        Spacer(modifier = Modifier.size(16.dp))

        state.cancelError?.let { message ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error
                )
                TextButton(onClick = viewModel::clearCancelError) {
                    Text("Cerrar")
                }
            }
        }

        state.hideError?.let { message ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(message, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::clearHideError) { Text("Cerrar") }
            }
        }

        if (state.error != null && reservations.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = state.error.orEmpty(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp
                )
                TextButton(onClick = { viewModel.loadReservations(email) }) {
                    Text("Reintentar")
                }
            }
        }

        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Cargando tus reservas…",
                        modifier = Modifier.padding(top = 12.dp),
                        color = EspaciosPalette.Slate
                    )
                }
            }

            state.error != null && reservations.isEmpty() -> OfflineErrorScreen(
                message = state.error.orEmpty(),
                onRetry = { viewModel.loadReservations(email) },
                modifier = Modifier.weight(1f)
            )

            visibleReservations.isEmpty() -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = EspaciosPalette.Slate,
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = when (selectedTab) {
                            ReservationTab.PENDIENTES ->
                                "No tienes solicitudes pendientes"
                            ReservationTab.PROXIMAS ->
                                "No tienes reservas próximas"
                            ReservationTab.PASADAS ->
                                "No tienes reservas pasadas"
                            ReservationTab.CANCELADAS ->
                                "No tienes reservas canceladas"
                        },
                        modifier = Modifier.padding(top = 12.dp),
                        color = EspaciosPalette.Navy,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tus reservas aparecerán aquí.",
                        modifier = Modifier.padding(top = 6.dp),
                        color = EspaciosPalette.Slate,
                        fontSize = 12.sp
                    )
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = visibleReservations,
                        key = { reservation -> reservation.id }
                    ) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            isPast = selectedTab == ReservationTab.PASADAS,
                            onCancelClick = { reservationToCancel = reservation },
                            canHide = canHideReservation(reservation),
                            isHiding = state.hidingReservationId == reservation.id,
                            onHideClick = { reservationToHide = reservation },
                            onClick = { reservationDetail = reservation }
                        )
                    }
                }
            }
        }
    }

    reservationToCancel?.let { reservation ->
        AlertDialog(
            onDismissRequest = { reservationToCancel = null },
            title = { Text("Cancelar reserva") },
            text = {
                Text("¿Quieres cancelar la reserva de ${reservation.espacio}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelReservation(reservation.id, email)
                        reservationToCancel = null
                    }
                ) {
                    Text("Sí, cancelar")
                }
            },
            dismissButton = {
                TextButton(onClick = { reservationToCancel = null }) {
                    Text("Volver")
                }
            }
        )
    }

    reservationToHide?.let { reservation ->
        AlertDialog(
            onDismissRequest = { reservationToHide = null },
            title = { Text("Quitar de Mis reservas") },
            text = {
                Text("Se ocultará de tu lista, pero el administrador conservará el registro. ¿Continuar?")
            },
            confirmButton = {
                TextButton(
                    enabled = state.hidingReservationId == null,
                    onClick = {
                        viewModel.hideReservation(reservation.id, email)
                        reservationToHide = null
                    }
                ) { Text("Quitar de mi lista") }
            },
            dismissButton = {
                TextButton(onClick = { reservationToHide = null }) { Text("Volver") }
            }
        )
    }

}
@Composable
private fun ReservationCard(
    reservation: ReservationResponse,
    isPast: Boolean,
    canHide: Boolean,
    isHiding: Boolean,
    onCancelClick: () -> Unit,
    onHideClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EspaciosPalette.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(11.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = EspaciosPalette.Beige
                ) {
                    Icon(
                        imageVector = Icons.Filled.SportsSoccer,
                        contentDescription = "Cancha deportiva",
                        tint = EspaciosPalette.Navy,
                        modifier = Modifier.padding(9.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp)
                ) {
                    Text(
                        text = reservation.espacio,
                        color = EspaciosPalette.Navy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${formatDate(reservation.fecha)} · ${reservation.horario}",
                        modifier = Modifier.padding(top = 4.dp),
                        color = EspaciosPalette.Slate,
                        fontSize = 10.sp
                    )
                    Surface(
                        modifier = Modifier.padding(top = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = EspaciosPalette.Beige
                    ) {
                        Text(
                            text = reservation.estado.replaceFirstChar {
                                it.titlecase(Locale("es", "CL"))
                            },
                            modifier = Modifier.padding(
                                horizontal = 9.dp,
                                vertical = 4.dp
                            ),
                            color = EspaciosPalette.Orange,
                            fontSize = 10.sp
                        )
                    }
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = EspaciosPalette.Steel
                )
            }

            Text(
                text = if (isPast) {
                    "Reserva #${reservation.id} · Fecha finalizada"
                } else {
                    "Reserva #${reservation.id}"
                },
                modifier = Modifier.padding(start = 52.dp, top = 8.dp),
                color = EspaciosPalette.Slate,
                fontSize = 9.sp
            )

            if (reservation.estado.equals("RECHAZADA", ignoreCase = true) &&
                !reservation.motivoRechazo.isNullOrBlank()
            ) {
                Text(
                    text = "Motivo del rechazo: ${reservation.motivoRechazo}",
                    modifier = Modifier.padding(start = 52.dp, top = 4.dp),
                    color = EspaciosPalette.Danger,
                    fontSize = 10.sp
                )
            }

            val canCancel = !isPast && (
                    reservation.estado.equals("PENDIENTE", ignoreCase = true) ||
                            reservation.estado.equals("APROBADA", ignoreCase = true)
                    )

            if (canCancel || canHide) {
                Row(
                    modifier = Modifier.align(Alignment.End),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (canHide) {
                        TextButton(onClick = onHideClick, enabled = !isHiding) {
                            Text(if (isHiding) "Quitando…" else "Quitar de mi lista", color = EspaciosPalette.Slate)
                        }
                    }
                    if (canCancel) {
                        TextButton(onClick = onCancelClick) {
                            Text("Cancelar", color = EspaciosPalette.Orange)
                        }
                    }
                }
            }
        }
    }
}

private fun canHideReservation(
    reservation: ReservationResponse,
    nowMillis: Long = System.currentTimeMillis()
): Boolean {
    return when (reservation.estado.uppercase(Locale.ROOT)) {
        "CANCELADA", "RECHAZADA" -> true
        "APROBADA" -> {
            val endText = reservation.horario.split("-").getOrNull(1)?.trim() ?: return false
            val endMillis = parseReservationTime(reservation.fecha, endText) ?: return false
            nowMillis >= endMillis
        }
        else -> false
    }
}

private fun formatDate(date: String): String {
    return try {
        val parsed = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.ROOT
        ).parse(date) ?: return date

        SimpleDateFormat(
            "EEE · dd MMM yyyy",
            Locale("es", "CL")
        ).format(parsed).replaceFirstChar { it.titlecase(Locale("es", "CL")) }
    } catch (_: Exception) {
        date
    }
}


private fun reservationDetailStatus(
    reservation: ReservationResponse,
    nowMillis: Long = System.currentTimeMillis()
): ReservationDetailStatus {
    val estado = reservation.estado.uppercase(Locale.ROOT)

    if (estado == "RECHAZADA") {
        return ReservationDetailStatus("Rechazada")
    }

    if (estado == "CANCELADA") {
        return ReservationDetailStatus("Cancelada")
    }

    val horas = reservation.horario.split("-")
    val inicio = horas.getOrNull(0)?.trim()
        ?.let { parseReservationTime(reservation.fecha, it) }
    val termino = horas.getOrNull(1)?.trim()
        ?.let { parseReservationTime(reservation.fecha, it) }

    if (inicio != null && termino != null) {
        if (estado == "PENDIENTE" && nowMillis >= inicio) {
            return ReservationDetailStatus(
                "Fuera de plazo",
                "La solicitud sigue pendiente, pero el horario ya comenzó."
            )
        }

        if (nowMillis >= termino) {
            return ReservationDetailStatus(
                "Pasada",
                "El horario de esta reserva ya finalizó."
            )
        }

        if (nowMillis >= inicio - 2 * 60 * 60 * 1000L) {
            return ReservationDetailStatus(
                "Fuera de plazo",
                "Faltan menos de 2 horas: ya no puedes cancelar esta reserva."
            )
        }
    }

    return when (estado) {
        "PENDIENTE" -> ReservationDetailStatus("Pendiente")
        "APROBADA" -> ReservationDetailStatus("Confirmada")
        else -> ReservationDetailStatus(reservation.estado)
    }
}

private fun parseReservationTime(fecha: String, hora: String): Long? {
    return try {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).apply {
            isLenient = false
        }.parse("$fecha $hora")?.time
    } catch (_: Exception) {
        null
    }
}


/** "valentina.diaz@escuelamilitar.cl" → "Valentina Diaz". */
private fun displayNameFromEmail(email: String): String =
    email.substringBefore("@")
        .replace("_", " ")
        .replace(".", " ")
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { part -> part.replaceFirstChar { it.uppercase() } }