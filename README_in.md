# NEO SDK Call Android

SDK ini memungkinkan Anda mengintegrasikan **fitur panggilan VoIP keluar (outgoing)** ke dalam aplikasi Android menggunakan **NEO SDK**.

---

## Persyaratan & Kompatibilitas

| Spesifikasi | Versi yang Didukung / Direkomendasikan |
|---|---|
| **Minimum Android SDK (`minSdk`)** | API 24 (Android 7.0 Nougat) |
| **Compile Android SDK (`compileSdk`)** | API 34+ (Dibuat dengan API 36) |
| **Versi Kotlin** | 2.0.0+ (Diuji dengan Kotlin 2.2.10) |
| **Kompatibilitas Java** | Java 11 (`JavaVersion.VERSION_11`) / JDK 11, 17, 21 |
| **Jetpack Compose** | Compose BOM 2024.09.00+ |
| **Version Call SDK** | 1.0.0 |

---

## Instalasi

### 1. Konfigurasi Repository

Tambahkan repository JitPack dan Linphone Maven ke dalam file root `settings.gradle.kts` (atau root `build.gradle.kts`):

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://download.linphone.org/maven_repository/") }
    }
}
```

### 2. Tambahkan Dependensi

Tambahkan dependensi berikut ke dalam file `build.gradle.kts` modul aplikasi Anda:

```kotlin
dependencies {
    implementation("com.github.kharozim:lib-call:1.0.0")
}
```

*(Catatan: Ganti `1.0.0` dengan tag rilis yang sesuai dari GitHub/JitPack).*

---

## Pengaturan Kebutuhan Android

### 1. Izin Android Manifest

Pastikan izin-izin berikut telah ditambahkan ke dalam file `AndroidManifest.xml` aplikasi Anda:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
        
<!-- Opsional jika terjadi error sync gradle / konflik maxSdkVersion -->
<uses-permission android:name="android.permission.WAKE_LOCK"
    tools:remove="android:maxSdkVersion" />
```

---

### 2. Konfigurasi ProGuard

Jika aplikasi Anda menggunakan minifikasi ProGuard / R8, tambahkan aturan berikut ke dalam file `proguard-rules.pro`:

```proguard
# Menjaga class internal Neo SDK Call
-keep class com.neo.lib_call.** { *; }
-dontwarn com.neo.lib_call.**

# Menjaga class Linphone SDK & native methods
-keep class org.linphone.** { *; }
-dontwarn org.linphone.**

# Menjaga stack protokol pendukung (SIP stack & media engine)
-keep class org.bellesip.** { *; }
-dontwarn org.bellesip.**

-keep class org.mediastreamer2.** { *; }
-dontwarn org.mediastreamer2.**

# Menjaga deklarasi method native JNI di seluruh proyek
-keepclasseswithmembernames class * {
    native <methods>;
}

# Menjaga metadata class, signature, dan anotasi untuk callback
-keepattributes *Annotation*, InnerClasses, EnclosingMethod, Signature, Exceptions

# Menjaga Retrofit, OkHttp, dan Gson
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
```

---

### 3. Konfigurasi Gradle (Penyelesaian Konflik)

Jika terjadi error duplikasi file saat sync / build gradle, tambahkan konfigurasi packaging ini ke `app/build.gradle.kts`:

```kotlin
android {
    ... 
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Abaikan atau kecualikan file MANIFEST.MF yang konflik
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}
```

> **Catatan:**
> Contoh pesan error gradle terkait:
> *2 files found with path 'META-INF/versions/9/OSGI-INF/MANIFEST.MF' from inputs:*
> - *com.squareup.okhttp3:logging-interceptor:5.4.0/logging-interceptor-5.4.0.jar*
> - *org.jspecify:jspecify:1.0.0/jspecify-1.0.0.jar*

---

## Penggunaan

### 1. Inisialisasi & Konfigurasi SDK

Inisialisasi SDK dan atur kredensial API backend di kelas `Application` Anda:

```kotlin
import android.app.Application
import com.neo.lib_call.api.CallSdk

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Inisialisasi Core SDK
        CallSdk.init(context = this, isDebug = false)

        // 2. Konfigurasi URL API PBX Backend dan Token Autentikasi
        CallSdk.initAPI(
            baseUrl = "https://your-pbx-api.com/",
            token = "your_auth_token"
        )
    }
}
```

---

### 2. Melakukan Panggilan Keluar (`makeCallSip`)

Picu panggilan langsung menggunakan fungsi `CallSdk.makeCallSip(...)`. Pengecekan izin runtime (seperti akses mikrofon) serta registrasi akun SIP ke backend PBX akan ditangani secara otomatis di dalam layar panggilan:

```kotlin
import com.neo.lib_call.api.CallSdk

CallSdk.makeCallSip(
    destinationNumber = "081234567890",
    destinationName = "Nama Customer",
    contactImage = "https://example.com/avatar.jpg", // Opsional: URL avatar/foto kontak
    metadata = mapOf(
        "customer_id" to "cust_123",       // Wajib: ID/identifier customer
        "customer_name" to "Nama Customer", // Wajib: nama customer
        "call_title" to "Telepon Customer", // Opsional: judul header layar panggilan
        "type_call" to "teleai"             // Opsional: label tipe panggilan
    )
)
```

