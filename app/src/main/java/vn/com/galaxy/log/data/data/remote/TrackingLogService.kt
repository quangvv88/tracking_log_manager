package vn.com.galaxy.log.data.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import vn.com.galaxy.log.data.model.TrackingLogModel

interface TrackingLogService {
    @Headers("Accept: */*", "Content-type: application/json")
    @POST("api.v0")
    suspend fun postEvent(@Body listTrackingLogModel: List<TrackingLogModel>): Response<Unit>
}