package com.brunno.appkmp.data.repository

import com.brunno.appkmp.data.local.UserDao
import com.brunno.appkmp.data.local.UserEntity
import com.brunno.appkmp.data.remote.AuthApi
import com.brunno.appkmp.data.remote.models.AvatarUpdateRequest
import com.brunno.appkmp.data.remote.models.UpdateUserRequest
import com.brunno.appkmp.domain.error.AppError
import com.brunno.appkmp.domain.error.AppResult
import com.brunno.appkmp.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class ProfileRepositoryImpl(
    private val api: AuthApi,
    private val dao: UserDao
) : ProfileRepository {

    override fun observeCurrentUser(): Flow<UserEntity?> {
        return dao.getAllUsers().map { users ->
            users.firstOrNull()
        }
    }

    override suspend fun updateUser(
        name: String
    ): AppResult<Unit, AppError> {
        return executeRepositoryCall {
            val response = api.updateUser(
                UpdateUserRequest(name = name)
            )

            val currentUser = dao
                .getAllUsers()
                .firstOrNull()
                ?.firstOrNull()

            if (currentUser != null) {
                val newName =
                    response.user?.name
                        ?: response.name
                        ?: name

                val newEmail =
                    response.user?.email
                        ?: response.email
                        ?: currentUser.email

                dao.insertUser(
                    currentUser.copy(
                        name = newName,
                        email = newEmail
                    )
                )
            }

            AppResult.Success(Unit)
        }
    }

    override suspend fun updateAvatar(
        base64: String,
        fileName: String,
        mimeType: String
    ): AppResult<Unit, AppError> {
        return executeRepositoryCall {
            val uploadResponse = api.uploadAvatar(
                AvatarUpdateRequest(
                    avatarBase64 = base64,
                    fileName = fileName,
                    mimeType = mimeType
                )
            )

            api.updateUser(
                UpdateUserRequest(
                    image = uploadResponse.url
                )
            )

            val safeFilename =
                uploadResponse.url.substringAfterLast("/")

            val currentUser = dao
                .getAllUsers()
                .firstOrNull()
                ?.firstOrNull()

            if (currentUser != null) {
                dao.insertUser(
                    currentUser.copy(
                        avatarData = base64,
                        avatarFilename = safeFilename
                    )
                )
            }

            AppResult.Success(Unit)
        }
    }

    @OptIn(ExperimentalEncodingApi::class)
    override suspend fun syncAvatar(
        filename: String
    ) {
        try {
            val safeFilename =
                filename.substringAfterLast("/")

            val bytes = api.getAvatar(safeFilename)
            val remoteBase64 = Base64.encode(bytes)

            val currentUser = dao
                .getAllUsers()
                .firstOrNull()
                ?.firstOrNull()

            if (
                currentUser != null &&
                currentUser.avatarData != remoteBase64
            ) {
                dao.insertUser(
                    currentUser.copy(
                        avatarData = remoteBase64,
                        avatarFilename = safeFilename
                    )
                )
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (expectedFailure: Exception) {
            println(
                "Erro no syncAvatar: ${expectedFailure.message}"
            )
        }
    }
}
