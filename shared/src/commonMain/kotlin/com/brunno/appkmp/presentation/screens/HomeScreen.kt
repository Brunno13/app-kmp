package com.brunno.appkmp.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import com.brunno.appkmp.presentation.components.AlertType
import com.brunno.appkmp.presentation.components.AppBottomBar
import com.brunno.appkmp.presentation.components.AppButton
import com.brunno.appkmp.presentation.components.AppErrorScreen
import com.brunno.appkmp.presentation.components.AppModal
import com.brunno.appkmp.presentation.components.AppToast
import com.brunno.appkmp.presentation.navigation.Routes
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import kotlinx.coroutines.delay
import kmpprojectbrunno.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToProfile: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val userName = currentUser?.name ?: ""

    var showToast by remember { mutableStateOf(false) }
    var showModal by remember { mutableStateOf(false) }
    var showCrash by remember { mutableStateOf(false) }

    LaunchedEffect(showToast) {
        if (showToast) {
            delay(3000)
            showToast = false
        }
    }

    if (showCrash) {
        AppErrorScreen(
            onRetry = { showCrash = false }
        )
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AppBottomBar(
                currentRoute = Routes.HOME,
                onNavigateToHome = onNavigateToHome,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = MaterialTheme.dimens.screenPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(Res.string.welcome_user, userName),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.dimens.spaceMedium)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceHuge))

                AppButton(
                    text = stringResource(Res.string.btn_test_toast),
                    onClick = { showToast = true },
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

                AppButton(
                    text = stringResource(Res.string.btn_test_modal),
                    onClick = { showModal = true }
                )

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

                AppButton(
                    text = stringResource(Res.string.btn_simulate_crash),
                    onClick = { showCrash = true },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }

            AnimatedVisibility(
                visible = showToast,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it }),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            ) {
                AppToast(
                    title = stringResource(Res.string.toast_success_title),
                    message = stringResource(Res.string.toast_success_profile_message),
                    type = AlertType.SUCCESS
                )
            }

            if (showModal) {
                AppModal(
                    title = stringResource(Res.string.modal_update_title),
                    message = stringResource(Res.string.modal_update_message),
                    type = AlertType.INFO,
                    onDismiss = { showModal = false }
                )
            }
        }
    }
}