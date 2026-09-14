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

    @Test
    fun revokeSessionRemovesOnlyRevokedSessionFromLocalState() = runTest {
        val session1 = SessionEntity(
            token = "token-1",
            id = "session-1",
            expiresAt = null,
            createdAt = null,
            updatedAt = null,
            ipAddress = null,
            userAgent = null,
            userId = "user-1"
        )

        val session2 = SessionEntity(
            token = "token-2",
            id = "session-2",
            expiresAt = null,
            createdAt = null,
            updatedAt = null,
            ipAddress = null,
            userAgent = null,
            userId = "user-1"
        )

        val fixture = createFixture(
            initialSessions = listOf(session1, session2)
        )

        val result = fixture.repository.revokeSession("token-1")

        assertIs<AppResult.Success<*>>(result)

        assertEquals(1, fixture.api.revokeSessionCalls)

        assertEquals(
            RevokeSessionRequest("token-1"),
            fixture.api.lastRevokeSessionRequest
        )

        assertEquals(1, fixture.sessionDao.deleteByTokenCalls)
        assertEquals("token-1", fixture.sessionDao.lastDeletedToken)

        assertEquals(
            listOf(session2),
            fixture.sessionDao.sessions
        )
    }

    @Test
    fun revokeSessionKeepsLocalStateWhenRemoteRequestFails() = runTest {
        val existingSession = SessionEntity(
            token = "token-1",
            id = "session-1",
            expiresAt = null,
            createdAt = null,
            updatedAt = null,
            ipAddress = null,
            userAgent = null,
            userId = "user-1"
        )

        val fixture = createFixture(
            initialSessions = listOf(existingSession),
            revokeSessionFailure = IllegalStateException("Remote failure")
        )

        val result = fixture.repository.revokeSession("token-1")

        val error = assertIs<AppResult.Error<*>>(result)

        assertEquals(
            NetworkError.UNKNOWN,
            error.error
        )

        assertEquals(1, fixture.api.revokeSessionCalls)

        assertEquals(
            RevokeSessionRequest("token-1"),
            fixture.api.lastRevokeSessionRequest
        )

        assertEquals(0, fixture.sessionDao.deleteByTokenCalls)
        assertNull(fixture.sessionDao.lastDeletedToken)

        assertEquals(
            listOf(existingSession),
            fixture.sessionDao.sessions
        )
    }

    @Test
    fun updateUserPersistsRemoteUserData() = runTest {
        val fixture = createFixture(
            updateUserResponse = UpdateUserResponse(
                user = BetterAuthUser(
                    id = "user-123",
                    name = "Updated Name",
                    email = "updated@example.com"
                )
            )
        )

        fixture.userDao.insertUser(
            UserEntity(
                id = 10,
                name = "Old Name",
                email = "old@example.com",
                avatarFilename = "avatar.png",
                avatarData = "avatar-data"
            )
        )

        val result = fixture.repository.updateUser("Requested Name")

        assertIs<AppResult.Success<*>>(result)

        assertEquals(1, fixture.api.updateUserCalls)
        assertEquals(
            UpdateUserRequest(name = "Requested Name"),
            fixture.api.lastUpdateUserRequest
        )

        val savedUser = fixture.userDao.users.single()

        assertEquals(10, savedUser.id)
        assertEquals("Updated Name", savedUser.name)
        assertEquals("updated@example.com", savedUser.email)

        // Campos não relacionados devem ser preservados.
        assertEquals("avatar.png", savedUser.avatarFilename)
        assertEquals("avatar-data", savedUser.avatarData)
    }

    @Test
    fun updateUserFallsBackToRequestedNameAndKeepsLocalEmail() = runTest {
        val fixture = createFixture(
            updateUserResponse = UpdateUserResponse()
        )

        fixture.userDao.insertUser(
            UserEntity(
                id = 10,
                name = "Old Name",
                email = "local@example.com"
            )
        )

        val result = fixture.repository.updateUser("Requested Name")

        assertIs<AppResult.Success<*>>(result)

        assertEquals(1, fixture.api.updateUserCalls)
        assertEquals(
            UpdateUserRequest(name = "Requested Name"),
            fixture.api.lastUpdateUserRequest
        )

        val savedUser = fixture.userDao.users.single()

        assertEquals("Requested Name", savedUser.name)
        assertEquals("local@example.com", savedUser.email)
    }

    @Test
    fun registerPersistsTokenAndUser() = runTest {
        val fixture = createFixture(
            registerResponse = LoginResponse(
                token = "register-token",
                user = BetterAuthUser(
                    id = "user-456",
                    name = "New User",
                    email = "new@example.com"
                )
            )
        )

        val result = fixture.repository.register(
            name = "New User",
            email = "new@example.com",
            password = "test-password"
        )

        assertIs<AppResult.Success<*>>(result)

        assertEquals(
            RegisterRequest(
                email = "new@example.com",
                password = "test-password",
                name = "New User"
            ),
            fixture.api.lastRegisterRequest
        )

        assertEquals(
            "register-token",
            fixture.settings.getStringOrNull("auth_token")
        )

        assertEquals(1, fixture.userDao.clearSessionCalls)

        val savedUser = fixture.userDao.users.single()

        assertEquals("New User", savedUser.name)
        assertEquals("new@example.com", savedUser.email)
    }

    @Test
    fun registerWithoutValidSessionReturnsUnauthorized() = runTest {
        val fixture = createFixture(
            registerResponse = LoginResponse(
                token = null,
                user = BetterAuthUser(
                    name = "New User",
                    email = "new@example.com"
                )
            )
        )

        val result = fixture.repository.register(
            name = "New User",
            email = "new@example.com",
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

    private fun createFixture(
        settings: MapSettings = MapSettings(),
        loginResponse: LoginResponse = LoginResponse(),
        logoutFailure: Exception? = null,
        sessionsResponse: List<ActiveSession> = emptyList(),
        listSessionsFailure: Exception? = null,
        initialSessions: List<SessionEntity> = emptyList(),
        revokeSessionFailure: Exception? = null,
        updateUserResponse: UpdateUserResponse = UpdateUserResponse(),
        registerResponse: LoginResponse = LoginResponse()
    ): Fixture {
        val api = FakeAuthApi(
            loginResponse = loginResponse,
            registerResponse = registerResponse,
            logoutFailure = logoutFailure,
            sessionsResponse = sessionsResponse,
            listSessionsFailure = listSessionsFailure,
            revokeSessionFailure = revokeSessionFailure,
            updateUserResponse = updateUserResponse
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
        var listSessionsFailure: Exception? = null,
        var revokeSessionFailure: Exception? = null,
        var updateUserResponse: UpdateUserResponse = UpdateUserResponse(),
        var registerResponse: LoginResponse = LoginResponse()
    ) : AuthApi {

        var lastRegisterRequest: RegisterRequest? = null
            private set

        var updateUserCalls: Int = 0
            private set

        var lastUpdateUserRequest: UpdateUserRequest? = null
            private set

        var revokeSessionCalls: Int = 0
            private set

        var lastRevokeSessionRequest: RevokeSessionRequest? = null
            private set

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
        ): LoginResponse {
            lastRegisterRequest = request
            return registerResponse
        }

        override suspend fun forgotPassword(
            request: ForgotPasswordRequest
        ): Unit = unused()

        override suspend fun changePassword(
            request: ChangePasswordRequest
        ): ChangePasswordResponse = unused()

        override suspend fun updateUser(
            request: UpdateUserRequest
        ): UpdateUserResponse {
            updateUserCalls++
            lastUpdateUserRequest = request

            return updateUserResponse
        }

        override suspend fun listSessions(): List<ActiveSession> {
            listSessionsCalls++

            listSessionsFailure?.let {
                throw it
            }

            return sessionsResponse
        }

        override suspend fun revokeSession(
            request: RevokeSessionRequest
        ): RevokeSessionResponse {
            revokeSessionCalls++
            lastRevokeSessionRequest = request

            revokeSessionFailure?.let {
                throw it
            }

            return RevokeSessionResponse(
                status = true
            )
        }

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

        var deleteByTokenCalls: Int = 0
            private set

        var lastDeletedToken: String? = null
            private set

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
            deleteByTokenCalls++
            lastDeletedToken = token

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