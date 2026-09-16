package com.brunno.appkmp.domain.repository

import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult

interface AuthenticationRepository {
    suspend fun login(
        email: String,
        password: String
    ): AppResult<Unit, AppError>

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): AppResult<Unit, AppError>

    suspend fun forgotPassword(
        email: String
    ): AppResult<Unit, AppError>

    suspend fun logout()
}
