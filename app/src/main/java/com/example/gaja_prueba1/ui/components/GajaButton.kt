package com.example.gaja_prueba1.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gaja_prueba1.ui.theme.EspaciosPalette
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import com.example.gaja_prueba1.ui.theme.InterFamily

@Composable
fun GajaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button (
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EspaciosPalette.Navy,
            contentColor = EspaciosPalette.White,
            disabledContainerColor = EspaciosPalette.Navy.copy(alpha = 0.55f),
            disabledContentColor = EspaciosPalette.White
        )
    ){
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = EspaciosPalette.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(text, fontFamily = InterFamily, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GajaButtonPreview(){
    GAJAPrueba1Theme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)){
            GajaButton("Iniciar Sesión", onClick = {})
            GajaButton("Enviando", onClick = {}, loading = true)
            GajaButton("No Disponible", onClick  = {}, enabled = false)
        }
    }
}