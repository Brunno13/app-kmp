package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.SessionEntity
import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.data.remote.models.AvatarUpdateRequest
import com.brunno.appkmp.data.remote.models.AvatarUploadResponse
import com.brunno.appkmp.data.remote.models.BetterAuthUser
import com.brunno.appkmp.data.remote.models.ChangePasswordRequest
import com.brunno.appkmp.data.remote.models.ChangePasswordResponse
import com.brunno.appkmp.data.remote.models.ForgotPasswordRequest
import com.brunno.appkmp.data.remote.models.LoginRequest
import com.brunno.appkmp.data.remote.models.LoginResponse
import com.brunno.appkmp.data.remote.models.RegisterRequest
import com.brunno.appkmp.data.remote.models.RevokeSessionRequest
import com.brunno.appkmp.data.remote.models.RevokeSessionResponse
import com.brunno.appkmp.data.remote.models.UpdateUserRequest
import com.brunno.appkmp.data.remote.models.UpdateUserResponse
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.AuthError
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import com.brunno.appkmp.domain.error.NetworkError

class AuthRepositoryImplTest {

    @Test
    fun currentTokenDefaultsToNull() {
        val fixture = createFixture()

        assertNull(fixture.repository.getCurrentToken())
    }

    @Test
    fun currentTokenReturnsPersistedValue() {
        val settings = MapSettings().apply {
            putString("auth_token", "test-token")
        }

        val fixture = createFixture(settings = settings)

        assertEquals(
            "test-token",
            fixture.repository.getCurrentToken()
        )
    }

    @Test
    fun biometricDefaultsToFalse() {
        val fixture = createFixture()

        assertFalse(fixture.repository.isBiometricEnabled())
    }

    @Test
    fun biometricSettingIsPersisted() {
        val settings = MapSettings()
        val fixture = createFixture(settings = settings)

        fixture.repository.setBiometricEnabled(true)

        assertTrue(fixture.repository.isBiometricEnabled())

        val recreatedFixture = createFixture(settings = settings)

        assertTrue(recreatedFixture.repository.isBiometricEnabled())

        recreatedFixture.repository.setBiometricEnabled(false)

        assertFalse(recreatedFixture.repository.isBiometricEnabled())
    }

    @Test
    fun loginPersistsTokenAndUser() = runTest {
        val fixture = createFixture(
            loginResponse = LoginResponse(
                token = "token-123",
                user = BetterAuthUser(
                    id = "user-123",
                    name = "Test User",
                    email = "test@example.com",
                    image = "https://example.com/avatar.png"
                )
            )
        )

        val result = fixture.repository.login(
            email = "test@example.com",
            password = "test-password"
        )

        assertIs<AppResult.Success<*>>(result)

        assertEquals(
            LoginRequest(
                email = "test@example.com",
                password = "test-password"
            ),
            fixture.api.lastLoginRequest
        )

        assertEquals(
            "token-123",
            fixture.settings.getStringOrNull("auth_token")
        )

        assertEquals(1, fixture.userDao.clearSessionCalls)
        assertEquals(1, fixture.userDao.users.size)

        val savedUser = fixture.userDao.users.single()

        assertEquals("Test User", savedUser.name)
        assertEquals("test@example.com", savedUser.email)
        assertEquals("avatar.png", savedUser.avatarFilename)
        assertNull(savedUser.avatarData)
    }

    @Test
    fun loginWithoutUserReturnsUnauthorizedAndDoesNotPersistSession() = runTest {
        val fixture = createFixture(
            loginResponse = LoginResponse(
                token = "token-123",
                user = null
            )
        )

        val result = fixture.repository.login(
            email = "test@example.com",
            password = "test-password"
        )

        val error = assertIs<AppResult.Error<*>>(result)

        assertEquals(
            AuthError.UNAUTHORIZED,
            error.error
        )

        assertNull(
            fixture.settings.getStringOrNull("auth_token")
        )

        assertEquals(0, fixture.userDao.clearSessionCalls)
        assertTrue(fixture.userDao.users.isEmpty())
    }

