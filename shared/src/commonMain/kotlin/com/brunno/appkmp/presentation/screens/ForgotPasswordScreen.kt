package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.LoginUiState
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.action_back_to_login
import kmpprojectbrunno.shared.generated.resources.action_send_link
import kmpprojectbrunno.shared.generated.resources.msg_reset_link_sent
import kmpprojectbrunno.shared.generated.resources.placeholder_email
import kmpprojectbrunno.shared.generated.resources.reset_password_title
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    var email by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()

    ForgotPasswordScreenContent(
        email = email,
        uiState = uiState,
        onEmailChange = { email = it },
        onSendLink = {
            viewModel.forgotPassword(email)
        },
        onNavigateToLogin = {
            viewModel.resetState()
            onNavigateToLogin()
        }
    )
}

@Composable
private fun ForgotPasswordScreenContent(
    email: String,
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onSendLink: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
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
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(
                    MaterialTheme.dimens.spaceXXL
                )
            )

            ForgotPasswordContent(
                email = email,
                uiState = uiState,
                onEmailChange = onEmailChange,
                onSendLink = onSendLink
            )

            Spacer(
                modifier = Modifier.height(
                    MaterialTheme.dimens.spaceExtraLarge
                )
            )

            Text(
                text = stringResource(
                    Res.string.action_back_to_login
                ),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onNavigateToLogin)
                    .padding(MaterialTheme.dimens.spaceSmall)
            )
        }
    }
}

@Composable
private fun ForgotPasswordContent(
    email: String,
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onSendLink: () -> Unit
) {
    if (uiState is LoginUiState.Success) {
        Text(
            text = stringResource(Res.string.msg_reset_link_sent),
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        return
    }

    AppTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = stringResource(Res.string.placeholder_email)
    )

    if (uiState is LoginUiState.Error) {
        Spacer(
            modifier = Modifier.height(MaterialTheme.dimens.spaceMedium)
        )

        Text(
            text = uiState.error.asString(),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
    }

    Spacer(
        modifier = Modifier.height(
            MaterialTheme.dimens.spaceExtraLarge
        )
    )

    Button(
        onClick = onSendLink,
        enabled = email.isNotBlank() &&
                uiState !is LoginUiState.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.dimens.buttonHeight),
        shape = MaterialTheme.shapes.medium
    ) {
        if (uiState is LoginUiState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(
                    MaterialTheme.dimens.spaceLarge
                ),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = stringResource(Res.string.action_send_link),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(
    name = "Forgot Password",
    showBackground = true
)
@Composable
private fun ForgotPasswordScreenPreview() {
    var email by remember {
        mutableStateOf("brunno@email.com")
    }

    ForgotPasswordScreenContent(
        email = email,
        uiState = LoginUiState.Idle,
        onEmailChange = { email = it },
        onSendLink = {},
        onNavigateToLogin = {}
    )
}
