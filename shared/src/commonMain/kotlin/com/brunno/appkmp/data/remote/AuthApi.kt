package com.brunno.appkmp.data.remote

import com.brunno.appkmp.data.remote.models.*
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path

interface AuthApi {

    @POST("api/auth/sign-in/email")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("api/auth/sign-up/email")
    suspend fun register(@Body request: RegisterRequest): LoginResponse

    @POST("api/auth/forget-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest)

    @POST("api/auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): ChangePasswordResponse

    @POST("api/auth/update-user")
    suspend fun updateUser(@Body request: UpdateUserRequest): UpdateUserResponse

    @GET("api/auth/list-sessions")
    suspend fun listSessions(): List<ActiveSession>

    @POST("api/auth/revoke-session")
    suspend fun revokeSession(@Body request: RevokeSessionRequest): RevokeSessionResponse

    @POST("api/auth/sign-out")
    suspend fun logout()

    @POST("api/avatar")
    suspend fun uploadAvatar(@Body request: AvatarUpdateRequest): AvatarUploadResponse

    @GET("api/avatar/{filename}")
    suspend fun getAvatar(@Path("filename") filename: String): ByteArray
}