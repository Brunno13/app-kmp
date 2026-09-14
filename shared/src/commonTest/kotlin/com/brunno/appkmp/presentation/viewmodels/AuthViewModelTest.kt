package com.brunno.appkmp.presentation.viewmodels

import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(
            StandardTestDispatcher()
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsIdle() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        assertEquals(
            LoginUiState.Idle,
            viewModel.uiState.value
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )

        assertEquals(
            false,
            viewModel.isBiometricEnabled.value
        )
    }

    @Test
    fun loginSuccessUpdatesUiAndAutoLoginState() = runTest {
        val repository = FakeAuthRepository(
            loginResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.login(
            email = "test@example.com",
            password = "test-password"
        )

        advanceUntilIdle()

        assertEquals(1, repository.loginCalls)

        assertEquals(
            "test@example.com",
            repository.lastLoginEmail
        )

        assertEquals(
            "test-password",
            repository.lastLoginPassword
        )

        assertEquals(
            LoginUiState.Success,
            viewModel.uiState.value
        )

        assertEquals(
            AutoLoginState.ProceedToHome,
            viewModel.autoLoginState.value
        )
    }

    @Test
    fun loginErrorUpdatesUiStateAndKeepsAutoLoginIdle() = runTest {
        val repository = FakeAuthRepository(
            loginResult = AppResult.Error(
                AuthError.INVALID_CREDENTIALS
            )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.login(
            email = "test@example.com",
            password = "wrong-password"
        )

        advanceUntilIdle()

        assertEquals(1, repository.loginCalls)

        val state =
            assertIs<LoginUiState.Error>(
                viewModel.uiState.value
            )

        assertEquals(
            AuthError.INVALID_CREDENTIALS,
            state.error
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )
    }

    @Test
    fun resetStateReturnsUiToIdle() = runTest {
        val repository = FakeAuthRepository(
            loginResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.login(
            email = "test@example.com",
            password = "test-password"
        )

        advanceUntilIdle()

        assertEquals(
            LoginUiState.Success,
            viewModel.uiState.value
        )

        viewModel.resetState()

        assertEquals(
            LoginUiState.Idle,
            viewModel.uiState.value
        )
    }

    private class FakeAuthRepository(
        var loginResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit)
    ) : AuthRepository {

        val currentUserFlow =
            MutableStateFlow<UserEntity?>(null)

        val activeSessionsFlow =
            MutableStateFlow<List<ActiveSession>>(
                emptyList()
            )

        var loginCalls: Int = 0
            private set

        var lastLoginEmail: String? = null
            private set

        var lastLoginPassword: String? = null
            private set

        override fun observeCurrentUser(): Flow<UserEntity?> =
            currentUserFlow

        override fun getCurrentToken(): String? =
            null

        override fun observeActiveSessions():
                Flow<List<ActiveSession>> =
            activeSessionsFlow

        override suspend fun syncActiveSessions():
                AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun login(
            email: String,
            password: String
        ): AppResult<Unit, AppError> {
            loginCalls++
            lastLoginEmail = email
            lastLoginPassword = password

            return loginResult
        }

        override suspend fun register(
            name: String,
            email: String,
            password: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun forgotPassword(
            email: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun updateUser(
            name: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun revokeSession(
            token: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun logout() {
        }

        override suspend fun updateAvatar(
            base64: String,
            fileName: String,
            mimeType: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun syncAvatar(
            filename: String
        ) {
        }
    }
}