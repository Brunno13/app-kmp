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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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

        assertEquals(
            1,
            repository.loginCalls
        )

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

        assertEquals(
            1,
            repository.loginCalls
        )

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

        assertEquals(
            1,
            repository.registerCalls
        )

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
            forgotPasswordResult =
                AppResult.Success(Unit)
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
            forgotPasswordResult =
                AppResult.Error(
                    AuthError.USER_NOT_FOUND
                )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.forgotPassword(
            email = "missing@example.com"
        )

        advanceUntilIdle()

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
    fun logoutClearsViewModelStateAndInvokesCallback() = runTest {
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

        assertEquals(
            AutoLoginState.ProceedToHome,
            viewModel.autoLoginState.value
        )

        var callbackCalls = 0

        viewModel.logout {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            1,
            repository.logoutCalls
        )

        assertEquals(
            LoginUiState.Idle,
            viewModel.uiState.value
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )

        assertEquals(
            1,
            callbackCalls
        )
    }

    @Test
    fun currentUserReflectsRepositoryUpdatesWhileSubscribed() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.currentUser.collect()
        }

        val user = UserEntity(
            id = 10,
            name = "Test User",
            email = "test@example.com",
            avatarFilename = "avatar.png",
            avatarData = "avatar-base64"
        )

        repository.currentUserFlow.value = user

        advanceUntilIdle()

        assertEquals(
            user,
            viewModel.currentUser.value
        )
    }

    @Test
    fun checkAutoLoginWithoutUserKeepsStateIdle() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.currentUser.collect()
        }

        advanceUntilIdle()

        viewModel.checkAutoLogin(
            isDeviceBiometricAvailable = true
        )

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )

        assertEquals(
            0,
            repository.isBiometricEnabledCalls
        )

        assertEquals(
            0,
            repository.logoutCalls
        )
    }

    @Test
    fun checkAutoLoginWithoutBiometricProceedsToHome() = runTest {
        val repository = FakeAuthRepository(
            biometricEnabledValue = false
        )

        val viewModel = AuthViewModel(repository)

        collectCurrentUser(
            viewModel = viewModel
        )

        repository.currentUserFlow.value =
            testUser()

        advanceUntilIdle()

        viewModel.checkAutoLogin(
            isDeviceBiometricAvailable = true
        )

        assertEquals(
            AutoLoginState.ProceedToHome,
            viewModel.autoLoginState.value
        )

        assertEquals(
            1,
            repository.isBiometricEnabledCalls
        )

        assertEquals(
            0,
            repository.logoutCalls
        )
    }

    @Test
    fun checkAutoLoginReadsCurrentBiometricPreference() = runTest {
        val repository = FakeAuthRepository(
            biometricEnabledValue = false
        )

        val viewModel = AuthViewModel(repository)

        collectCurrentUser(
            viewModel = viewModel
        )

        repository.currentUserFlow.value =
            testUser()

        advanceUntilIdle()

        /*
         * A preferência muda DEPOIS da criação do ViewModel.
         * checkAutoLogin deve consultar o repository novamente.
         */
        repository.biometricEnabledValue = true

        viewModel.checkAutoLogin(
            isDeviceBiometricAvailable = true
        )

        assertEquals(
            AutoLoginState.RequestBiometrics,
            viewModel.autoLoginState.value
        )

        assertEquals(
            1,
            repository.isBiometricEnabledCalls
        )
    }

    @Test
    fun checkAutoLoginWithUnavailableBiometricLogsOutAndMarksRevoked() =
        runTest {
            val repository = FakeAuthRepository(
                biometricEnabledValue = true
            )

            val viewModel =
                AuthViewModel(repository)

            collectCurrentUser(
                viewModel = viewModel
            )

            repository.currentUserFlow.value =
                testUser()

            advanceUntilIdle()

            viewModel.checkAutoLogin(
                isDeviceBiometricAvailable = false
            )

            advanceUntilIdle()

            assertEquals(
                1,
                repository.logoutCalls
            )

            assertEquals(
                LoginUiState.Idle,
                viewModel.uiState.value
            )

            assertEquals(
                AutoLoginState.BiometricsRevoked,
                viewModel.autoLoginState.value
            )
        }

    @Test
    fun onBiometricSuccessProceedsToHome() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onBiometricSuccess()

        assertEquals(
            AutoLoginState.ProceedToHome,
            viewModel.autoLoginState.value
        )
    }

    @Test
    fun resetAutoLoginStateReturnsStateToIdle() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.onBiometricSuccess()

        assertEquals(
            AutoLoginState.ProceedToHome,
            viewModel.autoLoginState.value
        )

        viewModel.resetAutoLoginState()

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )
    }

    private fun kotlinx.coroutines.test.TestScope.collectCurrentUser(
        viewModel: AuthViewModel
    ) {
        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.currentUser.collect()
        }
    }

    private fun testUser() =
        UserEntity(
            id = 10,
            name = "Test User",
            email = "test@example.com"
        )

    private class FakeAuthRepository(
        var loginResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var registerResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var forgotPasswordResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var biometricEnabledValue: Boolean = false
    ) : AuthRepository {

        val currentUserFlow =
            MutableStateFlow<UserEntity?>(null)

        private val activeSessionsFlow =
            MutableStateFlow<List<ActiveSession>>(
                emptyList()
            )

        var loginCalls = 0
            private set

        var lastLoginEmail: String? = null
            private set

        var lastLoginPassword: String? = null
            private set

        var registerCalls = 0
            private set

        var lastRegisterName: String? = null
            private set

        var lastRegisterEmail: String? = null
            private set

        var lastRegisterPassword: String? = null
            private set

        var forgotPasswordCalls = 0
            private set

        var lastForgotPasswordEmail: String? = null
            private set

        var logoutCalls = 0
            private set

        var isBiometricEnabledCalls = 0
            private set

        override fun isBiometricEnabled(): Boolean {
            isBiometricEnabledCalls++
            return biometricEnabledValue
        }

        override fun setBiometricEnabled(
            enabled: Boolean
        ) {
            biometricEnabledValue = enabled
        }

        override fun observeCurrentUser():
                Flow<UserEntity?> =
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
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun revokeSession(
            token: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun logout() {
            logoutCalls++
        }

        override suspend fun updateAvatar(
            base64: String,
            fileName: String,
            mimeType: String
        ): AppResult<Unit, AppError> =
            AppResult.Success(Unit)

        override suspend fun syncAvatar(
            filename: String
        ) = Unit
    }
}
