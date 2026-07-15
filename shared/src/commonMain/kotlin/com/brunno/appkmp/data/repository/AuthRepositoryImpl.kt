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

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val dao: UserDao,
    private val sessionDao: SessionDao,
    private val settings: Settings
) : AuthRepository {

    companion object {
        private const val PREF_AUTH_TOKEN = "auth_token"
    }

    private class InvalidSessionException : Exception()

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
            settings.remove(PREF_AUTH_TOKEN)
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

    private suspend fun saveSession(response: LoginResponse) {
        val actualToken = response.token

        if (response.user == null || actualToken == null) {
            throw InvalidSessionException()
        }

        settings.putString(PREF_AUTH_TOKEN, actualToken)

        val user = UserEntity(
            name = response.user.name ?: "",
            email = response.user.email ?: ""
        )

        dao.insertUser(user)
    }
}