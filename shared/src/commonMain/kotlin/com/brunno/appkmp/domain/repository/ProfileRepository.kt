package com.brunno.appkmp.domain.repository

import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeCurrentUser(): Flow<UserEntity?>

    suspend fun updateUser(
        name: String
    ): AppResult<Unit, AppError>

    suspend fun updateAvatar(
        base64: String,
        fileName: String,
        mimeType: String
    ): AppResult<Unit, AppError>

    suspend fun syncAvatar(filename: String)
}
