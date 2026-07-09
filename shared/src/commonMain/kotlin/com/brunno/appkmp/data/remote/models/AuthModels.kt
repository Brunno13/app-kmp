package com.brunno.appkmp.data.remote.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val passwordHash: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val userId: String,
    val name: String
)