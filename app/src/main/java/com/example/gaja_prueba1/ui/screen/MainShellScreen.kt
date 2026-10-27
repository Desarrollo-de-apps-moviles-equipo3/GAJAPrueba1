package com.example.gaja_prueba1.ui.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.gaja_prueba1.model.Space
import com.example.gaja_prueba1.ui.theme.EspaciosPalette

enum class BottomTab(val label: String, val icon: ImageVector) {
    Inicio("Inicio", Icons.Filled.Home),
    Calendario("Calendario", Icons.Filled.DateRange),
    Reservas("Reservas", Icons.Filled.CheckCircle),
    Alertas("Alertas", Icons.Filled.Notifications),
    Perfil("Perfil", Icons.Filled.Person)
}

@Composable
fun MainShellScreen(
    userName: String,
    onReserveClick: (Space) -> Unit = {},
    otherTabs: @Composable (BottomTab) -> Unit = { tab -> ComingSoonScreen(sectionName = tab.label) }
) {
    val tabs = BottomTab.entries
    var selectedIndex by rememberSaveable { mutableStateOf(0) }
    val selected = tabs[selectedIndex]

    // Inicio tiene fondo oscuro (íconos claros en la barra de estado); las demás son claras.
    StatusBarIcons(lightIcons = selected == BottomTab.Inicio)

    Scaffold(
        containerColor = EspaciosPalette.Cream,
        bottomBar = {
            AppBottomBar(
                tabs = tabs,
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (selected == BottomTab.Inicio) {
                HomeScreen(userName = userName, onReserveClick = onReserveClick)
            } else {
                otherTabs(selected)
            }
        }
    }
}

@Composable
fun AppBottomBar(
    tabs: List<BottomTab>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    NavigationBar(
        containerColor = EspaciosPalette.Cream,
        tonalElevation = 0.dp
    ) {
        tabs.forEachIndexed { index, tab ->
            NavigationBarItem(
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(text = tab.label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = EspaciosPalette.Navy,
                    selectedTextColor = EspaciosPalette.Navy,
                    unselectedIconColor = EspaciosPalette.Slate,
                    unselectedTextColor = EspaciosPalette.Slate,
                    indicatorColor = EspaciosPalette.Beige
                )
            )
        }
    }
}

@Composable
private fun StatusBarIcons(lightIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(lightIcons) {
        val window = view.context.findActivity()?.window
        val controller = if (window != null) WindowCompat.getInsetsController(window, view) else null
        val previous = controller?.isAppearanceLightStatusBars
        controller?.isAppearanceLightStatusBars = !lightIcons
        onDispose {
            if (controller != null && previous != null) {
                controller.isAppearanceLightStatusBars = previous
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
