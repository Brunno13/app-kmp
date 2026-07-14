package com.brunno.appkmp.domain.repository

import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<UserEntity?>

    suspend fun login(email: String, password: String): AppResult<Unit, AppError>

    suspend fun logout()
}