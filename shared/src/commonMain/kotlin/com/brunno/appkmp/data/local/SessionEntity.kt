package com.brunno.appkmp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.brunno.appkmp.data.remote.models.ActiveSession

@Entity(tableName = "sessions")
data class SessionEntity(
    @PrimaryKey val token: String,
    val id: String,
    val expiresAt: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val ipAddress: String?,
    val userAgent: String?,
    val userId: String?
)

fun SessionEntity.toDomain() = ActiveSession(
    id = this.id,
    expiresAt = this.expiresAt,
    token = this.token,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
    ipAddress = this.ipAddress,
    userAgent = this.userAgent,
    userId = this.userId
)

fun ActiveSession.toEntity(): SessionEntity? {
    if (this.token == null || this.id == null) return null

    return SessionEntity(
        token = this.token,
        id = this.id,
        expiresAt = this.expiresAt,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        ipAddress = this.ipAddress,
        userAgent = this.userAgent,
        userId = this.userId
    )
}