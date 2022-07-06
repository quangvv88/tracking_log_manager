package vn.com.galaxy.log.presentation

import android.os.Bundle
import com.google.gson.JsonObject
import org.json.JSONObject
import java.util.HashMap

interface TrackingLogManager {
    fun init()
    fun setFirebaseId(firebaseId: String?)
    fun setUserId(userId: String?)
    fun setChildId(childId: String?)
    fun setServerDeviceId(deviceID: String)
    fun setCurrentSessionID(sessionID: Long)
    fun setIp(ip: String)
    fun setPlatform(platform: String)
    fun setCountry(country: String)

    fun getFirebaseId(): String
    fun getPlatform(): String
    fun getEventTime(): String
    fun getCurrentSessionID(): String
    fun getDeviceID(): String
    fun getIP(): String

    suspend fun logEvent(
        json: String,
        callback: (String) -> Unit,
        firebaseCallback: (String, JsonObject) -> Unit
    )

    suspend fun logEvent(
        code: String,
        category: String,
        action: String,
        event: String,
        value: String
    )

    fun logEventToAppsFlyer(
        jsonObject: JSONObject,
        logEventFirebase: (String, Bundle) -> Unit,
        trackEvent: (String, String, String, HashMap<String, Any>) -> Unit
    )
}