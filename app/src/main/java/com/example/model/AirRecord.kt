package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "air_records")
data class AirRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val pm25: Float,
    val aqi: Int,
    val levelName: String,
    val extinctionCoeff: Float,
    val hazeIndexPercent: Int,
    val confidencePercent: Int,
    val province: String,
    val district: String,
    val subDistrict: String,
    val latitude: Double,
    val longitude: Double,
    val isGpsAuto: Boolean = false,
    val imageUri: String? = null,
    val isSynced: Boolean = false,
    val notes: String? = null
) {
    fun toAirQualityLevel(): AirQualityLevel {
        return try {
            AirQualityLevel.valueOf(levelName)
        } catch (_: Exception) {
            AirQualityLevel.fromPm25(pm25)
        }
    }

    fun toAreaLocation(): AreaLocation {
        return AreaLocation(
            province = province,
            district = district,
            subDistrict = subDistrict,
            latitude = latitude,
            longitude = longitude,
            isGpsAuto = isGpsAuto
        )
    }
}
