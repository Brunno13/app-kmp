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

    private fun createFixture(
        settings: MapSettings = MapSettings(),
        loginResponse: LoginResponse = LoginResponse()
    ): Fixture {
        val api = FakeAuthApi(
            loginResponse = loginResponse
        )

        val userDao = FakeUserDao()

        val repository = AuthRepositoryImpl(
            api = api,
            dao = userDao,
            sessionDao = NoOpSessionDao(),
            settings = settings
        )

        return Fixture(
            repository = repository,
            api = api,
            userDao = userDao,
            settings = settings
        )
    }

    private data class Fixture(
        val repository: AuthRepositoryImpl,
        val api: FakeAuthApi,
        val userDao: FakeUserDao,
        val settings: MapSettings
    )

    private class FakeAuthApi(
        var loginResponse: LoginResponse
    ) : AuthApi {

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

        override suspend fun listSessions(): List<ActiveSession> =
            unused()

        override suspend fun revokeSession(
            request: RevokeSessionRequest
        ): RevokeSessionResponse = unused()

        override suspend fun logout(): Unit = unused()

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

    private class NoOpSessionDao : SessionDao {

        override suspend fun insertAll(
            sessions: List<SessionEntity>
        ) {
            unused()
        }

        override fun observeAllSessions(): Flow<List<SessionEntity>> =
            MutableStateFlow(emptyList())

        override suspend fun deleteByToken(token: String) {
            unused()
        }

        override suspend fun clearAll() {
            unused()
        }
    }

    private companion object {
        fun unused(): Nothing =
            error("Unexpected call in test")
    }
}