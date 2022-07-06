package vn.com.galaxy.log.presentation

import android.content.Context
import android.os.Bundle
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.json.JSONObject
import timber.log.Timber
import vn.com.galaxy.log.utils.Utils
import vn.com.galaxy.log.data.repositories.TrackingLogApiRepository
import vn.com.galaxy.log.utils.*
import java.lang.Exception
import java.util.*
import javax.inject.Inject

class TrackingLogManagerImpl @Inject constructor(
    val context: Context,
    val repository: TrackingLogApiRepository,
    val host: String
) : TrackingLogManager {

    private var currentSessionID = -1L
    private var serverDeviceID = ""
    private var firebaseId = ""
    private var platform = ""

    override fun init() {
        Timber.plant(Timber.DebugTree())
        currentSessionID = System.currentTimeMillis()
        repository.initTrackingLog(
            appVersion = Utils.getAppVersionName(context),
            codeVersion = Utils.getAppVersion(context).toString(),
            packageName = context.packageName,
            deviceID = UUID.randomUUID().toString(),
            sessionID = System.currentTimeMillis(),
            density = context.resources.displayMetrics.density.toString(),
            host = host
        )
    }

    override suspend fun logEvent(json: String, callback: (String) -> Unit, firebaseCallback: (String, JsonObject) -> Unit) {
        val jsonParser = JsonParser()
        val jo = jsonParser.parse(json) as JsonObject

        if (jo.has(PARAM_EVENT_TIME)) {
            repository.setEventTime(eventTime = jo[PARAM_EVENT_TIME].asLong)
            jo.remove(PARAM_EVENT_TIME)
        } else {
            repository.setEventTime(eventTime = System.currentTimeMillis())
        }
        serverDeviceID = if (jo.has(PARAM_SERVER_DEVICE_ID)) {
            jo.get(PARAM_SERVER_DEVICE_ID).asString
        } else {
            serverDeviceID
        }
        jo.addProperty(PARAM_SERVER_DEVICE_ID, serverDeviceID)

        val eventCode = jo[PARAM_EVENT_CODE].asString
        jo.remove(PARAM_EVENT_CODE)

        val code = jo[PARAM_CODE].asInt
        jo.remove(PARAM_CODE)
        jo.addProperty(PARAM_CODE, code)

        val value = jo[PARAM_VALUE].asString
        jo.remove(PARAM_VALUE)
        jo.addProperty(PARAM_VALUE, value)

        if (jo.has(PARAM_CHILD_ID)) {
            repository.setChildId(childId = jo[PARAM_CHILD_ID].asString)
            jo.remove(PARAM_CHILD_ID)
        }

        if (jo.has(PARAM_USER_ID)) {
            val userID = jo[PARAM_USER_ID].asString
            callback.invoke(userID)
            repository.setUserId(userId = userID)
            jo.remove(PARAM_USER_ID)
        }
        Timber.tag(TRACKING_LOG_TAG).i("post event code: $eventCode")
        repository.postEvent(
            eventCode = eventCode,
            sessionID = currentSessionID,
            params = jo
        )
        firebaseCallback.invoke(eventCode, jo)
    }

    override suspend fun logEvent(
        code: String, category: String, action: String, event: String, value: String
    ) {
        val jsonObject = JsonObject()
        jsonObject.addProperty(PARAM_CODE, code.toInt())
        jsonObject.addProperty(PARAM_CATEGORY, category)
        jsonObject.addProperty(PARAM_ACTION, action)
        jsonObject.addProperty(PARAM_EVENT, event)
        jsonObject.addProperty(PARAM_VALUE, value)
        jsonObject.addProperty(PARAM_SERVER_DEVICE_ID, serverDeviceID)
        repository.postEvent(
            eventCode = "${category}_${action}_${event}",
            sessionID = currentSessionID,
            params = jsonObject
        )
    }

    override fun setFirebaseId(firebaseId: String?) {
        repository.setFirebaseId(firebaseId = firebaseId)
        if (firebaseId != null) {
            this.firebaseId = firebaseId
        }
    }

    override fun setUserId(userId: String?) {
        repository.setUserId(userId = userId)
    }

    override fun setChildId(childId: String?) {
        repository.setChildId(childId = childId)
    }

    override fun setServerDeviceId(deviceID: String) {
        serverDeviceID = deviceID
    }

    override fun setCurrentSessionID(sessionID: Long) {
        currentSessionID = sessionID
    }

    override fun setIp(ip: String) {
        repository.setIp(ip = ip)
    }

    override fun setPlatform(platform: String) {
        repository.setPlatform(platform = platform)
        this.platform = platform
    }

    override fun setCountry(country: String) {
        repository.setCountry(country = country)
    }

    override fun getFirebaseId(): String {
        return firebaseId
    }

    override fun getPlatform(): String {
        return platform
    }

    override fun getDeviceID(): String {
        return serverDeviceID
    }

    override fun getEventTime(): String {
        return repository.getEventTime()
    }

    override fun getCurrentSessionID(): String {
        return currentSessionID.toString()
    }

    override fun getIP(): String {
        return repository.getIP()
    }

    override fun logEventToAppsFlyer(
        jsonObject: JSONObject,
        logEventFirebase: (String, Bundle) -> Unit,
        trackEvent: (String, String, String, HashMap<String, Any>) -> Unit
    ) {
        Timber.tag(TRACKING_LOG_TAG).i("Log event to Firebase and AppsFlyer start")
        try {
            val bundle = Bundle()
            val evenCode = jsonObject.getString(PARAM_EVENT_CODE)
            bundle.putString(PARAM_EVENT_CODE, evenCode)
            val event = jsonObject.getString(PARAM_EVENT)
            bundle.putString(PARAM_EVENT, event)
            val category = jsonObject.getString(PARAM_CATEGORY)
            bundle.putString(PARAM_CATEGORY, category)
            val action = jsonObject.getString(PARAM_ACTION)
            bundle.putString(PARAM_ACTION, action)
            logEventFirebase.invoke(evenCode, bundle)
            val hashMap = HashMap<String, Any>()
            when (evenCode) {
                EVENT_CODE_ONBOARDING_COMPLETE -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[PARAM_CUSTOMER_USER_ID] = userID
                    trackEvent.invoke(userID, evenCode, EVENT_CODE_COMPLETE_REGISTRATION, hashMap)
                }
                EVENT_CODE_ONBOARDING_LOGIN -> if (jsonObject.has("Value")) {
                    val value = jsonObject.getString("Value")
                    val userID = jsonObject.getString("user_id")
                    hashMap[PARAM_CUSTOMER_USER_ID] = userID
                    hashMap[PARAM_CONTENT_TYPE] = value
                    trackEvent.invoke(userID, evenCode, EVENT_CODE_LOGIN, hashMap)
                }
                EVENT_CODE_AF_OPEN_APP -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[PARAM_CUSTOMER_USER_ID] = userID
                    trackEvent.invoke(userID, evenCode, EVENT_CODE_AF_OPEN_APP, hashMap)
                }
                EVENT_CODE_AF_PLAY_APP -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[PARAM_CUSTOMER_USER_ID] = userID
                    trackEvent.invoke(userID, evenCode, EVENT_CODE_AF_PLAY_APP, hashMap)
                }
                EVENT_CODE_AF_USER_PROFILE -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[PARAM_CUSTOMER_USER_ID] = userID
                    trackEvent.invoke(userID, evenCode, EVENT_CODE_AF_USER_PROFILE, hashMap)
                }
            }
            Timber.tag(TRACKING_LOG_TAG).i("Log event to Firebase and AppsFlyer end")
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.tag(TRACKING_LOG_TAG).i("Log event to Firebase and AppsFlyer err: $e")
        }
    }
}