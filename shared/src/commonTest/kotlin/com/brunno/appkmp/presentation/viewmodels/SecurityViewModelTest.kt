package com.brunno.appkmp.presentation.viewmodels

import com.brunno.appkmp.domain.model.ActiveSessionInfo
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.error.NetworkError
import com.brunno.appkmp.domain.repository.SecurityRepository
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
class SecurityViewModelTest {

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
    fun initialStateComesFromRepository() = runTest {
        val repository = FakeSecurityRepository(
            biometricEnabledValue = true
        )

        val viewModel = SecurityViewModel(repository)

        assertEquals(
            SecurityActionState.Idle,
            viewModel.uiState.value
        )

        assertEquals(
            null,
            viewModel.sessionError.value
        )

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
        )
    }

    @Test
    fun toggleBiometricPersistsValueThroughRepository() = runTest {
        val repository = FakeSecurityRepository()
        val viewModel = SecurityViewModel(repository)

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
    fun changePasswordSuccessUpdatesUiStateAndForwardsArguments() = runTest {
        val repository = FakeSecurityRepository(
            changePasswordResult = AppResult.Success(Unit)
        )

        val viewModel = SecurityViewModel(repository)

        viewModel.changePassword(
            currentPassword = "old-password",
            newPassword = "new-password"
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
            SecurityActionState.Success,
            viewModel.uiState.value
        )
    }

    @Test
    fun changePasswordErrorUpdatesUiState() = runTest {
        val repository = FakeSecurityRepository(
            changePasswordResult = AppResult.Error(
                AuthError.INVALID_PASSWORD
            )
        )

        val viewModel = SecurityViewModel(repository)

        viewModel.changePassword(
            currentPassword = "wrong-password",
            newPassword = "new-password"
        )

        advanceUntilIdle()

        val state = assertIs<SecurityActionState.Error>(
            viewModel.uiState.value
        )

        assertEquals(
            AuthError.INVALID_PASSWORD,
            state.error
        )
    }

    @Test
    fun loadSessionsRequestsSynchronization() = runTest {
        val repository = FakeSecurityRepository(
            syncActiveSessionsResult = AppResult.Success(Unit)
        )

        val viewModel = SecurityViewModel(repository)

        viewModel.loadSessions()

        advanceUntilIdle()

        assertEquals(
            1,
            repository.syncActiveSessionsCalls
        )

        assertEquals(
            null,
            viewModel.sessionError.value
        )
    }

    @Test
    fun loadSessionsErrorIsExposedAsSessionError() = runTest {
        val repository = FakeSecurityRepository(
            syncActiveSessionsResult = AppResult.Error(
                NetworkError.SERVER_ERROR
            )
        )

        val viewModel = SecurityViewModel(repository)

        viewModel.loadSessions()

        advanceUntilIdle()

        assertEquals(
            NetworkError.SERVER_ERROR,
            viewModel.sessionError.value
        )

        assertEquals(
            SecurityActionState.Idle,
            viewModel.uiState.value
        )
    }


    @Test
    fun activeSessionsReflectRepositoryUpdatesWhileSubscribed() = runTest {
        val repository =
            FakeSecurityRepository()

        val viewModel =
            SecurityViewModel(repository)

        backgroundScope.launch(
            UnconfinedTestDispatcher(
                testScheduler
            )
        ) {
            viewModel.activeSessions.collect()
        }

        val sessions = listOf(
            ActiveSessionInfo(
                id = "session-1",
                expiresAt = null,
                createdAt = null,
                updatedAt = null,
                userId = "user-1",
                ipAddress =
                    "192.168.31.10",
                userAgent =
                    "Test Agent"
            ),
            ActiveSessionInfo(
                id = "session-2",
                expiresAt = null,
                createdAt = null,
                updatedAt = null,
                userId = "user-1",
                ipAddress =
                    "192.168.31.11",
                userAgent =
                    "Other Agent"
            )
        )

        repository.activeSessionsFlow.value =
            sessions

        advanceUntilIdle()

        assertEquals(
            sessions,
            viewModel.activeSessions.value
        )
    }


    @Test
    fun revokeOtherSessionDoesNotInvokeCurrentSessionCallback() = runTest {
        val repository =
            FakeSecurityRepository(
                currentSessionId =
                    "current-session"
            )

        val viewModel =
            SecurityViewModel(repository)

        var callbackCalls = 0

        viewModel.revokeSession(
            sessionId = "other-session"
        ) {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            1,
            repository.revokeSessionCalls
        )

        assertEquals(
            "other-session",
            repository.lastRevokeSessionId
        )

        assertEquals(
            0,
            callbackCalls
        )

        assertEquals(
            null,
            viewModel.sessionError.value
        )
    }


    @Test
    fun revokeCurrentSessionDisablesBiometricAndInvokesCallback() = runTest {
        val repository =
            FakeSecurityRepository(
                currentSessionId =
                    "current-session",
                biometricEnabledValue = true
            )

        val viewModel =
            SecurityViewModel(repository)

        var callbackCalls = 0

        viewModel.revokeSession(
            sessionId = "current-session"
        ) {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            1,
            repository.revokeSessionCalls
        )

        assertEquals(
            "current-session",
            repository.lastRevokeSessionId
        )

        assertEquals(
            false,
            repository.biometricEnabledValue
        )

        assertEquals(
            false,
            viewModel.isBiometricEnabled.value
        )

        assertEquals(
            1,
            callbackCalls
        )

        assertEquals(
            null,
            viewModel.sessionError.value
        )
    }


    @Test
    fun revokeSessionErrorDoesNotInvokeCallback() = runTest {
        val repository =
            FakeSecurityRepository(
                currentSessionId =
                    "current-session",
                biometricEnabledValue = true,
                revokeSessionResult =
                    AppResult.Error(
                        NetworkError.SERVER_ERROR
                    )
            )

        val viewModel =
            SecurityViewModel(repository)

        var callbackCalls = 0

        viewModel.revokeSession(
            sessionId = "current-session"
        ) {
            callbackCalls++
        }

        advanceUntilIdle()

        assertEquals(
            NetworkError.SERVER_ERROR,
            viewModel.sessionError.value
        )

        assertEquals(
            0,
            callbackCalls
        )

        assertEquals(
            true,
            viewModel.isBiometricEnabled.value
        )
    }

    @Test
    fun resetStateReturnsUiToIdle() = runTest {
        val repository = FakeSecurityRepository()
        val viewModel = SecurityViewModel(repository)

        viewModel.changePassword(
            currentPassword = "old-password",
            newPassword = "new-password"
        )

        advanceUntilIdle()

        assertEquals(
            SecurityActionState.Success,
            viewModel.uiState.value
        )

        viewModel.resetState()

        assertEquals(
            SecurityActionState.Idle,
            viewModel.uiState.value
        )
    }

    private class FakeSecurityRepository(
        var biometricEnabledValue:
            Boolean = false,
        var currentSessionId:
            String? = null,
        var syncActiveSessionsResult:
            AppResult<Unit, AppError> =
            AppResult.Success(Unit),
        var revokeSessionResult:
            AppResult<Unit, AppError> =
            AppResult.Success(Unit),
        var changePasswordResult:
            AppResult<Unit, AppError> =
            AppResult.Success(Unit)
    ) : SecurityRepository {

        val activeSessionsFlow =
            MutableStateFlow<
                List<ActiveSessionInfo>
            >(emptyList())

        var setBiometricEnabledCalls = 0
            private set

        var lastBiometricEnabledValue:
            Boolean? = null
            private set

        var syncActiveSessionsCalls = 0
            private set

        var revokeSessionCalls = 0
            private set

        var lastRevokeSessionId:
            String? = null
            private set

        var changePasswordCalls = 0
            private set

        var lastCurrentPassword:
            String? = null
            private set

        var lastNewPassword:
            String? = null
            private set

        override fun isBiometricEnabled():
                Boolean =
            biometricEnabledValue

        override fun setBiometricEnabled(
            enabled: Boolean
        ) {
            setBiometricEnabledCalls++
            lastBiometricEnabledValue =
                enabled
            biometricEnabledValue =
                enabled
        }

        override fun isCurrentSession(
            sessionId: String
        ): Boolean =
            sessionId == currentSessionId

        override fun observeActiveSessions():
                Flow<List<ActiveSessionInfo>> =
            activeSessionsFlow

        override suspend fun syncActiveSessions():
                AppResult<Unit, AppError> {
            syncActiveSessionsCalls++

            return syncActiveSessionsResult
        }

        override suspend fun revokeSession(
            sessionId: String
        ): AppResult<Unit, AppError> {
            revokeSessionCalls++
            lastRevokeSessionId =
                sessionId

            return revokeSessionResult
        }

        override suspend fun changePassword(
            currentPassword: String,
            newPassword: String
        ): AppResult<Unit, AppError> {
            changePasswordCalls++
            lastCurrentPassword =
                currentPassword
            lastNewPassword =
                newPassword

            return changePasswordResult
        }
    }

}
