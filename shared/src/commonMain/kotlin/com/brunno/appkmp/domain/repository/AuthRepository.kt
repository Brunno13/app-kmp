package com.brunno.appkmp.domain.repository

import com.brunno.appkmp.data.local.UserEntity
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeCurrentUser(): Flow<UserEntity?>

    suspend fun login(email: String, password: String): Result<Unit>

    suspend fun logout()
}