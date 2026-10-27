package com.example.gaja_prueba1.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.viewmodels.HomeUiState
import com.example.gaja_prueba1.ui.viewmodels.HomeViewModel

@Composable
fun HomeScreen(
    userName: String,
    onReserveClick: (Space) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    HomeContent(
        state = state,
        userName = userName,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::loadSpaces,
        onReserveClick = onReserveClick
    )
}

@Composable
fun HomeContent(
    state: HomeUiState,
    userName: String,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onReserveClick: (Space) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(EspaciosPalette.NavyTop, EspaciosPalette.Navy)))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        HomeHeader()

        Text(
            text = "Hola, $userName",
            color = EspaciosPalette.White,
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Text(
            text = "Agenda tu cancha",
            color = EspaciosPalette.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            lineHeight = 21.sp,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 6.dp)
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val dash = "–"
            CounterCard(
                value = if (state.isLoading) dash else state.availableSpaces.toString(),
                label = "Áreas disponibles",
                highlight = true,
                modifier = Modifier.weight(1f)
            )
            CounterCard(
                value = if (state.isLoading) dash else state.upcomingReservations.toString(),
                label = "Próximas reservas",
                modifier = Modifier.weight(1f)
            )
            CounterCard(
                value = if (state.isLoading) dash else state.pastReservations.toString(),
                label = "Reservas pasadas",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            placeholder = { Text("Buscar cancha o deporte") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = EspaciosPalette.White,
                unfocusedContainerColor = EspaciosPalette.White,
                focusedBorderColor = EspaciosPalette.Orange,
                unfocusedBorderColor = EspaciosPalette.Beige,
                focusedTextColor = EspaciosPalette.Navy,
                unfocusedTextColor = EspaciosPalette.Navy,
                cursorColor = EspaciosPalette.Navy,
                focusedPlaceholderColor = EspaciosPalette.Slate,
                unfocusedPlaceholderColor = EspaciosPalette.Slate,
                focusedLeadingIconColor = EspaciosPalette.Slate,
                unfocusedLeadingIconColor = EspaciosPalette.Slate
            )
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Espacios",
            color = EspaciosPalette.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EspaciosPalette.White)
                }
            }

            state.errorMessage != null -> {
                MessageBlock(
                    text = state.errorMessage,
                    actionLabel = "Reintentar",
                    onAction = onRetry
                )
            }

            state.visibleSpaces.isEmpty() -> {
                MessageBlock(
                    text = if (state.query.isBlank()) "Aún no hay espacios registrados."
                    else "No encontramos espacios para “${state.query.trim()}”."
                )
            }

            else -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(state.visibleSpaces, key = { it.id }) { space ->
                        SpaceCard(space = space, onReserveClick = { onReserveClick(space) })
                    }
                }
            }
        }

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun HomeHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, EspaciosPalette.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFE0B13C),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "ESCUELA MILITAR",
                color = EspaciosPalette.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
        }
        Icon(
            imageVector = Icons.Filled.Notifications,
            contentDescription = "Notificaciones",
            tint = EspaciosPalette.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun CounterCard(
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    Column(
        modifier = modifier
            .background(EspaciosPalette.SkyCard, RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            color = if (highlight) EspaciosPalette.Green else EspaciosPalette.Navy,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (highlight) EspaciosPalette.Green else EspaciosPalette.Navy,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SpaceCard(space: Space, onReserveClick: () -> Unit) {
    val baseColor = colorFromHex(space.colorHex)
    Card(
        modifier = Modifier.width(260.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EspaciosPalette.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(Brush.verticalGradient(listOf(baseColor, baseColor.copy(alpha = 0.65f))))
        ) {
            StatusChip(
                text = if (space.available) "Disponible" else "No disponible",
                background = if (space.available) EspaciosPalette.GreenSoft else EspaciosPalette.DangerSoft,
                content = if (space.available) EspaciosPalette.Green else EspaciosPalette.Danger,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            )
            StatusChip(
                text = space.category,
                background = EspaciosPalette.SkyCard,
                content = EspaciosPalette.Navy,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = space.name,
                color = EspaciosPalette.Navy,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = space.description,
                color = EspaciosPalette.Slate,
                fontSize = 12.sp,
                lineHeight = 17.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Hasta ${space.capacity} personas",
                    color = EspaciosPalette.Slate,
                    fontSize = 12.sp
                )
                Button(
                    onClick = onReserveClick,
                    enabled = space.available,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EspaciosPalette.Navy,
                        contentColor = EspaciosPalette.White
                    )
                ) {
                    Text("Reservar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatusChip(text: String, background: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        color = content,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        modifier = modifier
            .background(background, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun MessageBlock(text: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = EspaciosPalette.White.copy(alpha = 0.85f),
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EspaciosPalette.Orange,
                    contentColor = EspaciosPalette.White
                )
            ) {
                Text(actionLabel)
            }
        }
    }
}

/** Convierte "#RRGGBB" en Color; si el texto viene mal en el JSON usa el azul acero. */
private fun colorFromHex(hex: String): Color =
    try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        EspaciosPalette.Steel
    }

// ---------- Previews (se ven en Android Studio sin ejecutar la app) ----------

private val previewSpaces = listOf(
    Space("multiusos-a", "Cancha Multiusos A", "Deportes",
        "Cancha multiusos para básquetbol, voleibol y actividades físicas grupales en un ambiente moderno.",
        30, true, "#B7791F", listOf("Básquetbol", "Voleibol")),
    Space("futbol-b", "Cancha de Fútbol B", "Deportes",
        "Cancha de fútbol 7 para entrenamientos y partidos entre compañías.",
        14, false, "#2F6B4F", listOf("Fútbol")),
    Space("tenis-c", "Cancha de Tenis C", "Deportes",
        "Cancha de tenis de cemento, ideal para clases y práctica libre.",
        4, true, "#3B6989", listOf("Tenis"))
)

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomePreview() {
    HomeContent(
        state = HomeUiState(
            isLoading = false, allSpaces = previewSpaces,
            upcomingReservations = 2, pastReservations = 1
        ),
        userName = "Gabriel", onQueryChange = {}, onRetry = {}, onReserveClick = {}
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomeLoadingPreview() {
    HomeContent(HomeUiState(isLoading = true), "Gabriel", {}, {}, {})
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomeErrorPreview() {
    HomeContent(
        HomeUiState(isLoading = false, errorMessage = "No pudimos cargar los espacios. Revisa tu conexión e inténtalo de nuevo."),
        "Gabriel", {}, {}, {}
    )
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomeEmptySearchPreview() {
    HomeContent(
        HomeUiState(isLoading = false, allSpaces = previewSpaces, query = "piscina"),
        "Gabriel", {}, {}, {}
    )
}
