package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.LoginRequest
import com.brunno.appkmp.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val dao: UserDao
) : AuthRepository {

    override fun observeCurrentUser(): Flow<UserEntity?> {
        return dao.getAllUsers().map { users -> users.firstOrNull() }
    }

    override suspend fun login(email: String, passwordHash: String): Result<Unit> {
        return try {
            val response = api.login(LoginRequest(email, passwordHash))

            val user = UserEntity(
                name = response.name,
                email = email,
                token = response.token
            )

            dao.insertUser(user)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        dao.clearSession()
    }
}