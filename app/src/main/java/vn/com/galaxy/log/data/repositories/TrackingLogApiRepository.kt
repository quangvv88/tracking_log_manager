package vn.com.galaxy.log.data.repositories

import com.google.gson.JsonObject

interface TrackingLogApiRepository {
    fun initTrackingLog(
        appVersion: String,
        codeVersion: String,
        packageName: String,
        deviceID: String,
        sessionID: Long,
        density: String,
        host: String
    )

    fun setFirebaseId(firebaseId: String?)
    fun setUserId(userId: String?)
    fun setChildId(childId: String?)
    fun setEventTime(eventTime: Long)
    fun setIp(ip: String)
    fun setPlatform(platform: String)
    fun setCountry(country: String)

    fun getEventTime() : String
    fun getIP() : String

    suspend fun postEvent(eventCode: String, sessionID: Long, params: JsonObject)
}