> **Catatan:**
> - Parameter `destinationNumber`, `customer_id`, dan `customer_name` wajib diisi.
> - SDK akan secara otomatis meminta izin audio dan mendaftarkan ekstensi SIP ke backend PBX saat layar panggilan dibuka.

---

### (Opsional) Menerima Hasil Panggilan (`makeCallIntent`)

Jika aplikasi Anda perlu menerima hasil status panggilan (misalnya mengetahui apakah panggilan berhasil diangkat atau terputus) saat user kembali dari layar panggilan, gunakan `makeCallIntent` bersama `rememberLauncherForActivityResult`:

```kotlin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.neo.lib_call.api.CallSdk

val callLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val isConnected = result.data?.getBooleanExtra(CallSdk.IS_CALL_CONNECT, false) ?: false
    val callDetail = result.data?.getStringExtra(CallSdk.CALL_DETAIL_RESULT)
    
    // isConnected: bernilai true jika panggilan sempat tersambung/diangkat, false jika tidak
    // callDetail: string JSON opsional berisi CDR (Call Detail Record)
}

val callIntent = CallSdk.makeCallIntent(
    context = context,
    destinationNumber = "081234567890",
    destinationName = "Nama Customer",
    contactImage = "https://example.com/avatar.jpg",
    metadata = mapOf(
        "customer_id" to "cust_123",
        "customer_name" to "Nama Customer",
        "call_title" to "Telepon Customer",
        "type_call" to "teleai"
    )
)
callLauncher.launch(callIntent)
```

---

## Pemantauan & Manajemen Status (State Management)

SDK menyediakan stream reactive `StateFlow` untuk memantau status registrasi dan status panggilan di seluruh aplikasi Anda:

```kotlin
// Memantau Status Registrasi SIP
CallSdk.registrationState.collect { state ->
    when (state) {
        RegisterState.None -> { /* Belum terdaftar */ }
        RegisterState.Progress -> { /* Sedang mendaftar... */ }
        RegisterState.Ok -> { /* Berhasil terdaftar & siap memanggil */ }
        RegisterState.Failed -> { /* Registrasi gagal */ }
        RegisterState.Cleared -> { /* Registrasi dibersihkan */ }
        RegisterState.Refreshing -> { /* Memperbarui token/registrasi */ }
    }
}

// Memantau Status Panggilan Aktif
CallSdk.callState.collect { state ->
    when (state) {
        CallState.Idle -> { /* Tidak ada panggilan aktif */ }
        CallState.Initializing -> { /* Menyiapkan sesi panggilan */ }
        CallState.Dialing -> { /* Mengirim request panggilan ke backend */ }
        CallState.Ringing -> { /* Tujuan sedang berdering */ }
        CallState.Connected -> { /* Panggilan terhubung & percakapan aktif */ }
        CallState.Ended -> { /* Panggilan berakhir normal */ }
        CallState.Failed -> { /* Panggilan gagal */ }
    }
}
```

---

## Konfigurasi Metadata Opsional

Anda dapat menyesuaikan teks tampilan antarmuka (UI), tombol, serta parameter backend melalui map `metadata` yang dikirimkan ke `makeCallSip` (atau `makeCallIntent`):

| Kunci Metadata | Deskripsi / Tujuan | Nilai Default |
|---|---|---|
| **`call_title`** | Judul header yang ditampilkan pada layar panggilan | `"Telepon Customer"` |
| **`call_btn_speaker`** | Label teks untuk tombol speaker | `"Speaker"` |
| **`call_btn_mute`** | Label teks untuk tombol mute mikrofon | `"Mute"` |
| **`call_numpad`** | Label teks untuk tombol dialpad DTMF | `"Numpad"` |
| **`customer_id`** | Identifier customer yang dikirim pada payload API PBX | `""` |
| **`customer_name`** | Nama customer yang dikirim pada payload API PBX | `""` |
| **`telephone_id`** | Identifier nomor telepon untuk pencocokan CDR | Opsional |
| **`type_call`** | Label kategori panggilan (contoh: `"teleai"`) | Opsional |

---

## Kontrak Hasil Panggilan (Call Result Contract)

Ketika layar panggilan selesai dan kembali ke Activity pemanggil, periksa intent result menggunakan kunci-kunci berikut:

- **`CallSdk.IS_CALL_CONNECT`** (`Boolean`):
  - `true`: Panggilan berhasil diangkat dan tersambung dengan pihak tujuan.
  - `false`: Panggilan berakhir sebelum tersambung (ditolak, sibuk, tidak dijawab, gagal, atau dibatalkan).
- **`CallSdk.CALL_DETAIL_RESULT`** (`String?`):
  - String JSON opsional berisi data CDR (Call Detail Record), durasi bicara (`billsec`), serta alasan terputus yang diterima dari event WebSocket.

---
