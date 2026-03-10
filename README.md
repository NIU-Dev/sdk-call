# Dokumentasi Library `sdkcall`

[![](https://jitpack.io/v/NIU-Dev/sdk-call.svg)](https://jitpack.io/#NIU-Dev/sdk-call)

## Spesifikasi Project yang Mendukung
- **Namespace:** com.neo.sdkcall
- **Versi Library:** 1.0.0
- **Versi Kotlin:** Minimal 1.5+
- **Min SDK Android:** 23 (Android 6.0 Marshmallow)
- **Compile SDK:** 35
- **Source/Target Compatibility:** Java 11
- **Gradle:** Minimal versi 7.0+
- **Kotlin Compiler Extension:** 1.5.13

## Cara Import Library (JitPack)
1. Tambahkan JitPack ke `settings.gradle.kts` atau `build.gradle.kts` root:
   ```kotlin
   dependencyResolutionManagement {
       repositories {
           ... // existing repositories
           maven("https://jitpack.io")
       }
   }
   ```
2. Tambahkan dependency di modul yang ingin menggunakan `sdkcall` (misal di `app/build.gradle.kts`):
   ```kotlin
   dependencies {
       implementation("com.github.NIU-Dev:sdk-call:Tag")
   }
   ```
   Ganti `Tag` dengan versi release yang tersedia. [![](https://jitpack.io/v/NIU-Dev/sdk-call.svg)](https://jitpack.io/#NIU-Dev/sdk-call)

## Permission di Manifest
Tambahkan permission berikut di `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_PHONE_CALL" />
<uses-permission android:name="android.permission.MANAGE_OWN_CALLS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
```


## Cara Inisialisasi Project
1. Pastikan dependency sudah di-import.
2. Inisialisasi SDK di `Application` sesuai contoh berikut:
   ```kotlin
   import android.app.Application
   import com.neo.sdkcall.NeoSdkCall

   class MyApp : Application() {
       override fun onCreate() {
           super.onCreate()
           NeoSdkCall.init(this)
           NeoSdkCall.setAPI(
               baseUrl = "<base url>",
               token = "<token Anda>"
           )
       }
   }
   ```
Ganti `<token Anda>` ,`<base url>` dengan token yang valid sesuai kebutuhan aplikasi.

Pastikan nama class Application (MyApp) didaftarkan di `AndroidManifest.xml`:
```xml
<application
    android:name=".MyApp"
    ... >
    ...
</application>
```

## Cara Memberikan Permission
Sebelum melakukan call, pastikan permission sudah diberikan. Contoh pengecekan dan permintaan permission:
```kotlin
val currentActivity = context.findComponentActivity()
if (!PermissionUtil.checkAndRequestPermissions(context, currentActivity)) {
   // Permission belum diberikan, tampilkan pesan atau minta ulang
   return
}
```
Pastikan PermissionUtil sudah tersedia di project Anda, atau gunakan library permission lain sesuai kebutuhan. file `PermissionUtil.kt` bisa copy dari link dibawah ini :
- https://github.com/NIU-Dev/sdk-call/blob/main/app/src/main/java/com/neo/app/PermissionUtil.kt

## Cara Melakukan Call
Contoh penggunaan untuk melakukan call (lihat implementasi di CallScreen):
```kotlin
import com.neo.sdkcall.NeoSdkCall

NeoSdkCall.makeCallSip(
   activity = currentActivity, // Activity yang memanggil call
   callerId = "", // Nomor atau ID pemanggil
   callerName = "", // Nama pemanggil
   callerAvatar = "", // URL/avatar pemanggil (opsional)
   checkSum = "checksum", // Checksum untuk validasi (opsional)
   metaData = emptyMap(), // Metadata tambahan (opsional)
   destination = "", // Nomor SIP tujuan
   destinationName = "", // Nama tujuan
   destinationAvatar = "", // URL/avatar tujuan (opsional)
)
```

---

**Catatan:**
- Pastikan endpoint dan parameter sesuai kebutuhan.
- Jika ada error dependency, pastikan versi JitPack dan Kotlin sudah sesuai.
- Pastikan permission dan service di manifest sudah lengkap sesuai contoh di atas.
