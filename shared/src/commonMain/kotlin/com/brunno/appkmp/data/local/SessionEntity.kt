package com.brunno.appkmp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brunno.appkmp.data.remote.models.ActiveSession
import com.brunno.appkmp.domain.model.ActiveSessionInfo

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val id: String,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val userId: String?
)

fun SessionEntity.toDomain() = ActiveSessionInfo(
    id = id,
    expiresAt = expiresAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    ipAddress = ipAddress,
    userAgent = userAgent,
    userId = userId
)

fun ActiveSession.toEntity(): SessionEntity? {
    val sessionId = id ?: return null

    return SessionEntity(
        id = sessionId,
        expiresAt = expiresAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        ipAddress = ipAddress,
        userAgent = userAgent,
        userId = userId
    )
}
