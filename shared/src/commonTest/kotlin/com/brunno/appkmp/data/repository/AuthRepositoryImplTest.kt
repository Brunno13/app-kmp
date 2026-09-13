package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.SessionEntity
import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.data.remote.models.AvatarUpdateRequest
import com.brunno.appkmp.data.remote.models.AvatarUploadResponse
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
import com.russhwolf.settings.MapSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AuthRepositoryImplTest {

    @Test
    fun currentTokenDefaultsToNull() {
        val repository = createRepository()

        assertNull(repository.getCurrentToken())
    }

    @Test
    fun currentTokenReturnsPersistedValue() {
        val settings = MapSettings().apply {
            putString("auth_token", "test-token")
        }

        val repository = createRepository(settings)

        assertEquals(
            "test-token",
            repository.getCurrentToken()
        )
    }

    @Test
    fun biometricDefaultsToFalse() {
        val repository = createRepository()

        assertFalse(repository.isBiometricEnabled())
    }

    @Test
    fun biometricSettingIsPersisted() {
        val settings = MapSettings()
        val repository = createRepository(settings)

        repository.setBiometricEnabled(true)

        assertTrue(repository.isBiometricEnabled())

        val recreatedRepository = createRepository(settings)

        assertTrue(recreatedRepository.isBiometricEnabled())

        recreatedRepository.setBiometricEnabled(false)

        assertFalse(recreatedRepository.isBiometricEnabled())
    }

    private fun createRepository(
        settings: MapSettings = MapSettings()
    ): AuthRepositoryImpl {
        return AuthRepositoryImpl(
            api = NoOpAuthApi(),
            dao = NoOpUserDao(),
            sessionDao = NoOpSessionDao(),
            settings = settings
        )
    }

    private class NoOpAuthApi : AuthApi {

        override suspend fun login(
            request: LoginRequest
        ): LoginResponse = unused()

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

    private class NoOpUserDao : UserDao {

        override suspend fun insertUser(user: UserEntity) {
            unused()
        }

        override fun getAllUsers(): Flow<List<UserEntity>> =
            flowOf(emptyList())

        override suspend fun clearSession() {
            unused()
        }
    }

    private class NoOpSessionDao : SessionDao {

        override suspend fun insertAll(
            sessions: List<SessionEntity>
        ) {
            unused()
        }

        override fun observeAllSessions(): Flow<List<SessionEntity>> =
            flowOf(emptyList())

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