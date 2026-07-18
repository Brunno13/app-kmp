package com.brunno.appkmp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brunno.appkmp.data.repository.AuthRepositoryImpl
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _autoLoginState = MutableStateFlow<AutoLoginState>(AutoLoginState.Idle)
    val autoLoginState = _autoLoginState.asStateFlow()

    private val _isBiometricEnabled = MutableStateFlow(
        (authRepository as? AuthRepositoryImpl)?.isBiometricEnabled() ?: false
    )
    val isBiometricEnabled: StateFlow<Boolean> = _isBiometricEnabled.asStateFlow()

    val currentUser = authRepository.observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val activeSessions = authRepository.observeActiveSessions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun checkAutoLogin(isDeviceBiometricAvailable: Boolean) {
        val user = currentUser.value
        if (user == null) {
            _autoLoginState.value = AutoLoginState.Idle
            return
        }

        if (_isBiometricEnabled.value) {
            if (isDeviceBiometricAvailable) {
                _autoLoginState.value = AutoLoginState.RequestBiometrics
            } else {
                logout {
                    _autoLoginState.value = AutoLoginState.BiometricsRevoked
                }
            }
        } else {
            _autoLoginState.value = AutoLoginState.ProceedToHome
        }
    }

    fun onBiometricSuccess() {
        _autoLoginState.value = AutoLoginState.ProceedToHome
    }

    fun resetAutoLoginState() {
        _autoLoginState.value = AutoLoginState.Idle
    }

    fun toggleBiometric(enabled: Boolean) {
        (authRepository as? AuthRepositoryImpl)?.setBiometricEnabled(enabled)
        _isBiometricEnabled.value = enabled
    }

    fun loadSessions() {
        viewModelScope.launch {
            authRepository.syncActiveSessions()
        }
    }

    fun revokeSession(token: String, onCurrentSessionRevoked: () -> Unit) {
        viewModelScope.launch {
            val isCurrentSession = token == authRepository.getCurrentToken()
            when (authRepository.revokeSession(token)) {
                is AppResult.Success -> {
                    if (isCurrentSession) {
                        authRepository.logout()
                        resetState()
                        onCurrentSessionRevoked()
                    }
                }
                is AppResult.Error -> { /* Handle Error */ }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.login(email, password)) {
                is AppResult.Success -> {
                    _uiState.value = LoginUiState.Success
                    _autoLoginState.value = AutoLoginState.ProceedToHome
                }
                is AppResult.Error -> _uiState.value = LoginUiState.Error(result.error)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.register(name, email, password)) {
                is AppResult.Success -> {
                    _uiState.value = LoginUiState.Success
                    _autoLoginState.value = AutoLoginState.ProceedToHome
                }
                is AppResult.Error -> _uiState.value = LoginUiState.Error(result.error)
            }
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.forgotPassword(email)) {
                is AppResult.Success -> _uiState.value = LoginUiState.Success
                is AppResult.Error -> _uiState.value = LoginUiState.Error(result.error)
            }
        }
    }

    fun updateUser(name: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.updateUser(name)) {
                is AppResult.Success -> _uiState.value = LoginUiState.Success
                is AppResult.Error -> _uiState.value = LoginUiState.Error(result.error)
            }
        }
    }

    fun updateAvatar(base64: String, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.updateAvatar(base64, fileName, mimeType)) {
                is AppResult.Success -> _uiState.value = LoginUiState.Success
                is AppResult.Error -> _uiState.value = LoginUiState.Error(result.error)
            }
        }
    }

    fun syncAvatarIfNeeded(filename: String?) {
        if (filename.isNullOrBlank()) return
        viewModelScope.launch {
            authRepository.syncAvatar(filename)
        }
    }

    fun changePassword(current: String, new: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.changePassword(current, new)) {
                is AppResult.Success -> _uiState.value = LoginUiState.Success
                is AppResult.Error -> _uiState.value = LoginUiState.Error(result.error)
            }
        }
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            authRepository.logout()
            resetState()
            _isBiometricEnabled.value = false
            onComplete()
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}