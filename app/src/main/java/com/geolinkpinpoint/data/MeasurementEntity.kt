package com.geolinkpinpoint.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "measurements")
data class MeasurementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pointALatitude: Double,
    val pointALongitude: Double,
    val pointALabel: String?,
    val pointBLatitude: Double,
    val pointBLongitude: Double,
    val pointBLabel: String?,
    val distanceMeters: Double,
    val bearingDegrees: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String? = null
)
