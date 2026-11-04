
package com.example.gaja_prueba1.ui.screen

import androidx.compose.material3.IconButton
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
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.data.ReservationEntity
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.MyReservationsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

private enum class ReservationTab(val label: String) {
    PENDIENTES("Pendientes"),
    PROXIMAS("Próximas"),
    PASADAS("Pasadas"),
    CANCELADAS("Canceladas")
}

@Composable
fun MyReservationsScreen(
    onBack: () -> Unit,
    viewModel: MyReservationsViewModel = viewModel()
) {
    val reservations by viewModel.reservations.collectAsState()

    var selectedTabName by rememberSaveable {
        mutableStateOf(ReservationTab.PENDIENTES.name)
    }

    val selectedTab = ReservationTab.valueOf(selectedTabName)

    // Las solicitudes existentes son pendientes de aprobación.
    // Sin un estado proveniente del servicio externo no podemos
    // clasificarlas como confirmadas o canceladas.
    val pendingReservations = reservations

    val visibleReservations = when (selectedTab) {
        ReservationTab.PENDIENTES -> pendingReservations
        ReservationTab.PROXIMAS -> emptyList()
        ReservationTab.PASADAS -> emptyList()
        ReservationTab.CANCELADAS -> emptyList()
    }

    val count = when (selectedTab) {
        ReservationTab.PENDIENTES -> pendingReservations.size
        else -> 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EspaciosPalette.Cream)
            .padding(horizontal = 16.dp)
    ) {
        // Encabezado
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

        // Selector de las cuatro categorías
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

                val tabCount = when (tab) {
                    ReservationTab.PENDIENTES ->
                        pendingReservations.size

                    else -> 0
                }

                Surface(
                    onClick = {
                        selectedTabName = tab.name
                    },
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
                            text = " $tabCount",
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

        if (visibleReservations.isEmpty()) {
            // Estado vacío
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
                    text = if (
                        selectedTab == ReservationTab.PENDIENTES
                    ) {
                        "No tienes solicitudes pendientes"
                    } else {
                        when (selectedTab) {
                            ReservationTab.PROXIMAS ->
                                "No tienes reservas próximas"

                            ReservationTab.PASADAS ->
                                "No tienes reservas pasadas"

                            ReservationTab.CANCELADAS ->
                                "No tienes reservas canceladas"

                            else -> "No hay reservas"
                        }
                    },
                    modifier = Modifier.padding(top = 12.dp),
                    color = EspaciosPalette.Navy,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Tus solicitudes aparecerán aquí.",
                    modifier = Modifier.padding(top = 6.dp),
                    color = EspaciosPalette.Slate,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = visibleReservations,
                    key = { reservation -> reservation.id }
                ) { reservation ->
                    PendingReservationCard(reservation)
                }
            }
        }
    }
}

@Composable
private fun PendingReservationCard(
    reservation: ReservationEntity
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EspaciosPalette.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(11.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono ilustrativo del espacio deportivo.
                // No requiere imágenes ni datos adicionales.
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
                        text = reservation.space,
                        color = EspaciosPalette.Navy,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "${formatDate(reservation.date)} · ${reservation.timeSlot}",
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
                            text = "Pendiente",
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
                text = "Solicitud #${reservation.id} · En revisión",
                modifier = Modifier.padding(
                    start = 52.dp,
                    top = 8.dp
                ),
                color = EspaciosPalette.Slate,
                fontSize = 9.sp
            )
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
        ).format(parsed).replaceFirstChar {
            it.uppercase()
        }
    } catch (_: Exception) {
        date
    }
}



