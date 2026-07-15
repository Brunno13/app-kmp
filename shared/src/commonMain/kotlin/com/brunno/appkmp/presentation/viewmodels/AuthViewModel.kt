package com.brunno.appkmp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brunno.appkmp.data.remote.models.ActiveSession
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

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState = _uiState.asStateFlow()
    private val _activeSessions = MutableStateFlow<List<ActiveSession>>(emptyList())
    val activeSessions = _activeSessions.asStateFlow()

    val currentUser = authRepository.observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun loadSessions() {
        viewModelScope.launch {
            when (val result = authRepository.getActiveSessions()) {
                is AppResult.Success -> {
                    _activeSessions.value = result.data
                }
                is AppResult.Error -> {
                    println("Falha silenciosa ao listar sessões: ${result.error}")
                }
            }
        }
    }

    fun revokeSession(token: String, onCurrentSessionRevoked: () -> Unit) {
        viewModelScope.launch {
            val isCurrentSession = token == authRepository.getCurrentToken()
            val currentList = _activeSessions.value
            _activeSessions.value = currentList.filter { it.token != token }

            when (authRepository.revokeSession(token)) {
                is AppResult.Success -> {
                    if (isCurrentSession) {
                        authRepository.logout()
                        resetState()
                        onCurrentSessionRevoked()
                    }
                }
                is AppResult.Error -> {
                    _activeSessions.value = currentList
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.login(email, password)) {
                is AppResult.Success -> _uiState.value = LoginUiState.Success
                is AppResult.Error -> _uiState.value = LoginUiState.Error(result.error)
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = LoginUiState.Loading
            when (val result = authRepository.register(name, email, password)) {
                is AppResult.Success -> _uiState.value = LoginUiState.Success
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
            onComplete()
        }
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}