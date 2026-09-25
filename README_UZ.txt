DARS JADVALI — ANDROID LOYIHA v1

Bu loyiha Mobil 11-versiyadagi tasdiqlangan HTML interfeysni Android WebView ichiga joylaydi.

Android Studio orqali:
1. DarsJadvali_Android_v1 papkasini Open Project qiling.
2. Gradle yuklanishini kuting.
3. Telefonni USB orqali ulang (USB debugging yoqilgan bo‘lsin).
4. Run tugmasini bosing.
5. APK olish uchun: Build > Build App Bundle(s) / APK(s) > Build APK(s).

Kiritilgan:
- Mobil 11-versiya dizayni va funksiyalari.
- Telefon xotirasidan rasm/audio/Excel tanlash uchun Android file picker.
- LocalStorage/IndexedDB orqali ilova ma’lumotlarini saqlash.
- Android notification channel va AlarmManager uchun native tayanch.
- Android 13+ notification ruxsati.
- Exact alarm ruxsati.

Eslatma:
HTML prototipdagi barcha brauzer timerlarini to‘liq native Android alarmiga avtomatik ko‘chirish uchun
keyingi bosqichda jadval saqlanganda AndroidApp.scheduleAlarm(...) chaqiruvlarini jadval bilan bog‘lash kerak.
Loyiha bunga tayyor native bridge bilan berildi.
