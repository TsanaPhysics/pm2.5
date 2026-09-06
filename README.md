# AeroScan PM2.5 🛰️
**แอปพลิเคชันตรวจวัดฝุ่นจากภาพถ่าย AI วิเคราะห์ PM2.5 แบบเรียลไทม์ พร้อม Web Application & Android APK**

<div align="center">
  <img src="aeroscan-qrcode.png" width="220" alt="AeroScan QR Code" />
  <p><b>📱 สแกน QR Code เพื่อเปิดใช้งานผ่านโทรศัพท์มือถือ</b></p>
  <p><code>https://dairy-faster-perjurer.ngrok-free.dev</code></p>
</div>

---

## 🌟 ฟีเจอร์หลัก (Key Features)

1. **AI Atmospheric Vision Engine**:
   - วิเคราะห์ฝุ่นละออง PM2.5 จากภาพถ่ายขอบฟ้าและท้องฟ้าด้วยทฤษฎี **Dark Channel Prior (DCP)** และ **Koschmieder's Law**
   - คำนวณค่าการส่องผ่านแสงบรรยากาศ ($t$), ดัชนีความหนาแน่นหมอกควัน (Haze Density Index %), และสัมประสิทธิ์การสูญพันธุ์ของแสง ($\beta$ Extinction)
   - มาตรวัด AQI ตามเกณฑ์มาตรฐานกรมควบคุมมลพิษ (PCD) และ US EPA

2. **Web Application & Live Camera HUD**:
   - รองรับโหมดอัปโหลดภาพถ่าย หรือทดสอบด้วยฉากจำลองสภาพอากาศ
   - โหมด **Live Camera HUD** ส่องกล้องมือถือ/เว็บแคมตรวจวัดค่าฝุ่นแบบเรียลไทม์
   - กราฟแนวโน้มสถิติย้อนหลัง (Trend Analytics) พร้อมปุ่มส่งออก CSV

3. **Android Application & APK**:
   - รองรับการทำงานออฟไลน์ 100% บนสมาร์ตโฟน Android
   - ดาวน์โหลดไฟล์ APK ได้โดยตรงผ่านหน้าเว็บที่ `/download/apk`

---

## 🚀 การรัน Web Server

```bash
# รันผ่าน Node.js
npm start
# หรือ
node server.js
```

เมื่อรันเสร็จแล้ว จะแสดง URL สำหรับเข้าใช้งาน:
- **Local URL**: `http://localhost:8080`
- **Public / Mobile URL**: `https://dairy-faster-perjurer.ngrok-free.dev`

---

## 📱 การ Build Android APK

```bash
# คอมไพล์และสร้าง APK แบบ Debug
./gradlew assembleDebug
```
ไฟล์ APK จะถูกสร้างไว้ที่ `app/build/outputs/apk/debug/app-debug.apk` หรือคัดลอกมาที่ `AeroScan-PM2.5-debug.apk`
