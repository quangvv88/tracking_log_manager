package vn.com.galaxy.trackinglogmanager.api

import android.util.Log
import vn.com.galaxy.trackinglogmanager.api.TrackingLogService
import vn.com.galaxy.trackinglogmanager.model.TrackingLogModel
import javax.inject.Inject

class TrackingLogApiRepositoryImp @Inject constructor(val trackingLogService: TrackingLogService) :
    TrackingLogApiRepository {
    override suspend fun postEvent(listLogModel: List<TrackingLogModel>) {
        trackingLogService.postEvent(listLogModel).let { response ->
            if (response.isSuccessful) {
                Log.d("qqTrackingLog", "postEvent Success: $listLogModel")
            } else {
                Log.d("qqTrackingLog", "postEvent Failed: $response + " + response.errorBody())
            }
        }
    }
}