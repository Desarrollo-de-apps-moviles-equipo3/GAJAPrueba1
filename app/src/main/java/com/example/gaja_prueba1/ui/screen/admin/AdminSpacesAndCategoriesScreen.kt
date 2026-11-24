package com.example.gaja_prueba1.ui.screen.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.gaja_prueba1.ui.components.GajaFilterChip

/** Pantalla del menú "Espacios y categorías": dos pestañas arriba que cambian entre ambas listas. */
@Composable
fun AdminSpacesAndCategoriesScreen(modifier: Modifier = Modifier) {
    var showCategories by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GajaFilterChip(text = "Espacios", selected = !showCategories, onClick = { showCategories = false })
            GajaFilterChip(text = "Categorías", selected = showCategories, onClick = { showCategories = true })
        }
        if (showCategories) {
            AdminCategoriesScreen(modifier = Modifier.weight(1f))
        } else {
            AdminSpacesScreen(modifier = Modifier.weight(1f))
        }
    }
}
