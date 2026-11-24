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

    LaunchedEffect(email) {
        viewModel.loadReservations(email)
    }

    var selectedTabName by rememberSaveable {
        mutableStateOf(ReservationTab.PENDIENTES.name)
    }
    val selectedTab = ReservationTab.valueOf(selectedTabName)

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

            state.error != null -> {
                Text(
                    text = state.error.orEmpty(),
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }

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
                            onCancelClick = { reservationToCancel = reservation }
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
}

@Composable
private fun ReservationCard(
    reservation: ReservationResponse,
    isPast: Boolean,
    onCancelClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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

            val canCancel = !isPast && (
                reservation.estado.equals("PENDIENTE", ignoreCase = true) ||
                    reservation.estado.equals("APROBADA", ignoreCase = true)
            )

            if (canCancel) {
                TextButton(
                    onClick = onCancelClick,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cancelar", color = EspaciosPalette.Orange)
                }
            }
        }
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