    @Test
    fun loginWithoutTokenReturnsUnauthorizedAndDoesNotPersistSession() = runTest {
        val fixture = createFixture(
            loginResponse = LoginResponse(
                token = null,
                user = BetterAuthUser(
                    id = "user-123",
                    name = "Test User",
                    email = "test@example.com"
                )
            )
        )

        val result = fixture.repository.login(
            email = "test@example.com",
            password = "test-password"
        )

        val error = assertIs<AppResult.Error<*>>(result)

        assertEquals(
            AuthError.UNAUTHORIZED,
            error.error
        )

        assertNull(
            fixture.settings.getStringOrNull("auth_token")
        )

        assertEquals(0, fixture.userDao.clearSessionCalls)
        assertTrue(fixture.userDao.users.isEmpty())
    }

    @Test
    fun logoutClearsLocalState() = runTest {
        val settings = MapSettings().apply {
            putString("auth_token", "token-123")
            putBoolean("biometric_enabled", true)
        }

        val fixture = createFixture(
            settings = settings
        )

        fixture.userDao.insertUser(
            UserEntity(
                name = "Test User",
                email = "test@example.com"
            )
        )

        fixture.repository.logout()

        assertEquals(1, fixture.api.logoutCalls)
        assertEquals(1, fixture.userDao.clearSessionCalls)
        assertEquals(1, fixture.sessionDao.clearAllCalls)

        assertTrue(fixture.userDao.users.isEmpty())
        assertNull(settings.getStringOrNull("auth_token"))
        assertFalse(settings.getBoolean("biometric_enabled", false))
    }

    @Test
    fun logoutClearsLocalStateEvenWhenRemoteLogoutFails() = runTest {
        val settings = MapSettings().apply {
            putString("auth_token", "token-123")
            putBoolean("biometric_enabled", true)
        }

        val fixture = createFixture(
            settings = settings,
            logoutFailure = IllegalStateException("Remote logout failed")
        )

        fixture.userDao.insertUser(
            UserEntity(
                name = "Test User",
                email = "test@example.com"
            )
        )

        fixture.repository.logout()

        assertEquals(1, fixture.api.logoutCalls)
        assertEquals(1, fixture.userDao.clearSessionCalls)
        assertEquals(1, fixture.sessionDao.clearAllCalls)

        assertTrue(fixture.userDao.users.isEmpty())
        assertNull(settings.getStringOrNull("auth_token"))
        assertFalse(settings.getBoolean("biometric_enabled", false))
    }

    @Test
    fun syncActiveSessionsReplacesLocalSessionsWithValidRemoteSessions() = runTest {
        val fixture = createFixture(
            sessionsResponse = listOf(
                ActiveSession(
                    id = "session-1",
                    token = "token-1",
                    userId = "user-1"
                ),
                ActiveSession(
                    id = null,
                    token = "invalid-without-id"
                ),
                ActiveSession(
                    id = "invalid-without-token",
                    token = null
                ),
                ActiveSession(
                    id = "session-2",
                    token = "token-2",
                    userId = "user-1"
                )
            )
        )

        val result = fixture.repository.syncActiveSessions()

        assertIs<AppResult.Success<*>>(result)

        assertEquals(1, fixture.api.listSessionsCalls)
        assertEquals(1, fixture.sessionDao.clearAllCalls)
        assertEquals(1, fixture.sessionDao.insertAllCalls)

        assertEquals(
            listOf("token-1", "token-2"),
            fixture.sessionDao.sessions.map { it.token }
        )

        assertEquals(
            listOf("session-1", "session-2"),
            fixture.sessionDao.sessions.map { it.id }
        )
    }

    @Test
    fun syncActiveSessionsKeepsLocalStateWhenRemoteRequestFails() = runTest {
        val existingSession = SessionEntity(
            token = "existing-token",
            id = "existing-session",
            expiresAt = null,
            createdAt = null,
            updatedAt = null,
            ipAddress = null,
            userAgent = null,
            userId = "user-1"
        )

        val fixture = createFixture(
            initialSessions = listOf(existingSession),
            listSessionsFailure = IllegalStateException("Remote failure")
        )

        val result = fixture.repository.syncActiveSessions()

        val error = assertIs<AppResult.Error<*>>(result)

        assertEquals(
            NetworkError.UNKNOWN,
            error.error
        )

        assertEquals(1, fixture.api.listSessionsCalls)
        assertEquals(0, fixture.sessionDao.clearAllCalls)
        assertEquals(0, fixture.sessionDao.insertAllCalls)

        assertEquals(
            listOf(existingSession),
            fixture.sessionDao.sessions
        )
    }

