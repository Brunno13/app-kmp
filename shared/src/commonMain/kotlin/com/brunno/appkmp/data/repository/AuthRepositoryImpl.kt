package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.LoginRequest
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.error.NetworkError
import com.brunno.appkmp.domain.repository.AuthRepository
import com.russhwolf.settings.Settings
import io.ktor.client.plugins.ClientRequestException
import io.ktor.utils.io.errors.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val dao: UserDao,
    private val settings: Settings
) : AuthRepository {

    override fun observeCurrentUser(): Flow<UserEntity?> {
        return dao.getAllUsers().map { users -> users.firstOrNull() }
    }

    override suspend fun login(email: String, password: String): AppResult<Unit, AppError> {
        return try {
            val response = api.login(LoginRequest(email, password))

            if (response.user == null || response.token == null) {
                return AppResult.Error(AuthError.INVALID_CREDENTIALS)
            }

            settings.putString("auth_token", response.token)

            val user = UserEntity(
                name = response.user.name,
                email = response.user.email
            )

            dao.insertUser(user)

            AppResult.Success(Unit)

        } catch (e: Exception) {
            val networkError = when (e) {
                is IOException -> NetworkError.NO_INTERNET
                is ClientRequestException -> {
                    if (e.response.status.value == 401 || e.response.status.value == 403) {
                        return AppResult.Error(AuthError.UNAUTHORIZED)
                    }
                    NetworkError.SERVER_ERROR
                }
                else -> NetworkError.UNKNOWN
            }

            AppResult.Error(networkError)
        }
    }

    override suspend fun logout() {
        dao.clearSession()
        settings.remove("auth_token")
    }

    fun getToken(): String? {
        return settings.getStringOrNull("auth_token")
    }
}