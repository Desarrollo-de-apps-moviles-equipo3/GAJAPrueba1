package com.example.gaja_prueba1.ui.screen

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.net.Uri
import android.app.Activity
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.data.remote.AlertResponse
import com.example.gaja_prueba1.data.remote.ReservationResponse
import com.example.gaja_prueba1.model.formatDistance
import com.example.gaja_prueba1.ui.viewmodels.HomeUiState
import com.example.gaja_prueba1.ui.viewmodels.HomeViewModel
import com.example.gaja_prueba1.ui.viewmodels.NearbyState
import com.example.gaja_prueba1.ui.viewmodels.WeatherStatus
import com.example.gaja_prueba1.util.NearbyLocationProvider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.gaja_prueba1.R
import com.example.gaja_prueba1.ui.components.GajaCard
import com.example.gaja_prueba1.ui.components.GajaChipTone
import com.example.gaja_prueba1.ui.components.GajaStatCard
import com.example.gaja_prueba1.ui.components.GajaStatusChip
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import androidx.compose.material3.LocalContentColor
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    userName: String,
    userEmail: String,
    onViewReservations: () -> Unit = {},
    onBrowseCourts: () -> Unit = {},
    onReserveClick: (Space) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val locationPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    LaunchedEffect(Unit) { viewModel.refreshSpaces() }
    LaunchedEffect(userEmail) { viewModel.loadPersonalData(userEmail) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.any { it }) {
            viewModel.onNearbyRequested()
        } else {
            val activity = context.findHostActivity()
            val canAskAgain = activity != null &&
                    locationPermissions.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
            viewModel.onPermissionDenied(permanently = !canAskAgain)
        }
    }

    HomeContent(
        state = state,
        userName = userName,
        reservations = state.reservations,
        alerts = state.alerts,
        onViewReservations = onViewReservations,
        onBrowseCourts = onBrowseCourts,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::loadSpaces,
        onReserveClick = onReserveClick,
        onNearbyClick = {
            if (NearbyLocationProvider.hasPermission(context)) viewModel.onNearbyRequested()
            else permissionLauncher.launch(locationPermissions)
        },
        onNearbyClear = viewModel::onNearbyCleared,
        onOpenSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        },
        onWeatherRetry = { viewModel.loadWeather() }
    )
}

private tailrec fun Context.findHostActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findHostActivity()
    else -> null
}

