package com.example.gaja_prueba1.ui.screen.admin

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.model.EstadoPerfil
import com.example.gaja_prueba1.model.RemoteUsuariosRepository
import com.example.gaja_prueba1.model.fechaAlta
import com.example.gaja_prueba1.ui.components.GajaCard
import kotlinx.coroutines.CancellationException

@Composable
fun AdminNotificationsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val repository = remember { RemoteUsuariosRepository() }
    var attempt by remember { mutableIntStateOf(0) }
    val notifications by loadAdminList(attempt) {
        repository.getAll()
            .filter { it.estado == EstadoPerfil.Pendiente }
            .sortedByDescending { it.creadoEn }
    }
    AdminActivityList("Notificaciones", onBack, modifier) {
        adminListItems(
            result = notifications,
            error = "No pudimos cargar las notificaciones.",
            empty = "No hay solicitudes de perfil pendientes.",
            onRetry = { attempt++ },
            key = { it.id }
        ) { profile ->
            AdminActivityCard(
                "Perfil por revisar",
                listOf(profile.nombre, profile.rol, profile.fechaAlta).filter { it.isNotBlank() }.joinToString(" · ")
            )
        }
    }
}

@Composable
fun AdminSessionHistoryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val repository = remember { RemoteUsuariosRepository() }
    var userId by rememberSaveable { mutableStateOf<Long?>(null) }
    var attempt by remember { mutableIntStateOf(0) }
    val users by loadAdminList(attempt) { repository.getAll() }
    val selected = users?.getOrNull()?.firstOrNull { it.id == userId }
    val back = if (userId == null) onBack else ({ userId = null })
    BackHandler(enabled = userId != null, onBack = back)

    if (selected == null) {
        AdminActivityList("Registro de sesiones", back, modifier) {
            adminListItems(
                result = users,
                error = "No pudimos cargar los perfiles.",
                empty = "No hay perfiles para consultar.",
                onRetry = { attempt++ },
                key = { it.id }
            ) { user ->
                AdminActivityCard(user.nombre, user.email, onClick = { userId = user.id })
            }
        }
    } else {
        val sessions by loadAdminList(attempt, selected.id) { repository.sessions(selected.id) }
        AdminActivityList("Registro de sesiones", back, modifier) {
            item { Text(selected.nombre, style = MaterialTheme.typography.titleMedium) }
            adminListItems(
                result = sessions,
                error = "No pudimos cargar las sesiones.",
                empty = "Este perfil aún no registra inicios de sesión.",
                onRetry = { attempt++ },
                key = { it.id }
            ) { session ->
                val date = session.fecha.substringBefore('T')
                val time = session.fecha.substringAfter('T', "").take(8)
                AdminActivityCard("Inicio de sesión", "$date · $time UTC")
            }
        }
    }
}

@Composable
private fun <T> loadAdminList(
    attempt: Int,
    subject: Long? = null,
    load: suspend () -> List<T>
) = produceState<Result<List<T>>?>(null, attempt, subject) {
    value = null
    value = try {
        Result.success(load())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@Composable
private fun AdminActivityList(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier,
    content: LazyListScope.() -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { TextButton(onClick = onBack) { Text("‹ Volver") } }
        item { Text(title, style = MaterialTheme.typography.headlineSmall) }
        content()
    }
}

private fun <T> LazyListScope.adminListItems(
    result: Result<List<T>>?,
    error: String,
    empty: String,
    onRetry: () -> Unit,
    key: (T) -> Any,
    row: @Composable (T) -> Unit
) {
    if (result == null) {
        item {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    result.fold(
        onSuccess = { entries ->
            if (entries.isEmpty()) item { Text(empty) }
            else items(entries, key = key) { row(it) }
        },
        onFailure = {
            item { Text(error, color = MaterialTheme.colorScheme.error) }
            item { TextButton(onClick = onRetry) { Text("Reintentar") } }
        }
    )
}

@Composable
private fun AdminActivityCard(title: String, detail: String, onClick: (() -> Unit)? = null) {
    GajaCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(detail, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
