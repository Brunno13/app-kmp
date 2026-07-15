package com.brunno.appkmp.domain.repository

import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<UserEntity?>
    fun getCurrentToken(): String?
    suspend fun login(email: String, password: String): AppResult<Unit, AppError>
    suspend fun register(name: String, email: String, password: String): AppResult<Unit, AppError>
    suspend fun forgotPassword(email: String): AppResult<Unit, AppError>
    suspend fun changePassword(currentPassword: String, newPassword: String): AppResult<Unit, AppError>
    suspend fun updateUser(name: String): AppResult<Unit, AppError>
    suspend fun getActiveSessions(): AppResult<List<ActiveSession>, AppError>
    suspend fun revokeSession(token: String): AppResult<Unit, AppError>
    suspend fun logout()
}