package com.brunno.appkmp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.repository.SecurityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS = 5_000L

sealed interface SecurityActionState {
    data object Idle : SecurityActionState
    data object Loading : SecurityActionState
    data object Success : SecurityActionState
    data class Error(val error: AppError) : SecurityActionState
}

class SecurityViewModel(
    private val securityRepository: SecurityRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<SecurityActionState>(SecurityActionState.Idle)

    val uiState = _uiState.asStateFlow()

    private val _sessionError =
        MutableStateFlow<AppError?>(null)

    val sessionError = _sessionError.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(
        securityRepository.isBiometricEnabled()
    )

    val isBiometricEnabled: StateFlow<Boolean> =
        _isBiometricEnabled.asStateFlow()

    val activeSessions = securityRepository
        .observeActiveSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
            ),
            initialValue = emptyList()
        )

    fun toggleBiometric(enabled: Boolean) {
        securityRepository.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    fun loadSessions() {
        viewModelScope.launch {
            when (val result = securityRepository.syncActiveSessions()) {
                is AppResult.Success -> {
                    _sessionError.value = null
                }

                is AppResult.Error -> {
                    _sessionError.value = result.error
                }
            }
        }
    }

    fun revokeSession(
        sessionId: String,
        onCurrentSessionRevoked: () -> Unit
    ) {
        viewModelScope.launch {
            val isCurrentSession =
                securityRepository.isCurrentSession(
                    sessionId
                )

            when (
                val result =
                    securityRepository.revokeSession(
                        sessionId
                    )
            ) {
                is AppResult.Success -> {
                    _sessionError.value = null

                    if (isCurrentSession) {
                        securityRepository.setBiometricEnabled(false)
                        _isBiometricEnabled.value = false
                        onCurrentSessionRevoked()
                    }
                }

                is AppResult.Error -> {
                    _sessionError.value = result.error
                }
            }
        }
    }

    fun changePassword(
        currentPassword: String,
        newPassword: String
    ) {
        viewModelScope.launch {
            _uiState.value = SecurityActionState.Loading

            when (
                val result = securityRepository.changePassword(
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = SecurityActionState.Success
                }

                is AppResult.Error -> {
                    _uiState.value =
                        SecurityActionState.Error(result.error)
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = SecurityActionState.Idle
    }
}
