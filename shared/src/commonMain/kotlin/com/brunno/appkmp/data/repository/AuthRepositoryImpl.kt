package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.LoginRequest
import com.brunno.appkmp.domain.repository.AuthRepository
import com.russhwolf.settings.Settings
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

    override suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(email, password))

            if (response.user == null || response.token == null) {
                val errorMessage = response.message ?: "E-mail ou senha incorretos."
                return Result.failure(Exception(errorMessage))
            }
            settings.putString("auth_token", response.token)
            val user = UserEntity(
                name = response.user.name,
                email = response.user.email
            )

            dao.insertUser(user)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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