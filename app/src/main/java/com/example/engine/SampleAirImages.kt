package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader

enum class SampleScene(val titleTh: String, val descriptionTh: String, val expectedPm25Approx: String) {
    CLEAR_MOUNTAIN(
        titleTh = "ท้องฟ้าแจ่มใส ดอยสุเทพ (เชียงใหม่)",
        descriptionTh = "อากาศบริสุทธิ์ ทัศนวิสัยชัดเจน > 20 กม. ความหนาแน่นละอองฝุ่นต่ำมาก",
        expectedPm25Approx = "10 - 15 µg/m³ (ดีมาก)"
    ),
    MODERATE_SUBURB(
        titleTh = "หมอกควันบางช่วงบ่าย (นนทบุรี)",
        descriptionTh = "มีฝุ่นละอองสะสมปานกลาง ทัศนวิสัยลดลงเล็กน้อยตามลมพัด",
        expectedPm25Approx = "28 - 36 µg/m³ (ปานกลาง)"
    ),
    HEAVY_SMOG_CITY(
        titleTh = "วิกฤตฝุ่นหนาทึบ หน้าลานเมือง (กรุงเทพฯ)",
        descriptionTh = "มลพิษสะสมเข้มข้น ท้องฟ้าขาวขุ่น ทัศนวิสัยต่ำกว่า 3 กม. เตือนภัย!",
        expectedPm25Approx = "78 - 95 µg/m³ (มีผลกระทบ)"
    );

    fun generateBitmap(): Bitmap {
        val width = 480
        val height = 360
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (this) {
            CLEAR_MOUNTAIN -> {
                // Vibrant deep azure sky
                val skyGradient = LinearGradient(
                    0f, 0f, 0f, height * 0.65f,
                    Color.rgb(30, 130, 230), Color.rgb(135, 206, 250),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyGradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // Sharp distant mountains (High contrast, deep dark channel)
                paint.color = Color.rgb(45, 90, 65)
                val mtn1 = Path().apply {
                    moveTo(0f, height * 0.65f)
                    lineTo(width * 0.25f, height * 0.42f)
                    lineTo(width * 0.55f, height * 0.58f)
                    lineTo(width * 0.85f, height * 0.38f)
                    lineTo(width.toFloat(), height * 0.60f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(mtn1, paint)

                // Forefront lush dark green forest ridge
                paint.color = Color.rgb(20, 60, 35)
                val mtn2 = Path().apply {
                    moveTo(0f, height * 0.75f)
                    lineTo(width * 0.35f, height * 0.62f)
                    lineTo(width * 0.70f, height * 0.70f)
                    lineTo(width.toFloat(), height * 0.65f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(mtn2, paint)
            }

            MODERATE_SUBURB -> {
                // Pale grayish-cyan sky with noticeable particulate haze scattering
                val skyGradient = LinearGradient(
                    0f, 0f, 0f, height * 0.7f,
                    Color.rgb(140, 175, 200), Color.rgb(215, 225, 235),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyGradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // City silhouette with moderate contrast attenuation
                paint.color = Color.rgb(120, 135, 145)
                // Skyline buildings
                val skyline = Path().apply {
                    moveTo(0f, height * 0.62f)
                    lineTo(width * 0.15f, height * 0.62f)
                    lineTo(width * 0.15f, height * 0.48f)
                    lineTo(width * 0.25f, height * 0.48f)
                    lineTo(width * 0.25f, height * 0.65f)
                    lineTo(width * 0.40f, height * 0.65f)
                    lineTo(width * 0.40f, height * 0.44f)
                    lineTo(width * 0.52f, height * 0.44f)
                    lineTo(width * 0.52f, height * 0.63f)
                    lineTo(width * 0.75f, height * 0.63f)
                    lineTo(width * 0.75f, height * 0.52f)
                    lineTo(width * 0.88f, height * 0.52f)
                    lineTo(width * 0.88f, height * 0.65f)
                    lineTo(width.toFloat(), height * 0.65f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(skyline, paint)

                // Haze overlay veil (moderate optical depth)
                paint.color = Color.argb(85, 220, 225, 230)
                canvas.drawRect(0f, height * 0.35f, width.toFloat(), height.toFloat(), paint)
            }

            HEAVY_SMOG_CITY -> {
                // Dense yellowish-gray smog sky (Mie scattering dominant)
                val skyGradient = LinearGradient(
                    0f, 0f, 0f, height * 0.8f,
                    Color.rgb(205, 200, 185), Color.rgb(225, 220, 205),
                    Shader.TileMode.CLAMP
                )
                paint.shader = skyGradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
                paint.shader = null

                // Heavily obscured distant buildings (High transmission loss, blurred edges)
                paint.color = Color.rgb(180, 175, 165)
                val skylineDistant = Path().apply {
                    moveTo(0f, height * 0.60f)
                    lineTo(width * 0.2f, height * 0.60f)
                    lineTo(width * 0.2f, height * 0.45f)
                    lineTo(width * 0.35f, height * 0.45f)
                    lineTo(width * 0.35f, height * 0.60f)
                    lineTo(width * 0.6f, height * 0.60f)
                    lineTo(width * 0.6f, height * 0.40f)
                    lineTo(width * 0.75f, height * 0.40f)
                    lineTo(width * 0.75f, height * 0.60f)
                    lineTo(width.toFloat(), height * 0.60f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(skylineDistant, paint)

                // Thick particulate aerosol veil (extinction beta > 0.4)
                paint.color = Color.argb(160, 230, 225, 210)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Faint roadway / foreground bridge
                paint.color = Color.rgb(140, 135, 125)
                canvas.drawRect(0f, height * 0.78f, width.toFloat(), height.toFloat(), paint)
            }
        }

        return bitmap
    }
}
