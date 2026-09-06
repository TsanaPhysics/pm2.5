package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AqiGood
import com.example.ui.theme.AqiHazardous
import com.example.ui.theme.AqiModerate
import com.example.ui.theme.AqiUnhealthy
import com.example.ui.theme.AqiUnhealthySensitive
import com.example.ui.theme.AqiVeryGood

enum class AirQualityLevel(
    val titleTh: String,
    val titleEn: String,
    val minPm25: Float,
    val maxPm25: Float,
    val minAqi: Int,
    val maxAqi: Int,
    val color: Color,
    val maskRecommendation: String,
    val outdoorRecommendation: String,
    val sensitiveGroupWarning: String,
    val airPurifierAdvice: String,
    val requiresAlert: Boolean
) {
    VERY_GOOD(
        titleTh = "คุณภาพอากาศดีมาก",
        titleEn = "Very Good",
        minPm25 = 0.0f,
        maxPm25 = 15.0f,
        minAqi = 0,
        maxAqi = 25,
        color = AqiVeryGood,
        maskRecommendation = "ไม่จำเป็นต้องสวมหน้ากากอนามัย",
        outdoorRecommendation = "เหมาะสำหรับกิจกรรมกลางแจ้งและการท่องเที่ยวทุกประเภท",
        sensitiveGroupWarning = "ปลอดภัยสำหรับกลุ่มเสี่ยงทุกคน",
        airPurifierAdvice = "สามารถเปิดหน้าต่างรับลมธรรมชาติได้",
        requiresAlert = false
    ),
    GOOD(
        titleTh = "คุณภาพอากาศดี",
        titleEn = "Good",
        minPm25 = 15.1f,
        maxPm25 = 25.0f,
        minAqi = 26,
        maxAqi = 50,
        color = AqiGood,
        maskRecommendation = "สามารถใช้ชีวิตได้ตามปกติ",
        outdoorRecommendation = "ทำกิจกรรมกลางแจ้งได้ตามปกติ",
        sensitiveGroupWarning = "ผู้มีอาการแพ้อากาศระวังตัวเล็กน้อย",
        airPurifierAdvice = "เปิดระบายอากาศได้ตามปกติ",
        requiresAlert = false
    ),
    MODERATE(
        titleTh = "คุณภาพอากาศปานกลาง",
        titleEn = "Moderate",
        minPm25 = 25.1f,
        maxPm25 = 37.5f,
        minAqi = 51,
        maxAqi = 100,
        color = AqiModerate,
        maskRecommendation = "ควรเตรียมหน้ากากอนามัยเมื่ออยู่กลางแจ้งนาน",
        outdoorRecommendation = "ทำกิจกรรมกลางแจ้งได้ แต่ผู้ป่วยควรสังเกตอาการ",
        sensitiveGroupWarning = "กลุ่มเสี่ยงควรลดระยะเวลาทำกิจกรรมกลางแจ้งที่ใช้แรงมาก",
        airPurifierAdvice = "แนะนำเปิดเครื่องฟอกอากาศในห้องนอน",
        requiresAlert = false
    ),
    UNHEALTHY_SENSITIVE(
        titleTh = "เริ่มมีผลกระทบต่อสุขภาพ",
        titleEn = "Unhealthy for Sensitive Groups",
        minPm25 = 37.6f,
        maxPm25 = 75.0f,
        minAqi = 101,
        maxAqi = 200,
        color = AqiUnhealthySensitive,
        maskRecommendation = "ควรสวมหน้ากาก N95 หรือหน้ากากป้องกันฝุ่นเมื่อออกนอกอาคาร",
        outdoorRecommendation = "ควรลดเวลาการทำกิจกรรมกลางแจ้ง โดยเฉพาะการออกกำลังกายหนัก",
        sensitiveGroupWarning = "กลุ่มเสี่ยง (เด็ก คนชรา สตรีมีครรภ์ ผู้ป่วยทางเดินหายใจ) ควรหลีกเลี่ยงกิจกรรมกลางแจ้ง",
        airPurifierAdvice = "ควรปิดหน้าต่างและเปิดเครื่องฟอกอากาศตลอดเวลา",
        requiresAlert = true // Exceeds Thai standard (37.5 µg/m³)
    ),
    UNHEALTHY(
        titleTh = "มีผลกระทบต่อสุขภาพ",
        titleEn = "Unhealthy",
        minPm25 = 75.1f,
        maxPm25 = 150.0f,
        minAqi = 201,
        maxAqi = 300,
        color = AqiUnhealthy,
        maskRecommendation = "จำเป็นต้องสวมหน้ากาก N95 ตลอดเวลาเมื่ออยู่ภายนอก",
        outdoorRecommendation = "งดกิจกรรมกลางแจ้งทุกชนิด ให้อยู่ภายในอาคารที่ปิดมิดชิด",
        sensitiveGroupWarning = "อันตรายต่อกลุ่มเสี่ยงอย่างยิ่ง หากมีอาการแน่นหน้าอกให้รีบพบแพทย์",
        airPurifierAdvice = "เปิดเครื่องฟอกอากาศระดับสูงสุด ตรวจสอบแผ่นกรอง HEPA",
        requiresAlert = true
    ),
    HAZARDOUS(
        titleTh = "มีผลกระทบต่อสุขภาพรุนแรง",
        titleEn = "Hazardous",
        minPm25 = 150.1f,
        maxPm25 = 999.0f,
        minAqi = 301,
        maxAqi = 500,
        color = AqiHazardous,
        maskRecommendation = "สวมหน้ากาก N95 แบบมีผนึกแน่น ห้ามถอดหน้ากากภายนอกอาคาร",
        outdoorRecommendation = "ห้ามออกนอกอาคารโดยเด็ดขาด ภาวะวิกฤตมลพิษ",
        sensitiveGroupWarning = "ทุกคนมีโอกาสเกิดผลกระทบต่อสุขภาพรุนแรงฉับพลัน",
        airPurifierAdvice = "จัดทำห้องปลอดฝุ่น (Clean Room) และเปิดระบบกรองอากาศเข้มงวด",
        requiresAlert = true
    );

    companion object {
        fun fromPm25(pm25: Float): AirQualityLevel {
            return when {
                pm25 <= 15.0f -> VERY_GOOD
                pm25 <= 25.0f -> GOOD
                pm25 <= 37.5f -> MODERATE
                pm25 <= 75.0f -> UNHEALTHY_SENSITIVE
                pm25 <= 150.0f -> UNHEALTHY
                else -> HAZARDOUS
            }
        }

        fun calculateAqi(pm25: Float): Int {
            // US EPA / Thai PCD Standard AQI Piecewise Linear Interpolation
            return when {
                pm25 <= 15.0f -> {
                    val p = pm25.coerceIn(0f, 15f)
                    (p / 15f * 25f).toInt()
                }
                pm25 <= 25.0f -> {
                    val p = pm25 - 15f
                    (26 + (p / 10f * 24f)).toInt()
                }
                pm25 <= 37.5f -> {
                    val p = pm25 - 25f
                    (51 + (p / 12.5f * 49f)).toInt()
                }
                pm25 <= 75.0f -> {
                    val p = pm25 - 37.5f
                    (101 + (p / 37.5f * 99f)).toInt()
                }
                pm25 <= 150.0f -> {
                    val p = pm25 - 75f
                    (201 + (p / 75f * 99f)).toInt()
                }
                else -> {
                    val p = (pm25 - 150f).coerceAtMost(350f)
                    (301 + (p / 350f * 199f)).toInt().coerceAtMost(500)
                }
            }
        }
    }
}
