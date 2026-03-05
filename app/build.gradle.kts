import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import com.google.gson.Gson

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.android)
  alias(libs.plugins.kotlin.compose)
}

data class EnvSimobile(
  val baseUrl: Map<String, String> = emptyMap(),
  val keys: Map<String, String> = emptyMap(),
  val signing: Map<String, String> = emptyMap()
)

android {
  namespace = "com.neo.app"
  compileSdk = 34

  defaultConfig {
    applicationId = "com.neo.app"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  kotlinOptions {
    jvmTarget = "11"
  }
  buildFeatures {
    compose = true
  }

//  val localProperties = gradleLocalProperties(projectRootDir = rootDir, providers = providers)
//  fun getLocalProperty(key: String) = localProperties.getProperty(key)
//
//  val rawEnvCI = System.getenv("ENV_NEO_CALL")
//  val env = rawEnvCI?.let {
//    runCatching { Gson().fromJson(it, EnvSimobile::class.java) }.getOrNull()
//  } ?: EnvSimobile()
//
//  fun envSigning(key: String) : String? = env.signing[key] ?: getLocalProperty(key)
//
//  signingConfigs {
//    register("release") {
//      val ksPath  = envSigning("KEYSTORE_PATH")
//      val ksPass  = envSigning("KEYSTORE_PASSWORD")
//      val alias   = envSigning("KEY_ALIAS")
//      val keyPass = envSigning("KEY_PASSWORD")
//
//      // ✅ penting: jangan set storeFile kalau kosong
//      if (!ksPath.isNullOrBlank() &&
//        !ksPass.isNullOrBlank() &&
//        !alias.isNullOrBlank() &&
//        !keyPass.isNullOrBlank()
//      ) {
//        storeFile = file(ksPath)
//        storePassword = ksPass
//        keyAlias = alias
//        keyPassword = keyPass
//      }
//    }
//
//    // You can define additional signing configurations if needed
//    // register("debug") {
//    //     ...
//    // }
//  }
}

dependencies {

  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.navigation3.runtime)
  implementation(libs.androidx.navigation3.ui)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  debugImplementation(libs.androidx.compose.ui.tooling)
  debugImplementation(libs.androidx.compose.ui.test.manifest)

  implementation(libs.converter.gson)
  implementation("com.github.cicareteam:cicare-sdk-rtc:1.2.1-rc.6")
}