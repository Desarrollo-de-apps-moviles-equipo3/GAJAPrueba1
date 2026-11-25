package com.example.gaja_prueba1.ui.screen.profile

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.gaja_prueba1.ui.screen.ProfileScreen
import com.example.gaja_prueba1.ui.viewmodels.MyProfileViewModel
import com.example.gaja_prueba1.ui.viewmodels.SupportViewModel

/** Las pantallas a las que se llega desde la pestaña Perfil. */
enum class ProfileDestination { Main, PersonalData, ChangePassword, Notifications, Help, NewRequest }

/**
 * Pestaña Perfil completa: el menú y las pantallas que cuelgan de él.
 * El botón "atrás" del teléfono vuelve un paso: de "Nueva solicitud" a "Ayuda" y de ahí al menú.
 */
@Composable
fun ProfileFlow(
    fullName: String,
    email: String,
    onMyReservations: () -> Unit,
    onLogout: () -> Unit,
    profileViewModel: MyProfileViewModel = viewModel(),
    supportViewModel: SupportViewModel = viewModel()
) {
    var destination by rememberSaveable { mutableStateOf(ProfileDestination.Main) }

    BackHandler(enabled = destination != ProfileDestination.Main) {
        destination = when (destination) {
            ProfileDestination.NewRequest -> ProfileDestination.Help
            else -> ProfileDestination.Main
        }
    }

    // Cada vez que se abre una pantalla se pide lo último al servicio.
    LaunchedEffect(destination) {
        when (destination) {
            ProfileDestination.PersonalData,
            ProfileDestination.ChangePassword,
            ProfileDestination.Notifications -> profileViewModel.load(email)

            ProfileDestination.Help -> supportViewModel.load(email)
            else -> Unit
        }
    }

    val profileState by profileViewModel.uiState.collectAsState()
    val supportState by supportViewModel.uiState.collectAsState()

    // Al enviarse una solicitud, volvemos a "Ayuda y soporte" para ver el aviso y la nueva solicitud en la lista.
    LaunchedEffect(supportState.sent) {
        if (supportState.sent) {
            destination = ProfileDestination.Help
            supportViewModel.onSentHandled()
        }
    }

    val backToMenu = {
        profileViewModel.onDiscardPersonal()
        profileViewModel.onClearPassword()
        destination = ProfileDestination.Main
    }
    val retryProfile = { profileViewModel.load(email) }

    when (destination) {
        ProfileDestination.Main -> ProfileScreen(
            fullName = fullName,
            email = email,
            onMyReservations = onMyReservations,
            onPersonalData = { destination = ProfileDestination.PersonalData },
            onChangePassword = { destination = ProfileDestination.ChangePassword },
            onNotifications = { destination = ProfileDestination.Notifications },
            onHelp = { destination = ProfileDestination.Help },
            onLogout = onLogout
        )

        ProfileDestination.PersonalData -> PersonalDataScreen(
            state = profileState,
            actions = PersonalDataActions(
                onBack = backToMenu,
                onRetry = retryProfile,
                onMessageShown = profileViewModel::onMessageShown,
                onNameChange = profileViewModel::onNameChange,
                onPhoneChange = profileViewModel::onPhoneChange,
                onInstitutionChange = profileViewModel::onInstitutionChange,
                onSave = profileViewModel::onSavePersonal
            )
        )

        ProfileDestination.ChangePassword -> ChangePasswordScreen(
            state = profileState,
            actions = ChangePasswordActions(
                onBack = backToMenu,
                onRetry = retryProfile,
                onMessageShown = profileViewModel::onMessageShown,
                onCurrentChange = profileViewModel::onCurrentPasswordChange,
                onNewChange = profileViewModel::onNewPasswordChange,
                onConfirmChange = profileViewModel::onConfirmPasswordChange,
                onSave = profileViewModel::onSavePassword
            )
        )

        ProfileDestination.Notifications -> NotificationsScreen(
            state = profileState,
            actions = NotificationsActions(
                onBack = backToMenu,
                onRetry = retryProfile,
                onMessageShown = profileViewModel::onMessageShown,
                onReservationAlertsChange = profileViewModel::onReservationAlertsChange,
                onAlertsChange = profileViewModel::onAlertsChange
            )
        )

        ProfileDestination.Help -> HelpScreen(
            state = supportState,
            actions = HelpActions(
                onBack = backToMenu,
                onRetry = { supportViewModel.load(email) },
                onMessageShown = supportViewModel::onMessageShown,
                onNewRequest = {
                    supportViewModel.onNewRequest()
                    destination = ProfileDestination.NewRequest
                }
            )
        )

        ProfileDestination.NewRequest -> NewRequestScreen(
            state = supportState,
            actions = NewRequestActions(
                onBack = { destination = ProfileDestination.Help },
                onMessageShown = supportViewModel::onMessageShown,
                onCategoryChange = supportViewModel::onCategoryChange,
                onDescriptionChange = supportViewModel::onDescriptionChange,
                onSend = { supportViewModel.onSend(email) }
            )
        )
    }
}
