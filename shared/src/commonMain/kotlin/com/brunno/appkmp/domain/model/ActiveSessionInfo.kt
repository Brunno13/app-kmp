package com.brunno.appkmp.domain.model

data class ActiveSessionInfo(
    val id: String,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val userId: String?
)
