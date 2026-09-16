package com.brunno.appkmp.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS = 5_000L

sealed interface ProfileActionState {
    data object Idle : ProfileActionState
    data object Loading : ProfileActionState
    data object Success : ProfileActionState
    data class Error(val error: AppError) : ProfileActionState
}

class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState =
        MutableStateFlow<ProfileActionState>(ProfileActionState.Idle)

    val uiState = _uiState.asStateFlow()

    val currentUser = profileRepository
        .observeCurrentUser()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = WHILE_SUBSCRIBED_STOP_TIMEOUT_MILLIS
            ),
            initialValue = null
        )

    fun updateUser(name: String) {
        viewModelScope.launch {
            _uiState.value = ProfileActionState.Loading

            when (val result = profileRepository.updateUser(name)) {
                is AppResult.Success -> {
                    _uiState.value = ProfileActionState.Success
                }

                is AppResult.Error -> {
                    _uiState.value =
                        ProfileActionState.Error(result.error)
                }
            }
        }
    }

    fun updateAvatar(
        base64: String,
        fileName: String,
        mimeType: String
    ) {
        viewModelScope.launch {
            _uiState.value = ProfileActionState.Loading

            when (
                val result = profileRepository.updateAvatar(
                    base64 = base64,
                    fileName = fileName,
                    mimeType = mimeType
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = ProfileActionState.Success
                }

                is AppResult.Error -> {
                    _uiState.value =
                        ProfileActionState.Error(result.error)
                }
            }
        }
    }

    fun syncAvatarIfNeeded(filename: String?) {
        if (filename.isNullOrBlank()) {
            return
        }

        viewModelScope.launch {
            profileRepository.syncAvatar(filename)
        }
    }

    fun resetState() {
        _uiState.value = ProfileActionState.Idle
    }
}
