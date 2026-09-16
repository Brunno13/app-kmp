package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.toDomain
import com.brunno.appkmp.data.local.toEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.data.remote.models.ChangePasswordRequest
import com.brunno.appkmp.data.remote.models.RevokeSessionRequest
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.repository.SecurityRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SecurityRepositoryImpl(
    private val api: AuthApi,
    private val sessionDao: SessionDao,
    private val settings: Settings
) : SecurityRepository {

    companion object {
        private const val PREF_AUTH_TOKEN = "auth_token"
        private const val PREF_BIOMETRIC_ENABLED =
            "biometric_enabled"
    }

    override fun isBiometricEnabled(): Boolean {
        return settings.getBoolean(
            PREF_BIOMETRIC_ENABLED,
            false
        )
    }

    override fun setBiometricEnabled(
        enabled: Boolean
    ) {
        settings.putBoolean(
            PREF_BIOMETRIC_ENABLED,
            enabled
        )
    }

    override fun getCurrentToken(): String? {
        return settings.getStringOrNull(
            PREF_AUTH_TOKEN
        )
    }

    override fun observeActiveSessions():
            Flow<List<ActiveSession>> {
        return sessionDao
            .observeAllSessions()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun syncActiveSessions():
            AppResult<Unit, AppError> {
        return executeRepositoryCall {
            val sessions = api.listSessions()

            sessionDao.clearAll()

            sessionDao.insertAll(
                sessions.mapNotNull { session ->
                    session.toEntity()
                }
            )

            AppResult.Success(Unit)
        }
    }

    override suspend fun revokeSession(
        token: String
    ): AppResult<Unit, AppError> {
        return executeRepositoryCall {
            api.revokeSession(
                RevokeSessionRequest(token)
            )

            sessionDao.deleteByToken(token)

            AppResult.Success(Unit)
        }
    }

    override suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): AppResult<Unit, AppError> {
        return executeRepositoryCall {
            api.changePassword(
                ChangePasswordRequest(
                    newPassword,
                    currentPassword
                )
            )

            AppResult.Success(Unit)
        }
    }
}
