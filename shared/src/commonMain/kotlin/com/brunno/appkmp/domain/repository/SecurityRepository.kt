package com.brunno.appkmp.domain.repository

import com.brunno.appkmp.domain.model.ActiveSessionInfo
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    fun isBiometricEnabled(): Boolean

    fun setBiometricEnabled(enabled: Boolean)

    fun isCurrentSession(
        sessionId: String
    ): Boolean

    fun observeActiveSessions():
        Flow<List<ActiveSessionInfo>>

    suspend fun syncActiveSessions(): AppResult<Unit, AppError>

    suspend fun revokeSession(
        sessionId: String
    ): AppResult<Unit, AppError>

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): AppResult<Unit, AppError>
}
