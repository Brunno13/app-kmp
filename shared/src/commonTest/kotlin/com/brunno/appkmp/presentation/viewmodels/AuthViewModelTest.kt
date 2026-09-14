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
import com.brunno.appkmp.domain.error.NetworkError

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

    @Test
    fun registerSuccessUpdatesUiAndAutoLoginState() = runTest {
        val repository = FakeAuthRepository(
            registerResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.register(
            name = "New User",
            email = "new@example.com",
            password = "test-password"
        )

        advanceUntilIdle()

        assertEquals(1, repository.registerCalls)

        assertEquals(
            "New User",
            repository.lastRegisterName
        )

        assertEquals(
            "new@example.com",
            repository.lastRegisterEmail
        )

        assertEquals(
            "test-password",
            repository.lastRegisterPassword
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
    fun registerErrorUpdatesUiStateAndKeepsAutoLoginIdle() = runTest {
        val repository = FakeAuthRepository(
            registerResult = AppResult.Error(
                AuthError.UNAUTHORIZED
            )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.register(
            name = "New User",
            email = "new@example.com",
            password = "test-password"
        )

        advanceUntilIdle()

        assertEquals(1, repository.registerCalls)

        val state =
            assertIs<LoginUiState.Error>(
                viewModel.uiState.value
            )

        assertEquals(
            AuthError.UNAUTHORIZED,
            state.error
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )
    }

    @Test
    fun forgotPasswordSuccessUpdatesUiState() = runTest {
        val repository = FakeAuthRepository(
            forgotPasswordResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.forgotPassword(
            email = "test@example.com"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.forgotPasswordCalls
        )

        assertEquals(
            "test@example.com",
            repository.lastForgotPasswordEmail
        )

        assertEquals(
            LoginUiState.Success,
            viewModel.uiState.value
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )
    }

    @Test
    fun forgotPasswordErrorUpdatesUiState() = runTest {
        val repository = FakeAuthRepository(
            forgotPasswordResult = AppResult.Error(
                AuthError.USER_NOT_FOUND
            )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.forgotPassword(
            email = "missing@example.com"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.forgotPasswordCalls
        )

        assertEquals(
            "missing@example.com",
            repository.lastForgotPasswordEmail
        )

        val state =
            assertIs<LoginUiState.Error>(
                viewModel.uiState.value
            )

        assertEquals(
            AuthError.USER_NOT_FOUND,
            state.error
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )
    }

    @Test
    fun updateUserSuccessUpdatesUiState() = runTest {
        val repository = FakeAuthRepository(
            updateUserResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.updateUser(
            name = "Updated Name"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.updateUserCalls
        )

        assertEquals(
            "Updated Name",
            repository.lastUpdateUserName
        )

        assertEquals(
            LoginUiState.Success,
            viewModel.uiState.value
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )
    }

    @Test
    fun updateUserErrorUpdatesUiState() = runTest {
        val repository = FakeAuthRepository(
            updateUserResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.updateUser(
            name = "Updated Name"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.updateUserCalls
        )

        assertEquals(
            "Updated Name",
            repository.lastUpdateUserName
        )

        val state =
            assertIs<LoginUiState.Error>(
                viewModel.uiState.value
            )

        assertEquals(
            NetworkError.SERVER_ERROR,
            state.error
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )
    }

    private class FakeAuthRepository(
        var loginResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var registerResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var forgotPasswordResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var updateUserResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit)
    ) : AuthRepository {

        var updateUserCalls: Int = 0
            private set

        var lastUpdateUserName: String? = null
            private set

        var forgotPasswordCalls: Int = 0
            private set

        var lastForgotPasswordEmail: String? = null
            private set

        var registerCalls: Int = 0
            private set

        var lastRegisterName: String? = null
            private set

        var lastRegisterEmail: String? = null
            private set

        var lastRegisterPassword: String? = null
            private set

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
        ): AppResult<Unit, AppError> {
            registerCalls++
            lastRegisterName = name
            lastRegisterEmail = email
            lastRegisterPassword = password

            return registerResult
        }

        override suspend fun forgotPassword(
            email: String
        ): AppResult<Unit, AppError> {
            forgotPasswordCalls++
            lastForgotPasswordEmail = email

            return forgotPasswordResult
        }

        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun updateUser(
            name: String
        ): AppResult<Unit, AppError> {
            updateUserCalls++
            lastUpdateUserName = name

            return updateUserResult
        }

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