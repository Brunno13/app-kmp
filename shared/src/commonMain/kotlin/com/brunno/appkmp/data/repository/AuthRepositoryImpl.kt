package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.local.toDomain
import com.brunno.appkmp.data.local.toEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.*
import com.brunno.appkmp.data.remote.parseNetworkError
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.repository.AuthRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val dao: UserDao,
    private val sessionDao: SessionDao,
    private val settings: Settings
) : AuthRepository {

    companion object {
        private const val PREF_AUTH_TOKEN = "auth_token"
        private const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"
    }

    private class InvalidSessionException : Exception()

    fun isBiometricEnabled(): Boolean {
        return settings.getBoolean(PREF_BIOMETRIC_ENABLED, false)
    }

    fun setBiometricEnabled(enabled: Boolean) {
        settings.putBoolean(PREF_BIOMETRIC_ENABLED, enabled)
    }

    override fun observeCurrentUser(): Flow<UserEntity?> {
        return dao.getAllUsers().map { users -> users.firstOrNull() }
    }

    override fun getCurrentToken(): String? {
        return settings.getStringOrNull(PREF_AUTH_TOKEN)
    }

    override fun observeActiveSessions(): Flow<List<ActiveSession>> {
        return sessionDao.observeAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun syncActiveSessions(): AppResult<Unit, AppError> {
        return try {
            val sessions = api.listSessions()
            sessionDao.clearAll()
            sessionDao.insertAll(sessions.mapNotNull { it.toEntity() })
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(parseNetworkError(e))
        }
    }

    override suspend fun revokeSession(token: String): AppResult<Unit, AppError> {
        return try {
            api.revokeSession(RevokeSessionRequest(token))
            sessionDao.deleteByToken(token)
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(parseNetworkError(e))
        }
    }

    override suspend fun logout() {
        try {
            api.logout()
        } catch (e: Exception) {
            // Ignorado em caso de falha de rede
        } finally {
            dao.clearSession()
            sessionDao.clearAll()
            settings.clear()
        }
    }

    override suspend fun login(email: String, password: String): AppResult<Unit, AppError> {
        return try {
            val response = api.login(LoginRequest(email, password))
            saveSession(response)
            AppResult.Success(Unit)
        } catch (e: InvalidSessionException) {
            AppResult.Error(AuthError.UNAUTHORIZED)
        } catch (e: Exception) {
            AppResult.Error(parseNetworkError(e))
        }
    }

    override suspend fun register(name: String, email: String, password: String): AppResult<Unit, AppError> {
        return try {
            val response = api.register(RegisterRequest(email = email, password = password, name = name))
            saveSession(response)
            AppResult.Success(Unit)
        } catch (e: InvalidSessionException) {
            AppResult.Error(AuthError.UNAUTHORIZED)
        } catch (e: Exception) {
            AppResult.Error(parseNetworkError(e))
        }
    }

    override suspend fun forgotPassword(email: String): AppResult<Unit, AppError> {
        return try {
            api.forgotPassword(ForgotPasswordRequest(email))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(parseNetworkError(e))
        }
    }

    override suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit, AppError> {
        return try {
            api.changePassword(ChangePasswordRequest(newPassword, currentPassword))
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(parseNetworkError(e))
        }
    }

    override suspend fun updateUser(name: String): AppResult<Unit, AppError> {
        return try {
            val response = api.updateUser(UpdateUserRequest(name = name))
            val currentUser = dao.getAllUsers().firstOrNull()?.firstOrNull()

            if (currentUser != null) {
                val newName = response.user?.name ?: response.name ?: name
                val newEmail = response.user?.email ?: response.email ?: currentUser.email
                dao.insertUser(currentUser.copy(name = newName, email = newEmail))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Error(parseNetworkError(e))
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun updateAvatar(base64: String, fileName: String, mimeType: String): AppResult<Unit, AppError> {
        return try {
            val uploadResponse = api.uploadAvatar(AvatarUpdateRequest(base64, fileName, mimeType))

            api.updateUser(UpdateUserRequest(image = uploadResponse.url))

            val safeFilename = uploadResponse.url.substringAfterLast("/")

            val currentUser = dao.getAllUsers().firstOrNull()?.firstOrNull()
            if (currentUser != null) {
                dao.insertUser(currentUser.copy(
                    avatarData = base64,
                    avatarFilename = safeFilename
                ))
            }
            AppResult.Success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            AppResult.Error(parseNetworkError(e))
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun syncAvatar(filename: String) {
        try {
            val safeFilename = filename.substringAfterLast("/")

            val bytes = api.getAvatar(safeFilename)
            val remoteBase64 = Base64.encode(bytes)

            val currentUser = dao.getAllUsers().firstOrNull()?.firstOrNull()
            if (currentUser != null && currentUser.avatarData != remoteBase64) {
                dao.insertUser(currentUser.copy(
                    avatarData = remoteBase64,
                    avatarFilename = safeFilename
                ))
            }
        } catch (e: Exception) {
            println("Erro no syncAvatar: ${e.message}")
        }
    }

    private suspend fun saveSession(response: LoginResponse) {
        val actualToken = response.token

        if (response.user == null || actualToken == null) {
            throw InvalidSessionException()
        }

        settings.putString(PREF_AUTH_TOKEN, actualToken)
        dao.clearSession()

        val user = UserEntity(
            name = response.user.name ?: "",
            email = response.user.email ?: "",
            avatarFilename = response.user.image?.substringAfterLast("/"),
            avatarData = null
        )

        dao.insertUser(user)
    }
}