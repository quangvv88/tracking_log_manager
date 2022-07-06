package vn.com.galaxy.log.data.data.local

import kotlinx.coroutines.flow.Flow
import vn.com.galaxy.log.data.model.TrackingLogModel

interface TrackingLogLocalSource {
    suspend fun allTrackingLog(): Flow<Result<List<TrackingLogModel>>>
    suspend fun countLog(): Int
    suspend fun insertAllLog(vararg trackingLog: TrackingLogModel)
    suspend fun deleteAllLog()
}