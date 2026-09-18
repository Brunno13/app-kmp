package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.AuthCredentialStore
import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.SessionEntity
import com.brunno.appkmp.data.local.toDomain
import com.brunno.appkmp.data.local.toEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.data.remote.models.ChangePasswordRequest
import com.brunno.appkmp.data.remote.models.RevokeSessionRequest
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.model.ActiveSessionInfo
import com.brunno.appkmp.domain.repository.SecurityRepository
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SecurityRepositoryImpl(
    private val api: AuthApi,
    private val sessionDao: SessionDao,
    private val settings: Settings,
    private val credentialStore: AuthCredentialStore
) : SecurityRepository {

    companion object {
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

    override fun isCurrentSession(
        sessionId: String
    ): Boolean {
        val currentToken =
            credentialStore.getAuthToken()

        val sessionToken =
            credentialStore.getSessionToken(
                sessionId
            )

        return currentToken != null &&
            sessionToken != null &&
            currentToken == sessionToken
    }

    override fun observeActiveSessions():
            Flow<List<ActiveSessionInfo>> {
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
            val remoteSessions =
                api.listSessions()

            val synchronizedSessions =
                remoteSessions.mapNotNull {
                    session ->
                    session.toSynchronizedSession()
                }

            credentialStore.replaceSessionTokens(
                emptyMap()
            )

            sessionDao.clearAll()

            sessionDao.insertAll(
                synchronizedSessions.map {
                    it.entity
                }
            )

            credentialStore.replaceSessionTokens(
                synchronizedSessions.associate {
                    synchronized ->
                    synchronized.sessionId to
                        synchronized.token
                }
            )

            AppResult.Success(Unit)
        }
    }

    override suspend fun revokeSession(
        sessionId: String
    ): AppResult<Unit, AppError> {
        val token =
            credentialStore.getSessionToken(
                sessionId
            ) ?: return AppResult.Error(
                AuthError.UNAUTHORIZED
            )

        return executeRepositoryCall {
            api.revokeSession(
                RevokeSessionRequest(token)
            )

            credentialStore.removeSessionToken(
                sessionId
            )

            sessionDao.deleteById(
                sessionId
            )

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

private data class SynchronizedSession(
    val sessionId: String,
    val token: String,
    val entity: SessionEntity
)

private fun ActiveSession.toSynchronizedSession():
        SynchronizedSession? {
    val actualSessionId = id
    val actualToken = token
    val sessionEntity = toEntity()

    return if (
        actualSessionId != null &&
        actualToken != null &&
        sessionEntity != null
    ) {
        SynchronizedSession(
            sessionId = actualSessionId,
            token = actualToken,
            entity = sessionEntity
        )
    } else {
        null
    }
}
