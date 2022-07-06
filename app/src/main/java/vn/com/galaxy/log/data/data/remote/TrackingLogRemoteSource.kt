package vn.com.galaxy.log.data.data.remote

import kotlinx.coroutines.flow.Flow
import vn.com.galaxy.log.data.model.TrackingLogModel

interface TrackingLogRemoteSource {
    suspend fun postEvent(listLogModel: List<TrackingLogModel>): Flow<Result<Boolean>>
}