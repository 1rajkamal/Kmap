package com.breadcrumbradar.app.data.repository

import com.breadcrumbradar.app.data.local.BreadcrumbDao
import com.breadcrumbradar.app.data.model.BreadcrumbPoint
import com.breadcrumbradar.app.data.model.TrailSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class BreadcrumbRepository(private val breadcrumbDao: BreadcrumbDao) {

    val activeSessionFlow: Flow<TrailSession?> = breadcrumbDao.getActiveSessionFlow()
    val allSessionsFlow: Flow<List<TrailSession>> = breadcrumbDao.getAllSessionsFlow()

    fun getPointsForSessionFlow(sessionId: Long): Flow<List<BreadcrumbPoint>> =
        breadcrumbDao.getPointsForSessionFlow(sessionId)

    suspend fun getActiveSession(): TrailSession? = withContext(Dispatchers.IO) {
        breadcrumbDao.getActiveSession()
    }

    suspend fun startNewSession(title: String): TrailSession = withContext(Dispatchers.IO) {
        // Stop any previously active session
        val currentActive = breadcrumbDao.getActiveSession()
        if (currentActive != null) {
            breadcrumbDao.updateSession(currentActive.copy(isActive = false, endTime = System.currentTimeMillis()))
        }
        val newSession = TrailSession(title = title, startTime = System.currentTimeMillis(), isActive = true)
        val id = breadcrumbDao.insertSession(newSession)
        newSession.copy(id = id)
    }

    suspend fun pauseSession(sessionId: Long) = withContext(Dispatchers.IO) {
        val s = breadcrumbDao.getSessionById(sessionId) ?: return@withContext
        breadcrumbDao.updateSession(s.copy(isPaused = true))
    }

    suspend fun resumeSession(sessionId: Long) = withContext(Dispatchers.IO) {
        val s = breadcrumbDao.getSessionById(sessionId) ?: return@withContext
        breadcrumbDao.updateSession(s.copy(isPaused = false))
    }

    suspend fun stopSession(sessionId: Long) = withContext(Dispatchers.IO) {
        val s = breadcrumbDao.getSessionById(sessionId) ?: return@withContext
        breadcrumbDao.updateSession(s.copy(isActive = false, endTime = System.currentTimeMillis()))
    }

    suspend fun addPoint(point: BreadcrumbPoint) = withContext(Dispatchers.IO) {
        breadcrumbDao.insertPoint(point)
        // Update session point count
        val s = breadcrumbDao.getSessionById(point.sessionId)
        if (s != null) {
            breadcrumbDao.updateSession(s.copy(pointCount = s.pointCount + 1))
        }
    }

    suspend fun getPointsForSession(sessionId: Long): List<BreadcrumbPoint> = withContext(Dispatchers.IO) {
        breadcrumbDao.getPointsForSession(sessionId)
    }

    suspend fun clearAllTrailData() = withContext(Dispatchers.IO) {
        breadcrumbDao.deleteAllPoints()
        breadcrumbDao.deleteAllSessions()
    }
}
