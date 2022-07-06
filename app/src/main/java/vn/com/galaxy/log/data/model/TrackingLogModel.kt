package vn.com.galaxy.log.data.model

import android.os.Build
import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.*

@Entity(tableName = "tracking_log", primaryKeys = ["event_time", "event_code"])
data class TrackingLogModel(
    @ColumnInfo(name = "event_time") var EventTime: Long = 0,
    @ColumnInfo(name = "device_id") var DeviceID: String? = null,
    @ColumnInfo(name = "session_id") var SessionID: Long? = null,
    @ColumnInfo(name = "f_id") var FID: String? = null,
    @ColumnInfo(name = "user_id") var UserID: String? = null,
    @ColumnInfo(name = "child_id") var ChildID: String? = null,
    @ColumnInfo(name = "platform") var Platform: String? = "Android",
    @ColumnInfo(name = "device_brand") var DeviceBrand: String? = Build.BRAND,
    @ColumnInfo(name = "device_manufacturer") var DeviceManufacturer: String? = Build.MANUFACTURER,
    @ColumnInfo(name = "device_model") var DeviceModel: String? = Build.MODEL,
    @ColumnInfo(name = "device_density") var DeviceDensity: String? = null,
    @ColumnInfo(name = "device_type") var DeviceType: String = "mobile",
    @ColumnInfo(name = "version_os") var VersionOS: String? = Build.VERSION.RELEASE,
    @ColumnInfo(name = "version_app") var VersionApp: String? = "1.0.0",
    @ColumnInfo(name = "version_code") var VersionCode: String? = "1",
    @ColumnInfo(name = "package_name") var PackageName: String? = null,
    @ColumnInfo(name = "android_sdk") var AndroidSDK: String? = Build.VERSION.SDK_INT.toString(),
    @ColumnInfo(name = "language") var Language: String = Locale.getDefault().language,
    @ColumnInfo(name = "country") var Country: String = Locale.getDefault().country,
    @ColumnInfo(name = "ip") var IP: String? = null,
    @ColumnInfo(name = "host") var Host: String? = null,
    @ColumnInfo(name = "event_code") var EventCode: String = "",
    @ColumnInfo(name = "params") var Params: String? = null
)
