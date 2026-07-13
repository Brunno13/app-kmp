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
data class LoginResponse(
    val token: String? = null,
    val user: BetterAuthUser? = null,
    val message: String? = null
)

@Serializable
data class BetterAuthUser(
    val id: String,
    val name: String,
    val email: String,
    val image: String? = null,
    val role: String? = null
)