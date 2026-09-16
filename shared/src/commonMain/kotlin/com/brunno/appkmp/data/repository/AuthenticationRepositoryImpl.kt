package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.SessionDao
import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.ForgotPasswordRequest
import com.brunno.appkmp.data.remote.models.LoginRequest
import com.brunno.appkmp.data.remote.models.LoginResponse
import com.brunno.appkmp.data.remote.models.RegisterRequest
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.error.AuthError
import com.brunno.appkmp.domain.repository.AuthenticationRepository
import com.russhwolf.settings.Settings
import kotlin.coroutines.cancellation.CancellationException

class AuthenticationRepositoryImpl(
    private val api: AuthApi,
    private val dao: UserDao,
    private val sessionDao: SessionDao,
    private val settings: Settings
) : AuthenticationRepository {

    companion object {
        private const val PREF_AUTH_TOKEN = "auth_token"
    }

    private class InvalidSessionException : Exception()

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<Unit, AppError> {
        return executeAuthenticationCall {
            val response = api.login(
                LoginRequest(
                    email = email,
                    password = password
                )
            )

            saveSession(response)
            AppResult.Success(Unit)
        }
    }

    override suspend fun register(
        name: String,
        email: String,
        password: String
    ): AppResult<Unit, AppError> {
        return executeAuthenticationCall {
            val response = api.register(
                RegisterRequest(
                    email = email,
                    password = password,
                    name = name
                )
            )

            saveSession(response)
            AppResult.Success(Unit)
        }
    }

    override suspend fun forgotPassword(
        email: String
    ): AppResult<Unit, AppError> {
        return executeRepositoryCall {
            api.forgotPassword(
                ForgotPasswordRequest(email)
            )

            AppResult.Success(Unit)
        }
    }

    override suspend fun logout() {
        try {
            api.logout()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (_: Exception) {
            // A sessão local deve ser limpa mesmo se o logout remoto falhar.
        } finally {
            dao.clearSession()
            sessionDao.clearAll()
            settings.clear()
        }
    }

    private suspend fun saveSession(
        response: LoginResponse
    ) {
        val actualToken = response.token

        if (response.user == null || actualToken == null) {
            throw InvalidSessionException()
        }

        settings.putString(
            PREF_AUTH_TOKEN,
            actualToken
        )

        dao.clearSession()

        val user = UserEntity(
            name = response.user.name ?: "",
            email = response.user.email ?: "",
            avatarFilename = response.user.image
                ?.substringAfterLast("/"),
            avatarData = null
        )

        dao.insertUser(user)
    }

    private suspend fun <T> executeAuthenticationCall(
        block: suspend () -> AppResult<T, AppError>
    ): AppResult<T, AppError> {
        return executeRepositoryCall(
            exceptionMapper = { failure ->
                if (failure is InvalidSessionException) {
                    AuthError.UNAUTHORIZED
                } else {
                    null
                }
            },
            block = block
        )
    }
}
