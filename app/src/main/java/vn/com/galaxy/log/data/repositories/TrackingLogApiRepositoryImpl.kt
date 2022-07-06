package vn.com.galaxy.log.data.repositories

import com.google.gson.JsonObject
import kotlinx.coroutines.flow.*
import timber.log.Timber
import vn.com.galaxy.log.data.data.local.TrackingLogLocalSource
import vn.com.galaxy.log.data.model.TrackingLogModel
import vn.com.galaxy.log.data.data.remote.TrackingLogRemoteSource
import vn.com.galaxy.log.utils.MAX_NUM_COUNT
import vn.com.galaxy.log.utils.TRACKING_LOG_TAG
import java.lang.Exception
import javax.inject.Inject
import kotlin.math.abs

class TrackingLogApiRepositoryImpl @Inject constructor(
    private val localSource: TrackingLogLocalSource,
    private val remoteSource: TrackingLogRemoteSource
) : TrackingLogApiRepository {

    private lateinit var trackingLog: TrackingLogModel

    private var lastTimeSuccessPostEven = 0L

    override fun initTrackingLog(
        appVersion: String,
        codeVersion: String,
        packageName: String,
        deviceID: String,
        sessionID: Long,
        density: String,
        host: String
    ) {
        Timber.tag(TRACKING_LOG_TAG).i("Init tracking log start!")
        trackingLog = TrackingLogModel()
        trackingLog.VersionApp = appVersion
        trackingLog.VersionCode = codeVersion
        trackingLog.PackageName = packageName
        trackingLog.DeviceID = deviceID
        trackingLog.SessionID = sessionID
        trackingLog.DeviceDensity = density
        trackingLog.Host = host
        Timber.tag(TRACKING_LOG_TAG).i("Init tracking log end!")
    }

    override fun setFirebaseId(firebaseId: String?) {
        firebaseId?.let {
            trackingLog.FID = it
        }
    }

    override fun setUserId(userId: String?) {
        userId.let {
            trackingLog.UserID = it
        }
    }

    override fun setChildId(childId: String?) {
        childId.let {
            trackingLog.ChildID = it
        }
    }

    override fun setEventTime(eventTime: Long) {
        trackingLog.EventTime = eventTime
    }

    override fun setIp(ip: String) {
        trackingLog.IP = ip
    }

    override fun getIP() : String {
        return trackingLog.IP ?: ""
    }

    override fun getEventTime(): String {
        return trackingLog.EventTime.toString()
    }

    override fun setPlatform(platform: String) {
        trackingLog.Platform = platform
    }

    override fun setCountry(country: String) {
        trackingLog.Country = country
    }

    override suspend fun postEvent(
        eventCode: String,
        sessionID: Long,
        params: JsonObject
    ) {
        trackingLog.SessionID = sessionID
        trackingLog.EventCode = eventCode
        if (trackingLog.EventTime <= 0L) {
            trackingLog.EventTime = System.currentTimeMillis()
        }
        trackingLog.Params = params.toString()
        Timber.tag(TRACKING_LOG_TAG).i("Save event log start")
        saveLogToLocal(trackingLog)
        try {
            if (countLogOnLocal() >= MAX_NUM_COUNT && abs(System.currentTimeMillis() - lastTimeSuccessPostEven) > 5000L) {
                Timber.tag(TRACKING_LOG_TAG).i("Post event log start")
                localSource.allTrackingLog().collect { result ->
                    result.fold(onSuccess = { data ->
                        remoteSource.postEvent(data).collect { resultPostEvent ->
                            lastTimeSuccessPostEven = System.currentTimeMillis()
                            resultPostEvent.fold(onSuccess = { status ->
                                if (status) {
                                    Timber.tag(TRACKING_LOG_TAG).i("Post event log success!")
                                    deleteAllLogOnLocal()
                                } else {
                                    Timber.tag(TRACKING_LOG_TAG).i("Post event failed!")
                                }
                            }, onFailure = { ex ->
                                Timber.tag(TRACKING_LOG_TAG)
                                    .e("Cannot post event, pending... /nErr: $ex")
                            })
                        }
                    }, onFailure = { ex ->
                        Timber.tag(TRACKING_LOG_TAG)
                            .e("Cannot get log on local, pending... /nErr: $ex")
                    })
                }
                Timber.tag(TRACKING_LOG_TAG).i("Post event log end")
            }
        } catch (exception: Exception) {
            Timber.tag(TRACKING_LOG_TAG).e("Cannot post event, pending... /nErr: $exception")
        }
        Timber.tag(TRACKING_LOG_TAG).i("Save event log end")
    }

    private suspend fun countLogOnLocal(): Int {
        return localSource.countLog().also {
            Timber.tag(TRACKING_LOG_TAG).e("Total log in cache: $it")
        }
    }

    private suspend fun saveLogToLocal(vararg trackingLog: TrackingLogModel) {
        localSource.insertAllLog(trackingLog = trackingLog)
    }

    private suspend fun deleteAllLogOnLocal() {
        localSource.deleteAllLog()
    }
}