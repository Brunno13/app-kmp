package com.brunno.appkmp.domain.repository

import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import kotlinx.coroutines.flow.Flow

interface SecurityRepository {
    fun isBiometricEnabled(): Boolean

    fun setBiometricEnabled(enabled: Boolean)

    fun getCurrentToken(): String?

    fun observeActiveSessions(): Flow<List<ActiveSession>>

    suspend fun syncActiveSessions(): AppResult<Unit, AppError>

    suspend fun revokeSession(
        token: String
    ): AppResult<Unit, AppError>

    suspend fun changePassword(
        currentPassword: String,
        newPassword: String
    ): AppResult<Unit, AppError>
}