    private fun createFixture(
        settings: MapSettings = MapSettings(),
        loginResponse: LoginResponse = LoginResponse(),
        logoutFailure: Exception? = null,
        sessionsResponse: List<ActiveSession> = emptyList(),
        listSessionsFailure: Exception? = null,
        initialSessions: List<SessionEntity> = emptyList()
    ): Fixture {
        val api = FakeAuthApi(
            loginResponse = loginResponse,
            logoutFailure = logoutFailure,
            sessionsResponse = sessionsResponse,
            listSessionsFailure = listSessionsFailure
        )

        val userDao = FakeUserDao()
        val sessionDao = FakeSessionDao(
            initialSessions = initialSessions
        )

        val repository = AuthRepositoryImpl(
            api = api,
            dao = userDao,
            sessionDao = sessionDao,
            settings = settings
        )

        return Fixture(
            repository = repository,
            api = api,
            userDao = userDao,
            sessionDao = sessionDao,
            settings = settings
        )
    }

    private data class Fixture(
        val repository: AuthRepositoryImpl,
        val api: FakeAuthApi,
        val userDao: FakeUserDao,
        val sessionDao: FakeSessionDao,
        val settings: MapSettings
    )

    private class FakeAuthApi(
        var loginResponse: LoginResponse,
        var logoutFailure: Exception? = null,
        var sessionsResponse: List<ActiveSession> = emptyList(),
        var listSessionsFailure: Exception? = null
    ) : AuthApi {

        var listSessionsCalls: Int = 0
            private set

        var logoutCalls: Int = 0
            private set

        var lastLoginRequest: LoginRequest? = null
            private set

        override suspend fun login(
            request: LoginRequest
        ): LoginResponse {
            lastLoginRequest = request
            return loginResponse
        }

        override suspend fun register(
            request: RegisterRequest
        ): LoginResponse = unused()

        override suspend fun forgotPassword(
            request: ForgotPasswordRequest
        ): Unit = unused()

        override suspend fun changePassword(
            request: ChangePasswordRequest
        ): ChangePasswordResponse = unused()

        override suspend fun updateUser(
            request: UpdateUserRequest
        ): UpdateUserResponse = unused()

        override suspend fun listSessions(): List<ActiveSession> {
            listSessionsCalls++

            listSessionsFailure?.let {
                throw it
            }

            return sessionsResponse
        }

        override suspend fun revokeSession(
            request: RevokeSessionRequest
        ): RevokeSessionResponse = unused()

        override suspend fun logout() {
            logoutCalls++

            logoutFailure?.let {
                throw it
            }
        }

        override suspend fun uploadAvatar(
            request: AvatarUpdateRequest
        ): AvatarUploadResponse = unused()

        override suspend fun getAvatar(
            filename: String
        ): ByteArray = unused()
    }

    private class FakeUserDao : UserDao {

        private val usersFlow =
            MutableStateFlow<List<UserEntity>>(emptyList())

        val users: List<UserEntity>
            get() = usersFlow.value

        var clearSessionCalls: Int = 0
            private set

        override suspend fun insertUser(user: UserEntity) {
            usersFlow.value = listOf(user)
        }

        override fun getAllUsers(): Flow<List<UserEntity>> =
            usersFlow

        override suspend fun clearSession() {
            clearSessionCalls++
            usersFlow.value = emptyList()
        }
    }

    private class FakeSessionDao(
        initialSessions: List<SessionEntity> = emptyList()
    ) : SessionDao {

        private val sessionsFlow =
            MutableStateFlow(initialSessions)

        val sessions: List<SessionEntity>
            get() = sessionsFlow.value

        var insertAllCalls: Int = 0
            private set

        var clearAllCalls: Int = 0
            private set

        override suspend fun insertAll(
            sessions: List<SessionEntity>
        ) {
            insertAllCalls++
            sessionsFlow.value = sessions
        }

        override fun observeAllSessions(): Flow<List<SessionEntity>> =
            sessionsFlow

        override suspend fun deleteByToken(token: String) {
            sessionsFlow.value =
                sessionsFlow.value.filterNot { it.token == token }
        }

        override suspend fun clearAll() {
            clearAllCalls++
            sessionsFlow.value = emptyList()
        }
    }

    private companion object {
        fun unused(): Nothing =
            error("Unexpected call in test")
    }
}