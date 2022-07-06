package vn.com.galaxy.log.data.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import retrofit2.Response
import timber.log.Timber
import vn.com.galaxy.log.data.model.TrackingLogModel
import vn.com.galaxy.log.utils.TRACKING_LOG_TAG
import javax.inject.Inject

class TrackingLogRemoteSourceImpl @Inject constructor(val service: TrackingLogService) :
    TrackingLogRemoteSource {

    override suspend fun postEvent(
        listLogModel: List<TrackingLogModel>
    ): Flow<Result<Boolean>> =
        flow {
            service.postEvent(listTrackingLogModel = listLogModel).let { response ->
                Timber.tag(TRACKING_LOG_TAG).d("postEvent code: ${response.code()}")
                Timber.tag(TRACKING_LOG_TAG).d("postEvent message: ${response.message()}")
                Timber.tag(TRACKING_LOG_TAG).d("postEvent body: ${response.body()}")
                Timber.tag(TRACKING_LOG_TAG).d("postEvent raw: ${response.raw()}")
                Timber.tag(TRACKING_LOG_TAG).d("postEvent isSuccessful: ${response.isSuccessful}")
                Timber.tag(TRACKING_LOG_TAG).d("postEvent errorBody: ${response.errorBody()}")
                if (response.isSuccessful && response.code() == 200) {
                    emit(Result.success(true))
                } else {
                    emit(Result.success(false))
                }
            }
        }.catch { ex ->
            emit(Result.failure(ex))
        }.flowOn(Dispatchers.IO)
}