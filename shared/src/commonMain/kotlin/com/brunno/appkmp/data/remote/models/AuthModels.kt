package com.brunno.appkmp.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
    val rememberMe: Boolean = true,
    val callbackURL: String = "/"
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val name: String
)

@Serializable
data class ForgotPasswordRequest(
    val email: String,
    val redirectTo: String = "/"
)

@Serializable
data class ChangePasswordRequest(
    val newPassword: String,
    val currentPassword: String,
    val revokeOtherSessions: Boolean = true
)

@Serializable
data class UpdateUserRequest(
    val name: String? = null,
    val image: String? = null
)

@Serializable
data class BetterAuthSessionInfo(
    val token: String? = null,
    val id: String? = null
)

@Serializable
data class LoginResponse(
    val token: String? = null,
    val session: BetterAuthSessionInfo? = null,
    val user: BetterAuthUser? = null,
    val message: String? = null
)

@Serializable
data class ChangePasswordResponse(
    val token: String? = null,
    val user: BetterAuthUser? = null
)

@Serializable
data class UpdateUserResponse(
    val user: BetterAuthUser? = null,
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val message: String? = null
)

@Serializable
data class BetterAuthUser(
    val id: String? = null,
    val name: String? = null,
    val email: String? = null,
    val emailVerified: Boolean? = null,
    val image: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val age: Int? = null,
    val role: String? = null
)

@Serializable
data class ActiveSession(
    val id: String? = null,
    val expiresAt: String? = null,
    val token: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val userId: String? = null
)

@Serializable
data class RevokeSessionRequest(
    val token: String
)

@Serializable
data class RevokeSessionResponse(
    val status: Boolean? = null
)