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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher

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

    @Test
    fun updateAvatarSuccessUpdatesUiStateAndForwardsArguments() = runTest {
        val repository = FakeAuthRepository(
            updateAvatarResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.updateAvatar(
            base64 = "avatar-base64",
            fileName = "avatar.jpg",
            mimeType = "image/jpeg"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.updateAvatarCalls
        )

        assertEquals(
            "avatar-base64",
            repository.lastUpdateAvatarBase64
        )

        assertEquals(
            "avatar.jpg",
            repository.lastUpdateAvatarFileName
        )

        assertEquals(
            "image/jpeg",
            repository.lastUpdateAvatarMimeType
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
    fun updateAvatarErrorUpdatesUiState() = runTest {
        val repository = FakeAuthRepository(
            updateAvatarResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.updateAvatar(
            base64 = "avatar-base64",
            fileName = "avatar.jpg",
            mimeType = "image/jpeg"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.updateAvatarCalls
        )

        assertEquals(
            "avatar-base64",
            repository.lastUpdateAvatarBase64
        )

        assertEquals(
            "avatar.jpg",
            repository.lastUpdateAvatarFileName
        )

        assertEquals(
            "image/jpeg",
            repository.lastUpdateAvatarMimeType
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

    @Test
    fun changePasswordSuccessUpdatesUiStateAndForwardsArguments() = runTest {
        val repository = FakeAuthRepository(
            changePasswordResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.changePassword(
            current = "old-password",
            new = "new-password"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.changePasswordCalls
        )

        assertEquals(
            "old-password",
            repository.lastCurrentPassword
        )

        assertEquals(
            "new-password",
            repository.lastNewPassword
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
    fun changePasswordErrorUpdatesUiState() = runTest {
        val repository = FakeAuthRepository(
            changePasswordResult = AppResult.Error(
                AuthError.INVALID_PASSWORD
            )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.changePassword(
            current = "wrong-password",
            new = "new-password"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.changePasswordCalls
        )

        assertEquals(
            "wrong-password",
            repository.lastCurrentPassword
        )

        assertEquals(
            "new-password",
            repository.lastNewPassword
        )

        val state =
            assertIs<LoginUiState.Error>(
                viewModel.uiState.value
            )

        assertEquals(
            AuthError.INVALID_PASSWORD,
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

        // Coloca o ViewModel em um estado diferente do inicial.
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

        // Para o FakeAuthRepository o cast para AuthRepositoryImpl
        // não acontece, mas o estado local do ViewModel ainda muda.
        viewModel.toggleBiometric(true)

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
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
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )

        assertEquals(
            LoginUiState.Idle,
            viewModel.uiState.value
        )

        assertEquals(
            false,
            viewModel.isBiometricEnabled.value
        )

        assertEquals(
            1,
            callbackCalls
        )
    }

    @Test
    fun syncAvatarIfNeededCallsRepositoryWhenFilenameIsValid() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.syncAvatarIfNeeded(
            "https://example.com/api/avatar/avatar.png"
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.syncAvatarCalls
        )

        assertEquals(
            "https://example.com/api/avatar/avatar.png",
            repository.lastSyncAvatarFilename
        )
    }

    @Test
    fun syncAvatarIfNeededIgnoresNullAndBlankFilename() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        viewModel.syncAvatarIfNeeded(null)
        viewModel.syncAvatarIfNeeded("")
        viewModel.syncAvatarIfNeeded("   ")

        advanceUntilIdle()

        assertEquals(
            0,
            repository.syncAvatarCalls
        )

        assertEquals(
            null,
            repository.lastSyncAvatarFilename
        )
    }

    @Test
    fun loadSessionsRequestsSessionSynchronization() = runTest {
        val repository = FakeAuthRepository(
            syncActiveSessionsResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.loadSessions()

        advanceUntilIdle()

        assertEquals(
            1,
            repository.syncActiveSessionsCalls
        )

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
    fun revokeOtherSessionDoesNotLogoutOrInvokeCurrentSessionCallback() = runTest {
        val repository = FakeAuthRepository(
            currentTokenValue = "current-token",
            revokeSessionResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = AuthViewModel(repository)

        var callbackCalls = 0

        viewModel.revokeSession(
            token = "other-token"
        ) {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            1,
            repository.revokeSessionCalls
        )

        assertEquals(
            "other-token",
            repository.lastRevokeSessionToken
        )

        assertEquals(
            0,
            repository.logoutCalls
        )

        assertEquals(
            0,
            callbackCalls
        )

        assertEquals(
            LoginUiState.Idle,
            viewModel.uiState.value
        )
    }

    @Test
    fun revokeCurrentSessionLogsOutResetsStateAndInvokesCallback() = runTest {
        val repository = FakeAuthRepository(
            currentTokenValue = "current-token",
            revokeSessionResult = AppResult.Success(Unit)
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

        viewModel.revokeSession(
            token = "current-token"
        ) {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            1,
            repository.revokeSessionCalls
        )

        assertEquals(
            "current-token",
            repository.lastRevokeSessionToken
        )

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
    fun revokeSessionErrorDoesNotLogoutOrInvokeCallback() = runTest {
        val repository = FakeAuthRepository(
            currentTokenValue = "current-token",
            revokeSessionResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = AuthViewModel(repository)

        var callbackCalls = 0

        viewModel.revokeSession(
            token = "current-token"
        ) {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            1,
            repository.revokeSessionCalls
        )

        assertEquals(
            "current-token",
            repository.lastRevokeSessionToken
        )

        assertEquals(
            0,
            repository.logoutCalls
        )

        assertEquals(
            0,
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
    fun activeSessionsReflectRepositoryUpdatesWhileSubscribed() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.activeSessions.collect()
        }

        val sessions = listOf(
            ActiveSession(
                id = "session-1",
                token = "token-1",
                userId = "user-1",
                ipAddress = "192.168.31.10",
                userAgent = "Test Agent"
            ),
            ActiveSession(
                id = "session-2",
                token = "token-2",
                userId = "user-1",
                ipAddress = "192.168.31.11",
                userAgent = "Other Agent"
            )
        )

        repository.activeSessionsFlow.value = sessions

        advanceUntilIdle()

        assertEquals(
            sessions,
            viewModel.activeSessions.value
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
            repository.logoutCalls
        )
    }

    @Test
    fun checkAutoLoginWithoutBiometricProceedsToHome() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.currentUser.collect()
        }

        repository.currentUserFlow.value =
            UserEntity(
                id = 10,
                name = "Test User",
                email = "test@example.com"
            )

        advanceUntilIdle()

        assertEquals(
            false,
            viewModel.isBiometricEnabled.value
        )

        viewModel.checkAutoLogin(
            isDeviceBiometricAvailable = true
        )

        assertEquals(
            AutoLoginState.ProceedToHome,
            viewModel.autoLoginState.value
        )

        assertEquals(
            0,
            repository.logoutCalls
        )
    }

    @Test
    fun checkAutoLoginWithAvailableBiometricRequestsBiometrics() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.currentUser.collect()
        }

        repository.currentUserFlow.value =
            UserEntity(
                id = 10,
                name = "Test User",
                email = "test@example.com"
            )

        advanceUntilIdle()

        // O FakeAuthRepository não é AuthRepositoryImpl,
        // então isto altera apenas o estado local do ViewModel.
        viewModel.toggleBiometric(true)

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
        )

        viewModel.checkAutoLogin(
            isDeviceBiometricAvailable = true
        )

        assertEquals(
            AutoLoginState.RequestBiometrics,
            viewModel.autoLoginState.value
        )

        assertEquals(
            0,
            repository.logoutCalls
        )
    }

    @Test
    fun checkAutoLoginWithUnavailableBiometricLogsOutAndMarksBiometricsRevoked() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = AuthViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(testScheduler)
        ) {
            viewModel.currentUser.collect()
        }

        repository.currentUserFlow.value =
            UserEntity(
                id = 10,
                name = "Test User",
                email = "test@example.com"
            )

        advanceUntilIdle()

        viewModel.toggleBiometric(true)

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
        )

        viewModel.checkAutoLogin(
            isDeviceBiometricAvailable = false
        )

        advanceUntilIdle()

        assertEquals(
            1,
            repository.logoutCalls
        )

        assertEquals(
            false,
            viewModel.isBiometricEnabled.value
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

        assertEquals(
            AutoLoginState.Idle,
            viewModel.autoLoginState.value
        )

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

    @Test
    fun initialBiometricStateComesFromRepository() = runTest {
        val repository = FakeAuthRepository(
            biometricEnabledValue = true
        )

        val viewModel = AuthViewModel(repository)

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
        )
    }

    @Test
    fun toggleBiometricPersistsValueThroughRepository() = runTest {
        val repository = FakeAuthRepository(
            biometricEnabledValue = false
        )

        val viewModel = AuthViewModel(repository)

        viewModel.toggleBiometric(true)

        assertEquals(
            1,
            repository.setBiometricEnabledCalls
        )

        assertEquals(
            true,
            repository.lastBiometricEnabledValue
        )

        assertEquals(
            true,
            repository.biometricEnabledValue
        )

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
        )
    }

    @Test
    fun revokeCurrentSessionDisablesBiometricState() = runTest {
        val repository = FakeAuthRepository(
            currentTokenValue = "current-token",
            revokeSessionResult = AppResult.Success(Unit)
        )

        val viewModel = AuthViewModel(repository)

        viewModel.toggleBiometric(true)

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
        )

        viewModel.revokeSession(
            token = "current-token"
        ) {
        }

        advanceUntilIdle()

        assertEquals(
            1,
            repository.revokeSessionCalls
        )

        assertEquals(
            1,
            repository.logoutCalls
        )

        assertEquals(
            false,
            viewModel.isBiometricEnabled.value
        )
    }

    @Test
    fun loadSessionsErrorIsExposedAsSessionError() = runTest {
        val repository = FakeAuthRepository(
            syncActiveSessionsResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = AuthViewModel(repository)

        viewModel.loadSessions()

        advanceUntilIdle()

        assertEquals(
            NetworkError.SERVER_ERROR,
            viewModel.sessionError.value
        )

        assertEquals(
            LoginUiState.Idle,
            viewModel.uiState.value
        )
    }

    @Test
    fun revokeSessionErrorIsExposedAsSessionError() = runTest {
        val repository = FakeAuthRepository(
            currentTokenValue = "current-token",
            revokeSessionResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = AuthViewModel(repository)

        var callbackCalls = 0

        viewModel.revokeSession(
            token = "current-token"
        ) {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            NetworkError.SERVER_ERROR,
            viewModel.sessionError.value
        )

        assertEquals(
            LoginUiState.Idle,
            viewModel.uiState.value
        )

        assertEquals(
            0,
            repository.logoutCalls
        )

        assertEquals(
            0,
            callbackCalls
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
            AppResult.Success(Unit),

        var updateAvatarResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var changePasswordResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var syncActiveSessionsResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var currentTokenValue: String? = null,

        var revokeSessionResult: AppResult<Unit, AppError> =
            AppResult.Success(Unit),

        var biometricEnabledValue: Boolean = false

    ) : AuthRepository {

        var setBiometricEnabledCalls: Int = 0
            private set

        var lastBiometricEnabledValue: Boolean? = null
            private set

        var revokeSessionCalls: Int = 0
            private set

        var lastRevokeSessionToken: String? = null
            private set

        var syncActiveSessionsCalls: Int = 0
            private set

        var syncAvatarCalls: Int = 0
            private set

        var lastSyncAvatarFilename: String? = null
            private set

        var logoutCalls: Int = 0
            private set

        var changePasswordCalls: Int = 0
            private set

        var lastCurrentPassword: String? = null
            private set

        var lastNewPassword: String? = null
            private set

        var updateAvatarCalls: Int = 0
            private set

        var lastUpdateAvatarBase64: String? = null
            private set

        var lastUpdateAvatarFileName: String? = null
            private set

        var lastUpdateAvatarMimeType: String? = null
            private set

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

        override fun isBiometricEnabled(): Boolean =
            biometricEnabledValue

        override fun setBiometricEnabled(enabled: Boolean) {
            setBiometricEnabledCalls++
            lastBiometricEnabledValue = enabled
            biometricEnabledValue = enabled
        }

        override fun observeCurrentUser(): Flow<UserEntity?> =
            currentUserFlow

        override fun getCurrentToken(): String? =
            currentTokenValue

        override fun observeActiveSessions():
                Flow<List<ActiveSession>> =
            activeSessionsFlow

        override suspend fun syncActiveSessions():
                AppResult<Unit, AppError> {
            syncActiveSessionsCalls++

            return syncActiveSessionsResult
        }

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
        ): AppResult<Unit, AppError> {
            changePasswordCalls++
            lastCurrentPassword = currentPassword
            lastNewPassword = newPassword

            return changePasswordResult
        }

        override suspend fun updateUser(
            name: String
        ): AppResult<Unit, AppError> {
            updateUserCalls++
            lastUpdateUserName = name

            return updateUserResult
        }

        override suspend fun revokeSession(
            token: String
        ): AppResult<Unit, AppError> {
            revokeSessionCalls++
            lastRevokeSessionToken = token

            return revokeSessionResult
        }

        override suspend fun logout() {
            logoutCalls++
        }

        override suspend fun updateAvatar(
            base64: String,
            fileName: String,
            mimeType: String
        ): AppResult<Unit, AppError> {
            updateAvatarCalls++
            lastUpdateAvatarBase64 = base64
            lastUpdateAvatarFileName = fileName
            lastUpdateAvatarMimeType = mimeType

            return updateAvatarResult
        }

        override suspend fun syncAvatar(
            filename: String
        ) {
            syncAvatarCalls++
            lastSyncAvatarFilename = filename
        }
    }
}