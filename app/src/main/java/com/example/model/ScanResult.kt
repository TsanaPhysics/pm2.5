package com.example.model

data class ScanResult(
    val pm25: Float,
    val aqi: Int,
    val level: AirQualityLevel,
    val extinctionCoeff: Float,
    val hazeIndexPercent: Int,
    val transmission: Float,
    val contrastEntropy: Float,
    val confidencePercent: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val location: AreaLocation,
    val imagePath: String? = null,
    val isOfflineProcessed: Boolean = true
) {
    fun isAlertTriggered(thresholdPm25: Float = 37.5f): Boolean {
        return pm25 > thresholdPm25
    }
}
