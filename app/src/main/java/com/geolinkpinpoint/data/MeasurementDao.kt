package com.geolinkpinpoint.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    fun getAllMeasurements(): Flow<List<MeasurementEntity>>

    @Query("SELECT * FROM measurements ORDER BY timestamp DESC")
    suspend fun getAllMeasurementsOnce(): List<MeasurementEntity>

    @Insert
    suspend fun insert(measurement: MeasurementEntity)

    @Delete
    suspend fun delete(measurement: MeasurementEntity)
}
