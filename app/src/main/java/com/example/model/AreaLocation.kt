package com.example.model

data class AreaLocation(
    val province: String,
    val district: String,
    val subDistrict: String,
    val latitude: Double,
    val longitude: Double,
    val isGpsAuto: Boolean = false
) {
    fun getDisplayName(): String {
        return if (isGpsAuto) {
            "ตำแหน่ง GPS ปัจจุบัน ($province $district)"
        } else {
            "$province, $district, $subDistrict"
        }
    }

    fun getCoordinatesFormatted(): String {
        return "%.4f, %.4f".format(latitude, longitude)
    }

    companion object {
        val DEFAULT = AreaLocation(
            province = "กรุงเทพมหานคร",
            district = "ปทุมวัน",
            subDistrict = "ลุมพินี",
            latitude = 13.7307,
            longitude = 100.5418,
            isGpsAuto = false
        )

        // Preloaded representative Thailand Administrative Areas with Lat/Long
        val PRESET_LOCATIONS = listOf(
            AreaLocation("กรุงเทพมหานคร", "ปทุมวัน", "ลุมพินี", 13.7307, 100.5418),
            AreaLocation("กรุงเทพมหานคร", "จตุจักร", "จตุจักร", 13.8282, 100.5598),
            AreaLocation("กรุงเทพมหานคร", "บางรัก", "สีลม", 13.7279, 100.5283),
            AreaLocation("กรุงเทพมหานคร", "คลองเตย", "คลองเตย", 13.7082, 100.5839),
            AreaLocation("กรุงเทพมหานคร", "ดินแดง", "ดินแดง", 13.7699, 100.5532),
            AreaLocation("เชียงใหม่", "เมืองเชียงใหม่", "สุเทพ", 18.7904, 98.9562),
            AreaLocation("เชียงใหม่", "เมืองเชียงใหม่", "ช้างเผือก", 18.8062, 98.9712),
            AreaLocation("เชียงใหม่", "แม่แจ่ม", "ช่างเคิ่ง", 18.4983, 98.3639),
            AreaLocation("เชียงใหม่", "หางดง", "หางดง", 18.6872, 98.9189),
            AreaLocation("ขอนแก่น", "เมืองขอนแก่น", "ในเมือง", 16.4419, 102.8359),
            AreaLocation("นนทบุรี", "เมืองนนทบุรี", "บางกระสอ", 13.8621, 100.5134),
            AreaLocation("ชลบุรี", "เมืองชลบุรี", "บางปลาสร้อย", 13.3611, 100.9847),
            AreaLocation("ชลบุรี", "บางละมุง", "พัทยา", 12.9276, 100.8771),
            AreaLocation("นครราชสีมา", "เมืองนครราชสีมา", "ในเมือง", 14.9799, 102.0978),
            AreaLocation("ลำปาง", "เมืองลำปาง", "สบตุ๋ย", 18.2778, 99.4928),
            AreaLocation("น่าน", "เมืองน่าน", "ในเวียง", 18.7756, 100.7730),
            AreaLocation("เชียงราย", "เมืองเชียงราย", "เวียง", 19.9105, 99.8406),
            AreaLocation("ภูเก็ต", "เมืองภูเก็ต", "ตลาดใหญ่", 7.8804, 98.3923),
            AreaLocation("สงขลา", "หาดใหญ่", "หาดใหญ่", 7.0084, 100.4767),
            AreaLocation("อุบลราชธานี", "เมืองอุบลราชธานี", "ในเมือง", 15.2449, 104.8473)
        )

        val PROVINCES = listOf(
            "กรุงเทพมหานคร",
            "เชียงใหม่",
            "ขอนแก่น",
            "นนทบุรี",
            "ชลบุรี",
            "นครราชสีมา",
            "ลำปาง",
            "น่าน",
            "เชียงราย",
            "ภูเก็ต",
            "สงขลา",
            "อุบลราชธานี",
            "ระยอง",
            "สมุทรปราการ",
            "ปทุมธานี",
            "สระบุรี"
        )
    }
}
