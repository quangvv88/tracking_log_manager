package vn.com.galaxy.trackinglogmanager.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import vn.com.galaxy.trackinglogmanager.model.TrackingLogModel

interface TrackingLogService {
    @Headers("Accept: */*", "Content-type: application/json")
    @POST("api.v0")
    suspend fun postEvent(@Body listLogModel: List<TrackingLogModel>) : Response<Unit>
}