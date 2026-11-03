package com.example.gaja_prueba1.ui.screen

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.util.ProfilePhotoStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ProfileScreen(
    fullName: String,
    email: String,
    onMyReservations: () -> Unit,
    onPersonalData: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("user_profile", Context.MODE_PRIVATE)
    }
    var photoUri by rememberSaveable {
        mutableStateOf(preferences.getString("photo_uri", null))
    }
    val scope = rememberCoroutineScope()
    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { selectedUri ->
        if (selectedUri != null) {
            scope.launch {
                val savedUri = withContext(Dispatchers.IO) {
                    ProfilePhotoStorage.copyToInternal(context, selectedUri)
                }
                if (savedUri != null) {
                    photoUri = savedUri
                    preferences.edit().putString("photo_uri", savedUri).apply()
                }
            }
        }
    }

    val avatar by produceState<ImageBitmap?>(null, photoUri) {
        value = withContext(Dispatchers.IO) {
            val path = photoUri?.let { Uri.parse(it).path } ?: return@withContext null
            BitmapFactory.decodeFile(path)?.asImageBitmap()
        }
    }
    val initials = fullName.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        .take(2).joinToString("") { it.first().uppercase() }

    Column(
        modifier = Modifier.fillMaxSize().background(EspaciosPalette.Cream).padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("‹", color = EspaciosPalette.Steel, fontSize = 24.sp)
            Text("Perfil", modifier = Modifier.weight(1f).padding(start = 12.dp), color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.Settings, contentDescription = "Configuración", tint = EspaciosPalette.Steel)
        }

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier.size(82.dp).clip(CircleShape).background(EspaciosPalette.Slate),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatar != null) {
                        Image(avatar!!, contentDescription = "Foto de perfil", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(initials.ifBlank { "?" }, color = EspaciosPalette.White, fontSize = 22.sp)
                    }
                }
                IconButton(
                    onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.size(30.dp).clip(CircleShape).background(EspaciosPalette.White)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = "Cambiar foto de perfil", tint = EspaciosPalette.Navy, modifier = Modifier.size(16.dp))
                }
            }
            Text(fullName, modifier = Modifier.padding(top = 10.dp), color = EspaciosPalette.Navy, fontWeight = FontWeight.SemiBold)
            Text(email, modifier = Modifier.padding(top = 3.dp), color = EspaciosPalette.Slate, fontSize = 12.sp)
        }

        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(EspaciosPalette.White)
        ) {
            ProfileOption("Mis reservas", Icons.Filled.CalendarMonth, onMyReservations)
            ProfileOption("Datos personales", Icons.Filled.Person, onPersonalData)
            ProfileOption("Cambiar contraseña", Icons.Filled.Lock, onChangePassword)
            ProfileOption("Cerrar sesión", Icons.Filled.Logout, onLogout)
        }
    }
}

@Composable
private fun ProfileOption(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = EspaciosPalette.Navy, modifier = Modifier.size(18.dp))
        Text(label, modifier = Modifier.weight(1f).padding(start = 10.dp), color = EspaciosPalette.Slate, fontSize = 13.sp)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = EspaciosPalette.Slate, modifier = Modifier.size(18.dp))
    }
    HorizontalDivider(color = EspaciosPalette.Beige.copy(alpha = 0.65f), thickness = 0.5.dp)
}