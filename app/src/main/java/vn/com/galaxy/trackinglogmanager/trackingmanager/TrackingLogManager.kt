package vn.com.galaxy.trackinglogmanager.trackingmanager

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import com.appsflyer.AFInAppEventParameterName
import com.appsflyer.AFInAppEventType
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerTrackingRequestListener
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.installations.FirebaseInstallations
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import vn.com.galaxy.trackinglogmanager.api.TrackingLogApiRepositoryImp
import vn.com.galaxy.trackinglogmanager.model.TrackingLogModel
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class TrackingLogManager @Inject constructor(
    var trackingLogRepository : TrackingLogRepository,
    var trackingLogApiCase : TrackingLogApiRepositoryImp
) {

    companion object {
        const val SESSION_TIMEOUT = 1000 * 60 * 60
        var MAX_NUM_COUNT = 5
    }

    var logModel: TrackingLogModel = TrackingLogModel()
    var userID : String? = ""
    var currentSessionID = -1L
    var context: Context ?= null
    var serverDeviceID: String = ""
    var sharedPreferences: SharedPreferences?=null

    /** Gọi 1 lần duy nhất tại onCreate MainActivity **/
    fun initUID(context: Context, host: String) {
        this.context = context
        sharedPreferences = context.getSharedPreferences(context.applicationContext.packageName.toString() + ".v2.playerprefs", Context.MODE_PRIVATE)
        if (currentSessionID > 0) {
            return
        }

        FirebaseInstallations.getInstance().id
            .addOnCompleteListener { task: Task<String?> ->
                try {
                    val id = task.result
                    logModel.FID = id
                } catch (ignored: Exception) {
                }
            }

        logModel.PackageName = context.packageName
        val deviceID: String = UUID.randomUUID().toString()
        logModel.DeviceID = deviceID
        val sessionID = System.currentTimeMillis()
        currentSessionID = sessionID
        logModel.SessionID = sessionID
        logModel.DeviceDensity = context.resources.displayMetrics.density.toString()
        logModel.Host = host
    }

    /** Lưu log với chuỗi json **/
    fun saveLog(json: String?) {
        try {
            val jsonParser = JsonParser()
            val jo = jsonParser.parse(json) as JsonObject

            if (jo.has("EventTime")){
                logModel.EventTime = jo["EventTime"].asLong
                jo.remove("EventTime")
            } else {
                logModel.EventTime = System.currentTimeMillis()
            }
            if (jo.has("device_id")){
                serverDeviceID = jo.get("device_id").asString
            } else {
                serverDeviceID = sharedPreferences?.getString("device_id", "")?:""
            }
            val eventCode = jo["EventCode"].asString
            jo.remove("EventCode")
            val code = jo["Code"].asInt
            jo.remove("Code")
            jo.addProperty("Code", code)
            val value = jo["Value"].asString
            jo.remove("Value")
            jo.addProperty("Value", value)
            if (jo.has("ChildID")) {
                if (logModel.ChildID == null || logModel.ChildID != jo["ChildID"].asString)
                    logModel.ChildID = jo["ChildID"].asString

                jo.remove("ChildID")
            }

            if (jo.has("user_id")) {
                userID = jo["user_id"].asString
                FirebaseCrashlytics.getInstance().setUserId(userID!!)

                jo.remove("user_id")
            }

            if (logModel.UserID == null || logModel.UserID != userID)
                logModel.UserID = userID

            Log.e("postEvent 1", eventCode)

            saveLog(eventCode = eventCode, params = jo)
        } catch (exception: Exception) {
            Log.e("qqTrackingLog", "saveLog: $logModel/nErr: $exception")
        }
    }

    /** Lưu log với parameter **/
    fun saveLog(
        code: String,
        category: String,
        action: String,
        event: String,
        value: String
    ) {
        val eventCode = category + "_" + action + "_" + event

        val jsonObject = JsonObject()
        jsonObject.addProperty("Code", code.toInt())
        jsonObject.addProperty("Category", category)
        jsonObject.addProperty("Action", action)
        jsonObject.addProperty("Event", event)
        jsonObject.addProperty("Value", value)

        saveLog(eventCode, jsonObject)
    }

    val mutex = Mutex()
    private fun saveLog(eventCode : String, params: JsonObject) {
        GlobalScope.launch {
            mutex.withLock {
                logModel.SessionID = currentSessionID
                logModel.EventCode = eventCode
                if (TextUtils.isEmpty(serverDeviceID)) {
                    serverDeviceID = sharedPreferences?.getString("device_id", "") ?: ""
                }
                params.addProperty("Server-Device-ID", serverDeviceID)
                logModel.Params = params.toString()
                if (logModel.EventTime <= 0L) {
                    logModel.EventTime = System.currentTimeMillis()
                }

                try {
                    trackingLogRepository.insertAll(logModel).also {
                        Log.d("qqTrackingLog", "saveLog Success: $logModel")
                    }
                } catch (exception: Exception) {
                    Log.e("qqTrackingLog", "saveLog: $logModel/nErr: $exception")
                }

                if (getTotalLog() >= MAX_NUM_COUNT) {
                    try {
                        val list = getListTrackingLog()
                        clearLog()
                        trackingLogApiCase.postEvent(list).also {
                            getTotalLog()
                        }
                    } catch (exception: Exception) {
                        Log.e("qqTrackingLog", "Cannot post event, pending... /nErr: $exception")
                    }
                }
            }
        }
    }

    var isHomeApp = false
    private fun onResume() {
        if (isHomeApp) {
            val currentTime = System.currentTimeMillis()
            val durationHomeApp = currentTime - timeAtHomeApp
            if (durationHomeApp >= SESSION_TIMEOUT) {
                val sessionID = System.currentTimeMillis()
                currentSessionID = sessionID
            }
        }
        isHomeApp = false

        startTime = System.currentTimeMillis()
    }

    /** Gọi hàm này để gửi log khi trờ về app từ ứng dụng khác/home app **/
    fun saveLogOnResume(
        code: String,
        category: String,
        action: String,
        event: String,
        value: String
    ) {
        onResume()

        saveLog(
            code= code,
            category= category,
            action= action,
            event= event,
            value= value
        )
    }

    private var startTime: Long = 0
    private var endTime: Long = 0
    private var totalTime: Long = 0
    private var timeAtHomeApp: Long = 0
    private fun onStop() {
        if (isHomeApp) {
            endTime = System.currentTimeMillis()
            val duration = endTime - startTime
            totalTime += duration

            timeAtHomeApp = System.currentTimeMillis()
        }

        startTime = System.currentTimeMillis()
    }

    /** Gọi hàm này để gửi log khi chuyển từ app sang home app **/
    fun saveLogOnStop(
        code: String,
        category: String,
        action: String,
        event: String,
        value: String? = totalTime.toString()
    ) {
        onStop()

        saveLog(
            code= code,
            category= category,
            action= action,
            event= event,
            value= value ?: ""
        )
    }

    fun logEventToApplyer(firebaseAnalytics: FirebaseAnalytics, jsonObject: JSONObject) {
        val bundle = Bundle()
        try {
            val evenCode = jsonObject.getString("EventCode")
            bundle.putString("EventCode", evenCode)
            val event = jsonObject.getString("Event")
            bundle.putString("Event", event)
            val category = jsonObject.getString("Category")
            bundle.putString("Category", category)
            val action = jsonObject.getString("Action")
            bundle.putString("Action", action)
            firebaseAnalytics.logEvent(evenCode, bundle)
            val hashMap = HashMap<String, Any>()
            when (evenCode) {
                "onboarding_firstchild_complete" -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[AFInAppEventParameterName.CUSTOMER_USER_ID] = userID
                    AppsFlyerLib.getInstance().setCustomerUserId(userID)
                    AppsFlyerLib.getInstance().trackEvent(
                        context?.applicationContext,
                        AFInAppEventType.COMPLETE_REGISTRATION,
                        hashMap, object :
                            AppsFlyerTrackingRequestListener{
                            override fun onTrackingRequestSuccess() {
                                Log.d("qqTrackingLog", "Appflyer send log success: onboarding_firstchild_complete")
                            }

                            override fun onTrackingRequestFailure(p0: String?) {
                                Log.e("qqTrackingLog", "Appflyer send log failed: onboarding_firstchild_complete")
                            }

                        })
                }
                "onboarding_info_login" -> if (jsonObject.has("Value")) {
                    val value = jsonObject.getString("Value")
                    val userID = jsonObject.getString("user_id")
                    hashMap[AFInAppEventParameterName.CUSTOMER_USER_ID] = userID
                    hashMap[AFInAppEventParameterName.CONTENT_TYPE] = value
                    AppsFlyerLib.getInstance().setCustomerUserId(userID)
                    AppsFlyerLib.getInstance()
                        .trackEvent(context?.applicationContext, AFInAppEventType.LOGIN, hashMap, object :
                            AppsFlyerTrackingRequestListener{
                            override fun onTrackingRequestSuccess() {
                                Log.d("qqTrackingLog", "Appflyer send log success: onboarding_info_login")
                            }

                            override fun onTrackingRequestFailure(p0: String?) {
                                Log.e("qqTrackingLog", "Appflyer send log failed: onboarding_info_login")
                            }

                        })
                }
                "af_open_app" -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[AFInAppEventParameterName.CUSTOMER_USER_ID] = userID
                    AppsFlyerLib.getInstance().setCustomerUserId(userID)
                    AppsFlyerLib.getInstance().trackEvent(context?.applicationContext, "af_open_app", hashMap, object :
                        AppsFlyerTrackingRequestListener{
                        override fun onTrackingRequestSuccess() {
                            Log.d("qqTrackingLog", "Appflyer send log success: af_open_app")
                        }

                        override fun onTrackingRequestFailure(p0: String?) {
                            Log.e("qqTrackingLog", "Appflyer send log failed: af_open_app")
                        }

                    })
                }
                "af_info_playApp" -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[AFInAppEventParameterName.CUSTOMER_USER_ID] = userID
                    AppsFlyerLib.getInstance().setCustomerUserId(userID)
                    AppsFlyerLib.getInstance()
                        .trackEvent(context?.applicationContext, "af_info_playApp", hashMap, object :
                            AppsFlyerTrackingRequestListener{
                            override fun onTrackingRequestSuccess() {
                                Log.d("qqTrackingLog", "Appflyer send log success: af_info_playApp")
                            }

                            override fun onTrackingRequestFailure(p0: String?) {
                                Log.e("qqTrackingLog", "Appflyer send log failed: af_info_playApp")
                            }

                        })
                }
                "af_info_userProfile" -> if (jsonObject.has("user_id")) {
                    val userID = jsonObject.getString("user_id")
                    hashMap[AFInAppEventParameterName.CUSTOMER_USER_ID] = userID
                    AppsFlyerLib.getInstance().setCustomerUserId(userID)
                    AppsFlyerLib.getInstance()
                        .trackEvent(context?.applicationContext, "af_info_userProfile", hashMap, object :
                            AppsFlyerTrackingRequestListener{
                            override fun onTrackingRequestSuccess() {
                                Log.d("qqTrackingLog", "Appflyer send log success: af_info_userProfile")
                            }

                            override fun onTrackingRequestFailure(p0: String?) {
                                Log.e("qqTrackingLog", "Appflyer send log failed: af_info_userProfile")
                            }

                        })
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun clearLog() {
        trackingLogRepository.deleteAll()
    }

    suspend fun getTotalLog(): Int {
        val totalLog : Int = trackingLogRepository.logCount().also {
            Log.e("qqTrackingLog", "Total log in cache: $it")
        }
        return totalLog
    }

    private suspend fun getListTrackingLog() : List<TrackingLogModel> {
        val listTrackingLog : ArrayList<TrackingLogModel> = arrayListOf()
        listTrackingLog.addAll(trackingLogRepository.allTrackingLog())
        return listTrackingLog
    }
}