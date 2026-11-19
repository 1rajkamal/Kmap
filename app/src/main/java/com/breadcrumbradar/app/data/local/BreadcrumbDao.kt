package com.breadcrumbradar.app.data.local

import androidx.room.*
import com.breadcrumbradar.app.data.model.BreadcrumbPoint
import com.breadcrumbradar.app.data.model.TrailSession
import kotlinx.coroutines.flow.Flow

@Dao
interface BreadcrumbDao {

    // Trail Sessions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: TrailSession): Long

    @Update
    suspend fun updateSession(session: TrailSession)

    @Query("SELECT * FROM trail_sessions WHERE id = :id LIMIT 1")
    suspend fun getSessionById(id: Long): TrailSession?

    @Query("SELECT * FROM trail_sessions WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getActiveSession(): TrailSession?

    @Query("SELECT * FROM trail_sessions WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun getActiveSessionFlow(): Flow<TrailSession?>

    @Query("SELECT * FROM trail_sessions ORDER BY startTime DESC")
    fun getAllSessionsFlow(): Flow<List<TrailSession>>

    @Query("DELETE FROM trail_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Long)

    @Query("DELETE FROM trail_sessions")
    suspend fun deleteAllSessions()

    // Breadcrumb Points
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoint(point: BreadcrumbPoint): Long

    @Query("SELECT * FROM breadcrumb_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getPointsForSessionFlow(sessionId: Long): Flow<List<BreadcrumbPoint>>

    @Query("SELECT * FROM breadcrumb_points WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getPointsForSession(sessionId: Long): List<BreadcrumbPoint>

    @Query("SELECT * FROM breadcrumb_points ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentPointsFlow(limit: Int = 100): Flow<List<BreadcrumbPoint>>

    @Query("DELETE FROM breadcrumb_points WHERE sessionId = :sessionId")
    suspend fun deletePointsForSession(sessionId: Long)

    @Query("DELETE FROM breadcrumb_points")
    suspend fun deleteAllPoints()
}
