package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.LoginUiState
import kmpprojectbrunno.shared.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = MaterialTheme.dimens.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(Res.string.reset_password_title),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceXXL))

            if (uiState is LoginUiState.Success) {
                Text(
                    text = stringResource(Res.string.msg_reset_link_sent),
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = stringResource(Res.string.placeholder_email)
                )

                if (uiState is LoginUiState.Error) {
                    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
                    Text(
                        text = (uiState as LoginUiState.Error).error.asString(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

                Button(
                    onClick = { viewModel.forgotPassword(email) },
                    enabled = email.isNotBlank() && uiState !is LoginUiState.Loading,
                    modifier = Modifier.fillMaxWidth().height(MaterialTheme.dimens.buttonHeight),
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (uiState is LoginUiState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(MaterialTheme.dimens.spaceLarge),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(text = stringResource(Res.string.action_send_link), fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            Text(
                text = stringResource(Res.string.action_back_to_login),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable {
                        viewModel.resetState()
                        onNavigateToLogin()
                    }
                    .padding(MaterialTheme.dimens.spaceSmall)
            )
        }
    }
}