@Composable
fun HomeContent(
    state: HomeUiState,
    userName: String,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onReserveClick: (Space) -> Unit,
    onNearbyClick: () -> Unit = {},
    onNearbyClear: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onWeatherRetry: () -> Unit = {},
    reservations: List<ReservationResponse> = emptyList(),
    alerts: List<AlertResponse> = emptyList(),
    onViewReservations: () -> Unit = {},
    onBrowseCourts: () -> Unit = {}
) {
    val scheme = MaterialTheme.colorScheme
    val gradientTop = lerp(scheme.primary, scheme.secondary, 0.2f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradientTop, scheme.primary)))
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().height(245.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.escuela_militar_hero),
                contentDescription = "Recintos deportivos de la Escuela Militar",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(
                        0f to Color(0xCC0E2638),
                        0.42f to Color(0x44102536),
                        1f to Color(0xF20E2638)
                    )
                )
            )
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                HomeHeader(unreadAlertCount = alerts.count { !it.leida })
                Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)) {
                    Text(
                        text = "Hola, $userName",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Agenda tu cancha y disfruta tu deporte favorito.",
                        color = Color.White.copy(alpha = 0.84f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Button(
                        onClick = onBrowseCourts,
                        modifier = Modifier.padding(top = 14.dp).height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = scheme.tertiary,
                            contentColor = scheme.onTertiary
                        )
                    ) {
                        Text("Reservar", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CounterCard(
                value = if (state.isLoading) null else state.availableSpaces,
                label = "Áreas disponibles",
                highlight = true,
                modifier = Modifier.weight(1f)
            )
            CounterCard(
                value = if (state.isLoading) null else state.upcomingReservations,
                label = "Próximas reservas",
                modifier = Modifier.weight(1f)
            )
            CounterCard(
                value = if (state.isLoading) null else state.pastReservations,
                label = "Historial de reservas",
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
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = scheme.surfaceContainerLowest,
                unfocusedContainerColor = scheme.surfaceContainerLowest,
                focusedBorderColor = scheme.tertiary,
                unfocusedBorderColor = scheme.outlineVariant
            )
        )

        Spacer(Modifier.height(12.dp))

        NearbyControls(
            nearby = state.nearby,
            onNearbyClick = onNearbyClick,
            onNearbyClear = onNearbyClear,
            onOpenSettings = onOpenSettings
        )

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Canchas destacadas",
            color = scheme.onPrimary,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        if (state.weatherStatus == WeatherStatus.Error && !state.isLoading && state.errorMessage == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sin conexión: no pudimos cargar el clima.",
                    color = scheme.onPrimary.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onWeatherRetry) {
                    Text("Reintentar", color = scheme.onPrimary)
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = scheme.onPrimary)
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
                    items(state.visibleSpaces.take(3), key = { it.id }) { space ->
                        SpaceCard(
                            space = space,
                            distanceText = state.distanceTo(space)?.let(::formatDistance),
                            weatherText = state.weather[space.id]?.summary,
                            onReserveClick = { onReserveClick(space) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        WeekReservationsSection(
            reservations = reservations,
            onViewReservations = onViewReservations
        )

        Spacer(Modifier.height(22.dp))

        HomeAlertsSection(alerts = alerts)

        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun WeekReservationsSection(
    reservations: List<ReservationResponse>,
    onViewReservations: () -> Unit
) {
    val weekDays = remember { homeWeekDays() }
    var selectedDate by rememberSaveable {
        mutableStateOf(homeIsoDate(System.currentTimeMillis()))
    }
    val selectedReservations = reservations
        .filter {
            it.fecha == selectedDate &&
                (it.estado.equals("PENDIENTE", true) || it.estado.equals("APROBADA", true))
        }
        .sortedBy { it.horario }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Mi semana",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            TextButton(onClick = onViewReservations) {
                Text("Calendario  ›", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
            }
        }

        GajaCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekDays.forEach { day ->
                    val iso = homeIsoDate(day)
                    val selected = iso == selectedDate
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedDate = iso }
                            .padding(vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = SimpleDateFormat("EEEEE", Locale("es", "CL")).format(Date(day)).uppercase(),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Spacer(Modifier.height(5.dp))
                        Text(
                            text = SimpleDateFormat("d", Locale.ROOT).format(Date(day)),
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (selectedReservations.isEmpty()) {
                Text(
                    text = "No tienes reservas para este día.",
                    modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Column(
                    modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    selectedReservations.take(3).forEach { reservation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f), RoundedCornerShape(9.dp))
                                .padding(horizontal = 10.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(width = 3.dp, height = 32.dp)
                                    .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(3.dp))
                            )
                            Column(modifier = Modifier.weight(1f).padding(start = 9.dp)) {
                                Text(reservation.espacio, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(reservation.horario, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            }
                            Text(
                                reservation.estado.lowercase().replaceFirstChar { it.titlecase(Locale("es", "CL")) },
                                color = MaterialTheme.colorScheme.tertiary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeAlertsSection(alerts: List<AlertResponse>) {
    var showAll by remember { mutableStateOf(false) }
    val visibleAlerts = if (showAll) alerts else alerts.take(2)
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Alertas",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            if (alerts.size > 2) {
                TextButton(onClick = { showAll = !showAll }) {
                    Text(if (showAll) "Ver menos" else "Ver todas", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }
        }
        Spacer(Modifier.height(10.dp))

        if (alerts.isEmpty()) {
            GajaCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "No tienes alertas nuevas.",
                    modifier = Modifier.padding(14.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visibleAlerts.forEach { alert ->
                    GajaCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(34.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(17.dp))
                            }
                            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                                Text(alert.mensaje, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(homeAlertTime(alert.creadaEn), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                            }
                            if (!alert.leida) {
                                Box(Modifier.size(7.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun homeWeekDays(): List<Long> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return (0..6).map { offset ->
        (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, offset) }.timeInMillis
    }
}

private fun homeIsoDate(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date(millis))

private fun homeAlertTime(value: String): String =
    value.substringAfter('T', value).take(5).let { if (it.isBlank()) "Reciente" else it }

@Composable
private fun HomeHeader(unreadAlertCount: Int) {
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(38.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFE0B13C),
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "ESCUELA MILITAR",
                color = onPrimary,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .border(1.dp, onPrimary.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("?", color = onPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(modifier = Modifier.size(28.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notificaciones",
                    tint = onPrimary,
                    modifier = Modifier.size(22.dp)
                )
                if (unreadAlertCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(7.dp)
                            .background(MaterialTheme.colorScheme.tertiary, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun CounterCard(
    value: Int?,
    label: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false
) {
    val animated by animateIntAsState(
        targetValue = value ?: 0,
        animationSpec = tween(durationMillis = 700),
        label = "counter"
    )
    GajaStatCard(
        value = if (value == null) "–" else animated.toString(),
        label = label,
        modifier = modifier,
        highlight = highlight
    )
}

@Composable
private fun NearbyControls(
    nearby: NearbyState,
    onNearbyClick: () -> Unit,
    onNearbyClear: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        if (nearby == NearbyState.Active) {
            OutlinedButton(
                onClick = onNearbyClear,
                shape = CircleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = scheme.onPrimary)
            ) {
                Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Quitar orden por cercanía", style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Button(
                onClick = onNearbyClick,
                enabled = nearby != NearbyState.Locating,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.tertiary,
                    contentColor = scheme.onTertiary
                )
            ) {
                if (nearby == NearbyState.Locating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = LocalContentColor.current,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (nearby == NearbyState.Locating) "Buscando…" else "Cerca de mí",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        val message = nearbyMessage(nearby)
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = message.orEmpty(),
                    color = if (nearby == NearbyState.Active) scheme.onPrimary.copy(alpha = 0.85f)
                    else scheme.errorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (nearby == NearbyState.PermissionBlocked) {
                    TextButton(onClick = onOpenSettings) {
                        Text(
                            text = "Abrir ajustes",
                            color = scheme.tertiary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun nearbyMessage(nearby: NearbyState): String? = when (nearby) {
    NearbyState.Idle, NearbyState.Locating -> null
    NearbyState.Active -> "Mostrando primero los espacios más cercanos a ti."
    NearbyState.PermissionDenied ->
        "Sin el permiso de ubicación no podemos ordenar por cercanía. Intentalo de nuevo."
    NearbyState.PermissionBlocked ->
        "El permiso de ubicación está bloqueado. Actívalo en Ajustes para usar “Cerca de mí”."
    NearbyState.LocationOff -> "La ubicación del teléfono está apagada. Actívala e inténtalo de nuevo."
    NearbyState.Unavailable -> "No pudimos obtener tu ubicación. Inténtalo de nuevo."
}

@Composable
private fun SpaceCard(
    space: Space,
    onReserveClick: () -> Unit,
    modifier: Modifier = Modifier,
    distanceText: String? = null,
    weatherText: String? = null
) {
    val scheme = MaterialTheme.colorScheme
    val baseColor = colorFromHex(space.colorHex, fallback = scheme.secondary)

    GajaCard(
        modifier = modifier
            .width(200.dp)
            .animateContentSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(106.dp)
                .background(Brush.verticalGradient(listOf(baseColor, baseColor.copy(alpha = 0.65f))))
        ) {
            GajaStatusChip(
                text = if (space.available) "Disponible" else "No disponible",
                tone = if (space.available) GajaChipTone.Success else GajaChipTone.Danger,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            )
            GajaStatusChip(
                text = space.category,
                tone = GajaChipTone.Info,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
            )
        }
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = space.name,
                color = scheme.onSurface,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = space.description,
                color = scheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (weatherText != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = weatherText,
                    color = scheme.secondary,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        append("Hasta ${space.capacity} personas")
                        if (distanceText != null) append(" · $distanceText")
                    },
                    color = scheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
                Button(
                    onClick = onReserveClick,
                    enabled = space.available,
                    modifier = Modifier.height(36.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = "Reservar",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBlock(text: String, actionLabel: String? = null, onAction: () -> Unit = {}) {
    val scheme = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            color = scheme.onPrimary.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null) {
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = scheme.tertiary,
                    contentColor = scheme.onTertiary
                )
            ) {
                Text(actionLabel)
            }
        }
    }
}

private fun colorFromHex(hex: String, fallback: Color): Color =
    try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: IllegalArgumentException) {
        fallback
    }

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
    GAJAPrueba1Theme {
        HomeContent(
            state = HomeUiState(
                isLoading = false, allSpaces = previewSpaces,
                upcomingReservations = 2, pastReservations = 1
            ),
            userName = "Gabriel", onQueryChange = {}, onRetry = {}, onReserveClick = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomeNearbyBlockedPreview() {
    GAJAPrueba1Theme {
        HomeContent(
            HomeUiState(isLoading = false, allSpaces = previewSpaces, nearby = NearbyState.PermissionBlocked),
            "Gabriel", {}, {}, {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomeLoadingPreview() {
    GAJAPrueba1Theme {
        HomeContent(HomeUiState(isLoading = true), "Gabriel", {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomeErrorPreview() {
    GAJAPrueba1Theme {
        HomeContent(
            HomeUiState(isLoading = false, errorMessage = "No pudimos cargar los espacios. Revisa tu conexión e inténtalo de nuevo."),
            "Gabriel", {}, {}, {}
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 800)
@Composable
private fun HomeEmptySearchPreview() {
    GAJAPrueba1Theme {
        HomeContent(
            HomeUiState(isLoading = false, allSpaces = previewSpaces, query = "piscina"),
            "Gabriel", {}, {}, {}
        )
    }
}
