package com.example.gaja_prueba1.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gaja_prueba1.ui.theme.GAJAPrueba1Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GajaComponentsTest {

    @get:Rule
    val composeRule = createComposeRule()

    // ---------- GajaButton ----------

    @Test
    fun boton_llama_a_onClick_al_tocarlo() {
        var clicks = 0
        composeRule.setContent {
            GAJAPrueba1Theme { GajaButton(text = "Entrar", onClick = { clicks++ }) }
        }

        composeRule.onNodeWithText("Entrar").performClick()

        assertEquals(1, clicks)
    }

    @Test
    fun boton_deshabilitado_no_se_puede_tocar() {
        composeRule.setContent {
            GAJAPrueba1Theme { GajaButton(text = "Entrar", onClick = {}, enabled = false) }
        }

        composeRule.onNodeWithText("Entrar").assertIsNotEnabled()
    }

    @Test
    fun boton_en_carga_oculta_el_texto_y_se_bloquea() {
        composeRule.setContent {
            GAJAPrueba1Theme { GajaButton(text = "Entrar", onClick = {}, loading = true) }
        }

        composeRule.onNodeWithText("Entrar").assertDoesNotExist()
        composeRule.onNode(hasClickAction()).assertIsNotEnabled()
    }

    // ---------- GajaTextField ----------

    @Test
    fun campo_de_texto_muestra_lo_que_se_escribe() {
        composeRule.setContent {
            GAJAPrueba1Theme {
                var texto by remember { mutableStateOf("") }
                GajaTextField(value = texto, onValueChange = { texto = it }, label = "Usuario")
            }
        }

        composeRule.onNode(hasSetTextAction()).performTextInput("galcala")

        composeRule.onNodeWithText("galcala").assertIsDisplayed()
    }

    @Test
    fun campo_de_texto_muestra_el_mensaje_de_error() {
        composeRule.setContent {
            GAJAPrueba1Theme {
                GajaTextField(
                    value = "hola",
                    onValueChange = {},
                    label = "Correo",
                    errorText = "Correo no válido"
                )
            }
        }

        composeRule.onNodeWithText("Correo no válido").assertIsDisplayed()
    }

    @Test
    fun campo_de_contrasena_alterna_mostrar_y_ocultar() {
        composeRule.setContent {
            GAJAPrueba1Theme {
                GajaTextField(
                    value = "12345",
                    onValueChange = {},
                    label = "Contraseña",
                    isPassword = true
                )
            }
        }

        composeRule.onNodeWithContentDescription("Mostrar contraseña").performClick()
        composeRule.onNodeWithContentDescription("Ocultar contraseña").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("Ocultar contraseña").performClick()
        composeRule.onNodeWithContentDescription("Mostrar contraseña").assertIsDisplayed()
    }

    // ---------- GajaSelector ----------

    private val roles = listOf("Cadete", "Oficial", "Oficial de semana", "Institución externa")

    @Test
    fun selector_marca_solo_la_opcion_elegida() {
        composeRule.setContent {
            GAJAPrueba1Theme {
                var elegido by remember { mutableStateOf(0) }
                GajaSelector(options = roles, selectedIndex = elegido, onSelect = { elegido = it })
            }
        }

        composeRule.onNodeWithText("Cadete").assertIsSelected()
        composeRule.onNodeWithText("Oficial").assertIsNotSelected()

        composeRule.onNodeWithText("Oficial").performClick()

        composeRule.onNodeWithText("Oficial").assertIsSelected()
        composeRule.onNodeWithText("Cadete").assertIsNotSelected()
    }

    @Test
    fun selector_informa_el_indice_de_la_opcion_tocada() {
        var recibido = -1
        composeRule.setContent {
            GAJAPrueba1Theme {
                GajaSelector(options = roles, selectedIndex = 0, onSelect = { recibido = it })
            }
        }

        composeRule.onNodeWithText("Institución externa").performClick()

        assertEquals(3, recibido)
    }

    // ---------- Chips ----------

    @Test
    fun chip_de_estado_muestra_su_texto() {
        composeRule.setContent {
            GAJAPrueba1Theme { GajaStatusChip(text = "Confirmada", tone = GajaChipTone.Success) }
        }

        composeRule.onNodeWithText("Confirmada").assertIsDisplayed()
    }

    @Test
    fun chip_de_filtro_cambia_de_estado_al_tocarlo() {
        composeRule.setContent {
            GAJAPrueba1Theme {
                var marcado by remember { mutableStateOf(false) }
                GajaFilterChip(text = "Fútbol", selected = marcado, onClick = { marcado = !marcado })
            }
        }

        composeRule.onNodeWithText("Fútbol").assertIsNotSelected()
        composeRule.onNodeWithText("Fútbol").performClick()
        composeRule.onNodeWithText("Fútbol").assertIsSelected()
    }

    // ---------- Tarjetas ----------

    @Test
    fun tarjeta_clicable_llama_a_onClick() {
        var tocada = false
        composeRule.setContent {
            GAJAPrueba1Theme {
                GajaCard(onClick = { tocada = true }) { Text("Cancha de Fútbol A") }
            }
        }

        composeRule.onNodeWithText("Cancha de Fútbol A").performClick()

        assertTrue(tocada)
    }

    @Test
    fun tarjeta_de_contador_muestra_numero_y_etiqueta() {
        composeRule.setContent {
            GAJAPrueba1Theme { GajaStatCard(value = "3", label = "Pendientes") }
        }

        composeRule.onNodeWithText("3").assertIsDisplayed()
        composeRule.onNodeWithText("Pendientes").assertIsDisplayed()
    }
}