package vn.com.galaxy.log.data.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import vn.com.galaxy.log.data.model.TrackingLogModel
import javax.inject.Inject

class TrackingLogLocalSourceImpl @Inject constructor(val database: TrackingLogDatabase) :
    TrackingLogLocalSource {

    override suspend fun allTrackingLog(): Flow<Result<List<TrackingLogModel>>> = flow {
        emit(Result.success(database.trackingLogDao().getAll()))
    }.catch { ex ->
        emit(Result.failure(ex))
    }.flowOn(Dispatchers.IO)

    override suspend fun countLog(): Int = database.trackingLogDao().getCount()

    override suspend fun insertAllLog(vararg trackingLog: TrackingLogModel) {
        trackingLog.forEach {
            database.trackingLogDao().insertAll(it)
        }
    }

    override suspend fun deleteAllLog() {
        database.trackingLogDao().deleteAll()
    }
}