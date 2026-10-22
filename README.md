# NEO SDK Call Android

This SDK allows you to integrate **outgoing VoIP call features** into your Android app using the **NEO SDK** 

---

## Requirements & Compatibility

| Specification | Supported / Recommended Version |
|---|---|
| **Minimum Android SDK (`minSdk`)** | API 24 (Android 7.0 Nougat) |
| **Compile Android SDK (`compileSdk`)** | API 34+ (Compiled with API 36) |
| **Kotlin Version** | 2.0.0+ (Tested with Kotlin 2.2.10) |
| **Java Compatibility** | Java 11 (`JavaVersion.VERSION_11`) / JDK 11, 17, 21 |
| **Jetpack Compose** | Compose BOM 2024.09.00+ |
| **Version Call SDK** | 1.0.0 |
---

## Installation

### 1. Configure Repositories

Add JitPack and the Linphone Maven repository to your root `settings.gradle.kts` (or root `build.gradle.kts`):

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

### 2. Add Dependency

Add the dependency to your application module's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.kharozim:lib-call:1.0.0")
}
```

*(Note: Replace `1.0.0` with the desired release tag from GitHub/JitPack).*

---

## Android Setup Requirements

### 1. Android Manifest Permissions

Ensure the following permissions are present in your app's `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
        
<!--  optional if error sync gradle-->
<uses-permission android:name="android.permission.WAKE_LOCK"
tools:remove="android:maxSdkVersion" />
```

---

### 2. ProGuard Configuration

If your app uses ProGuard / R8 minification, add the following rules to your `proguard-rules.pro` file:

```proguard
# Keep Neo SDK Call internal classes
-keep class com.neo.lib_call.** { *; }
-dontwarn com.neo.lib_call.**

# Keep Linphone SDK classes & native methods
-keep class org.linphone.** { *; }
-dontwarn org.linphone.**

# keep stack protokol support (SIP stack & media engine)
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

# Keep Retrofit, OkHttp, and Gson
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
### 3. Gradle Configuration (Resolve Conflict)
if error sync gradle, add this to `app/build.gradle.kts`:


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
> **Note:**
> sample error gradle : 
> *2 files found with path 'META-INF/versions/9/OSGI-INF/MANIFEST.MF' from inputs:*
> - *com.squareup.okhttp3:logging-interceptor:5.4.0/logging-interceptor-5.4.0.jar*
> - *org.jspecify:jspecify:1.0.0/jspecify-1.0.0.jar*

---

## Usage

### 1. Initialize & Configure SDK

Initialize the SDK and configure backend API credentials in your `Application` class:

```kotlin
import android.app.Application
import com.neo.lib_call.api.CallSdk

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()

        // 1. Initialize SDK Core
        CallSdk.init(context = this, isDebug = false)

        // 2. Configure Backend PBX API URL and Auth Token
        CallSdk.initAPI(
            baseUrl = "https://your-pbx-api.com/",
            token = "your_auth_token"
        )
    }
}
```

---

### 2. Make an Outgoing Call (`makeCallSip`)

Trigger a call directly using `CallSdk.makeCallSip(...)`. Runtime permissions (such as microphone access) and SIP account registration with the PBX backend are handled automatically within the call screen:

```kotlin
import com.neo.lib_call.api.CallSdk

CallSdk.makeCallSip(
    destinationNumber = "081234567890",
    destinationName = "Customer Name",
    contactImage = "https://example.com/avatar.jpg", // Optional contact image URL
    metadata = mapOf(
        "customer_id" to "cust_123",       // Required: customer identifier
        "customer_name" to "Customer Name", // Required: customer name
        "call_title" to "Telepon Customer", // Optional: call screen title
        "type_call" to "teleai"             // Optional: call type tag
    )
)
```

> **Note:**
> - `destinationNumber`, `customer_id`, and `customer_name` are required parameters.
> - The SDK automatically checks/requests audio permissions and registers the SIP extension with the PBX backend upon launching the call screen.

---

### (Optional) Receive Call Result (`makeCallIntent`)

If your app needs to receive call results (e.g. whether the call was answered or disconnected) when the user returns from the call screen, use `makeCallIntent` with `rememberLauncherForActivityResult`:

```kotlin
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.neo.lib_call.api.CallSdk

val callLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
) { result ->
    val isConnected = result.data?.getBooleanExtra(CallSdk.IS_CALL_CONNECT, false) ?: false
    val callDetail = result.data?.getStringExtra(CallSdk.CALL_DETAIL_RESULT)
    
    // isConnected: true if call was connected/answered, false otherwise
    // callDetail: optional CDR JSON string
}

val callIntent = CallSdk.makeCallIntent(
    context = context,
    destinationNumber = "081234567890",
    destinationName = "Customer Name",
    contactImage = "https://example.com/avatar.jpg",
    metadata = mapOf(
        "customer_id" to "cust_123",
        "customer_name" to "Customer Name",
        "call_title" to "Telepon Customer",
        "type_call" to "teleai"           
    )
)
callLauncher.launch(callIntent)
```

---

## Observability & State Management

The SDK exposes reactive `StateFlow` streams that allow you to monitor registration and call states across your application:

```kotlin
// Monitor SIP Registration State
CallSdk.registrationState.collect { state ->
    when (state) {
        RegisterState.None -> { /* Not registered */ }
        RegisterState.Progress -> { /* Registering... */ }
        RegisterState.Ok -> { /* Ready to call */ }
        RegisterState.Failed -> { /* Registration failed */ }
        RegisterState.Cleared -> { /* Unregistered */ }
        RegisterState.Refreshing -> { /* Token refresh */ }
    }
}

// Monitor Active Call State
CallSdk.callState.collect { state ->
    when (state) {
        CallState.Idle -> { /* No active call */ }
        CallState.Initializing -> { /* Preparing session */ }
        CallState.Dialing -> { /* Requesting backend call */ }
        CallState.Ringing -> { /* Callee is ringing */ }
        CallState.Connected -> { /* Call active & running */ }
        CallState.Ended -> { /* Call hung up normally */ }
        CallState.Failed -> { /* Call failed */ }
    }
}
```

---

## Optional Metadata Configuration

You can customize UI text, button labels, and backend parameters via the `metadata` map passed to `makeCallSip` (or `makeCallIntent`):

| Metadata Key | Description / Purpose | Default Value |
|---|---|---|
| **`call_title`** | Title header displayed on the call screen | `"Telepon Customer"` |
| **`call_btn_speaker`** | Label text for the speaker toggle button | `"Speaker"` |
| **`call_btn_mute`** | Label text for the microphone mute button | `"Mute"` |
| **`call_numpad`** | Label text for the DTMF dialpad button | `"Numpad"` |
| **`customer_id`** | Customer identifier sent in PBX API payload | `""` |
| **`customer_name`** | Customer name sent in PBX API payload | `""` |
| **`telephone_id`** | Custom telephone identifier for CDR mapping | Optional |
| **`type_call`** | Call categorization tag (e.g., `"teleai"`) | Optional |

---

## Call Result Contract

When the call screen finishes and returns to your calling Activity, inspect the result intent for these keys:

- **`CallSdk.IS_CALL_CONNECT`** (`Boolean`):
  - `true`: The call was successfully answered and established with the destination.
  - `false`: The call was terminated before being answered (rejected, busy, unanswered, failed, or cancelled).
- **`CallSdk.CALL_DETAIL_RESULT`** (`String?`):
  - Optional JSON string containing CDR (Call Detail Record), duration/billsec, and disconnect reasons received from the WebSocket event.

---
