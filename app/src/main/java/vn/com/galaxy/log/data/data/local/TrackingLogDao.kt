package vn.com.galaxy.log.data.data.local

import androidx.room.*
import vn.com.galaxy.log.data.model.TrackingLogModel

@Dao
interface TrackingLogDao {
    @Query("SELECT * FROM tracking_log")
    suspend fun getAll(): List<TrackingLogModel>

    @Query("SELECT COUNT(*) FROM tracking_log")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg trackingLog: TrackingLogModel)

    @Delete
    suspend fun delete(trackingLog: TrackingLogModel)

    @Query("DELETE FROM tracking_log")
    suspend fun deleteAll()
}