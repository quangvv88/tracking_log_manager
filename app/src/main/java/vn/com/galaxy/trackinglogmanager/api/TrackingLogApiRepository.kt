package vn.com.galaxy.trackinglogmanager.api

import vn.com.galaxy.trackinglogmanager.model.TrackingLogModel

interface TrackingLogApiRepository {
    suspend fun postEvent(listLogModel: List<TrackingLogModel>)
}