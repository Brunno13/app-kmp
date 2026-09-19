package com.brunno.appkmp.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.brunno.appkmp.presentation.components.AppTextField
import com.brunno.appkmp.presentation.theme.dimens
import com.brunno.appkmp.presentation.utils.asString
import com.brunno.appkmp.presentation.viewmodels.AuthViewModel
import com.brunno.appkmp.presentation.viewmodels.LoginUiState
import kmpprojectbrunno.shared.generated.resources.Res
import kmpprojectbrunno.shared.generated.resources.action_sign_in
import kmpprojectbrunno.shared.generated.resources.action_sign_up
import kmpprojectbrunno.shared.generated.resources.create_account
import kmpprojectbrunno.shared.generated.resources.msg_already_have_account
import kmpprojectbrunno.shared.generated.resources.placeholder_confirm_password
import kmpprojectbrunno.shared.generated.resources.placeholder_email
import kmpprojectbrunno.shared.generated.resources.placeholder_full_name
import kmpprojectbrunno.shared.generated.resources.placeholder_password
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private data class RegisterFormState(
    val fullName: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
) {
    val isValid: Boolean
        get() = fullName.isNotBlank() &&
                email.isNotBlank() &&
                password.isNotBlank() &&
                password == confirmPassword
}

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    var form by remember { mutableStateOf(RegisterFormState()) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            viewModel.resetState()
            onRegisterSuccess()
        }
    }

    RegisterContent(
        form = form,
        uiState = uiState,
        onFormChange = { form = it },
        onRegister = {
            viewModel.register(
                form.fullName,
                form.email,
                form.password
            )
        },
        onNavigateToLogin = {
            viewModel.resetState()
            onNavigateToLogin()
        }
    )
}

@Composable
private fun RegisterContent(
    form: RegisterFormState,
    uiState: LoginUiState,
    onFormChange: (RegisterFormState) -> Unit,
    onRegister: () -> Unit,
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
            RegisterHeader()

            RegisterIdentityFields(
                fullName = form.fullName,
                email = form.email,
                onFullNameChange = {
                    onFormChange(form.copy(fullName = it))
                },
                onEmailChange = {
                    onFormChange(form.copy(email = it))
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

            RegisterPasswordFields(
                password = form.password,
                confirmPassword = form.confirmPassword,
                onPasswordChange = {
                    onFormChange(form.copy(password = it))
                },
                onConfirmPasswordChange = {
                    onFormChange(form.copy(confirmPassword = it))
                }
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            RegisterStatusMessage(uiState = uiState)

            RegisterSubmitButton(
                isFormValid = form.isValid,
                isLoading = uiState is LoginUiState.Loading,
                onRegister = onRegister
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceExtraLarge))

            RegisterLoginPrompt(
                onNavigateToLogin = onNavigateToLogin
            )
        }
    }
}

@Composable
private fun RegisterHeader() {
    Text(
        text = stringResource(Res.string.create_account),
        style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold
        ),
        color = MaterialTheme.colorScheme.onBackground
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceXXL))
}

@Composable
private fun RegisterIdentityFields(
    fullName: String,
    email: String,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit
) {
    AppTextField(
        value = fullName,
        onValueChange = onFullNameChange,
        placeholder = stringResource(Res.string.placeholder_full_name)
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

    AppTextField(
        value = email,
        onValueChange = onEmailChange,
        placeholder = stringResource(Res.string.placeholder_email)
    )
}

@Composable
private fun RegisterPasswordFields(
    password: String,
    confirmPassword: String,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit
) {
    AppTextField(
        value = password,
        onValueChange = onPasswordChange,
        placeholder = stringResource(Res.string.placeholder_password),
        isPassword = true
    )

    Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))

    AppTextField(
        value = confirmPassword,
        onValueChange = onConfirmPasswordChange,
        placeholder = stringResource(Res.string.placeholder_confirm_password),
        isPassword = true
    )
}

@Composable
private fun RegisterStatusMessage(
    uiState: LoginUiState
) {
    when (val state = uiState) {
        is LoginUiState.Error -> {
            Text(
                text = state.error.asString(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(MaterialTheme.dimens.spaceMedium))
        }

        else -> Unit
    }
}

@Composable
private fun RegisterSubmitButton(
    isFormValid: Boolean,
    isLoading: Boolean,
    onRegister: () -> Unit
) {
    Button(
        onClick = onRegister,
        enabled = isFormValid && !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.dimens.buttonHeight),
        shape = MaterialTheme.shapes.medium
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(MaterialTheme.dimens.spaceLarge),
                color = MaterialTheme.colorScheme.onPrimary
            )
        } else {
            Text(
                text = stringResource(Res.string.action_sign_up),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun RegisterLoginPrompt(
    onNavigateToLogin: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(Res.string.msg_already_have_account),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(MaterialTheme.dimens.spaceTiny))

        Text(
            text = stringResource(Res.string.action_sign_in),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .clickable(onClick = onNavigateToLogin)
                .padding(MaterialTheme.dimens.spaceTiny)
        )
    }
}

@Preview(
    name = "Register",
    showBackground = true
)
@Composable
private fun RegisterScreenPreview() {
    var form by remember {
        mutableStateOf(
            RegisterFormState(
                fullName = "",
                email = "",
                password = "",
                confirmPassword = ""
            )
        )
    }

    RegisterContent(
        form = form,
        uiState = LoginUiState.Idle,
        onFormChange = { form = it },
        onRegister = {},
        onNavigateToLogin = {}
    )
}
