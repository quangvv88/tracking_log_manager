package vn.com.galaxy.trackinglogmanager.trackingmanager

import vn.com.galaxy.trackinglogmanager.model.TrackingLogModel
import javax.inject.Inject

class TrackingLogRepository @Inject constructor (private val trackingLogDao: TrackingLogDao) {
    suspend fun allTrackingLog() : List<TrackingLogModel> = trackingLogDao.getAll()

    suspend fun logCount() : Int = trackingLogDao.getCount()

    suspend fun insertAll(vararg trackingLog: TrackingLogModel) {
        trackingLog.forEach {
            trackingLogDao.insertAll(it)
        }
    }

    suspend fun deleteAll() {
        trackingLogDao.deleteAll()
    }
}