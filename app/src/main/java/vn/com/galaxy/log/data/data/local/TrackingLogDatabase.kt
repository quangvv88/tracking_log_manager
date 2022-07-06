package vn.com.galaxy.log.data.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import vn.com.galaxy.log.data.model.TrackingLogModel

@Database(
    entities = [
        TrackingLogModel::class],
    version = 1,
    exportSchema = false
)
abstract class TrackingLogDatabase : RoomDatabase() {
    abstract fun trackingLogDao(): TrackingLogDao
}