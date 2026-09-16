package com.brunno.appkmp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data object Success : LoginUiState
    data class Error(val error: AppError) : LoginUiState
}

sealed interface AutoLoginState {
    data object Idle : AutoLoginState
    data object ProceedToHome : AutoLoginState
    data object RequestBiometrics : AutoLoginState
    data object BiometricsRevoked : AutoLoginState
}

private const val WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS = 5_000L

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<LoginUiState>(LoginUiState.Idle)

    val uiState = _uiState.asStateFlow()

    private val _autoLoginState =
        MutableStateFlow<AutoLoginState>(AutoLoginState.Idle)

    val autoLoginState = _autoLoginState.asStateFlow()

    val currentUser = authRepository
        .observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis =
                    WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
            ),
            initialValue = null
        )

    fun checkAutoLogin(
        isDeviceBiometricAvailable: Boolean
    ) {
        val user = currentUser.value

        if (user == null) {
            _autoLoginState.value = AutoLoginState.Idle
            return
        }

        val isBiometricEnabled =
            authRepository.isBiometricEnabled()

        if (!isBiometricEnabled) {
            _autoLoginState.value =
                AutoLoginState.ProceedToHome
            return
        }

        if (isDeviceBiometricAvailable) {
            _autoLoginState.value =
                AutoLoginState.RequestBiometrics
        } else {
            logout {
                _autoLoginState.value =
                    AutoLoginState.BiometricsRevoked
            }
        }
    }

    fun onBiometricSuccess() {
        _autoLoginState.value =
            AutoLoginState.ProceedToHome
    }

    fun resetAutoLoginState() {
        _autoLoginState.value =
            AutoLoginState.Idle
    }

    fun login(
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value =
                LoginUiState.Loading

            when (
                val result =
                    authRepository.login(
                        email = email,
                        password = password
                    )
            ) {
                is AppResult.Success -> {
                    _uiState.value =
                        LoginUiState.Success

                    _autoLoginState.value =
                        AutoLoginState.ProceedToHome
                }

                is AppResult.Error -> {
                    _uiState.value =
                        LoginUiState.Error(
                            result.error
                        )
                }
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.value =
                LoginUiState.Loading

            when (
                val result =
                    authRepository.register(
                        name = name,
                        email = email,
                        password = password
                    )
            ) {
                is AppResult.Success -> {
                    _uiState.value =
                        LoginUiState.Success

                    _autoLoginState.value =
                        AutoLoginState.ProceedToHome
                }

                is AppResult.Error -> {
                    _uiState.value =
                        LoginUiState.Error(
                            result.error
                        )
                }
            }
        }
    }

    fun forgotPassword(
        email: String
    ) {
        viewModelScope.launch {
            _uiState.value =
                LoginUiState.Loading

            when (
                val result =
                    authRepository.forgotPassword(
                        email
                    )
            ) {
                is AppResult.Success -> {
                    _uiState.value =
                        LoginUiState.Success
                }

                is AppResult.Error -> {
                    _uiState.value =
                        LoginUiState.Error(
                            result.error
                        )
                }
            }
        }
    }

    fun logout(
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            authRepository.logout()

            resetState()
            resetAutoLoginState()

            onComplete()
        }
    }

    fun resetState() {
        _uiState.value =
            LoginUiState.Idle
    }
}
