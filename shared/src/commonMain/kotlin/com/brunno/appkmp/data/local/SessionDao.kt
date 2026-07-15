package com.brunno.appkmp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<SessionEntity>)

    @Query("SELECT * FROM sessions")
    fun observeAllSessions(): Flow<List<SessionEntity>>

    @Query("DELETE FROM sessions WHERE token = :token")
    suspend fun deleteByToken(token: String)

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}