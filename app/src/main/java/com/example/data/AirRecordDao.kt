package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.model.AirRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AirRecordDao {
    @Query("SELECT * FROM air_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<AirRecord>>

    @Query("SELECT * FROM air_records ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<AirRecord>>

    @Query("SELECT * FROM air_records WHERE isSynced = 0 ORDER BY timestamp ASC")
    suspend fun getUnsyncedRecords(): List<AirRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: AirRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AirRecord>)

    @Query("UPDATE air_records SET isSynced = :synced WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, synced: Boolean)

    @Query("UPDATE air_records SET isSynced = 1 WHERE isSynced = 0")
    suspend fun markAllAsSynced()

    @Query("DELETE FROM air_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM air_records")
    suspend fun clearAll()

    @Query("SELECT AVG(pm25) FROM air_records")
    fun getAveragePm25(): Flow<Float?>

    @Query("SELECT MAX(pm25) FROM air_records")
    fun getMaxPm25(): Flow<Float?>

    @Query("SELECT MIN(pm25) FROM air_records")
    fun getMinPm25(): Flow<Float?>

    @Query("SELECT COUNT(*) FROM air_records")
    fun getCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM air_records WHERE pm25 > :threshold")
    fun getExceededCount(threshold: Float = 37.5f): Flow<Int>
}
