package com.neo.lib_call.core

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONObject
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Created by Kharozim
 * 11/06/26 - kharozim.wrk@gmail.com
 * Copyright (c) 2026. My Application
 * All Rights Reserved
 */
internal data class CallApiRequest(
  @SerializedName("number")
  val number: String,
  @SerializedName("device")
  val device: String,
  @SerializedName("agent_extension")
  val agenExtention: String,
  @SerializedName("metadata")
  val metadata: Map<String, String>,
)

internal data class BaseResponse<T>(
  @SerializedName("success")
  val success: Boolean? = null,
  @SerializedName("message")
  val message: String? = null,
  @SerializedName("data")
  val data: T? = null,
)

internal data class CallResponse(
  @SerializedName("callId")
  val callId: String? = null,
  @SerializedName("finalNumber")
  val finalNumber: String? = null,
)


internal data class RegisterRequest(
  @SerializedName("caller_mobile")
  val callerMobile: String,
  @SerializedName("name")
  val name: String,
)

internal data class ReleaseRequest(
  @SerializedName("extension")
  val extension: String,
  @SerializedName("caller_mobile")
  val callerMobile: String,
)

internal data class RegisterResponse(
  @SerializedName("extension")
  val extension: String? = null,
  @SerializedName("password")
  val password: String? = null,
  @SerializedName("domain")
  val domain: String? = null,
)

internal interface CallApiService {
  @POST("ami/api/v1/call/")
  suspend fun requestCall(@Body request: CallApiRequest): Response<BaseResponse<CallResponse>>

  @POST("ami/api/v1/client/call-settings/")
  suspend fun registerCall(
    @Header("Authorization") token: String,
    @Body request: RegisterRequest,
  ): Response<BaseResponse<RegisterResponse>>

  @POST("ami/api/v1/client/release-extension/")
  suspend fun releaseCall(
    @Header("Authorization") token: String,
    @Body request: ReleaseRequest,
  ): Response<BaseResponse<JSONObject>>
}

internal class CallApiException(
  message: String,
  val statusCode: Int? = null,
) : Exception(message)

internal class CallApiRepository(private val service: CallApiService) {
  suspend fun requestCall(request: CallApiRequest): Result<CallResponse> {
    return withContext(Dispatchers.IO) {
      try {
        val response = service.requestCall(request)
        val envelope = response.body()
        val call = envelope?.data

        when {
          !response.isSuccessful -> Result.failure(
            CallApiException("Call API returned HTTP ${response.code()}", response.code())
          )

          envelope?.success == false -> Result.failure(
            CallApiException("Call API rejected the request", response.code())
          )

          call?.callId.isNullOrBlank() -> Result.failure(
            CallApiException("Call API response is missing a call ID", response.code())
          )

          else -> Result.success(call)
        }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Exception) {
        Result.failure(error)
      }
    }
  }

  suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
    return withContext(Dispatchers.IO) {
      try {
        val token = "Bearer ${ApiClient.AUTH_TOKEN}"
        val response = service.registerCall(token = token, request = request)
        val envelope = response.body()
        val data = envelope?.data

        when {
          !response.isSuccessful -> Result.failure(
            CallApiException("Call API returned HTTP ${response.code()}", response.code())
          )

          envelope?.success == false -> Result.failure(
            CallApiException("Call API rejected the request", response.code())
          )

          data?.extension.isNullOrBlank() -> Result.failure(
            CallApiException("Call API response is missing extension", response.code())
          )

          else -> Result.success(data)
        }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Exception) {
        Result.failure(error)
      }
    }
  }

  suspend fun release(request: ReleaseRequest): Result<String> {
    return withContext(Dispatchers.IO) {
      try {
        val token = "Bearer ${ApiClient.AUTH_TOKEN}"
        val response = service.releaseCall(token = token, request = request)
        val envelope = response.body()

        when {
          !response.isSuccessful -> Result.failure(
            CallApiException("Call API returned HTTP ${response.code()}", response.code())
          )

          envelope?.success == false -> Result.failure(
            CallApiException("Call API rejected the request", response.code())
          )

          else -> Result.success(envelope?.message.orEmpty())
        }
      } catch (cancelled: CancellationException) {
        throw cancelled
      } catch (error: Exception) {
        Result.failure(error)
      }
    }
  }
}


internal object ApiClient {
  @Volatile
  var BASE_URL = "" // untuk emulator Android

  @Volatile
  var AUTH_TOKEN = "" // Ganti dengan token dinamis jika perlu

  val api: CallApiService by lazy {
    if (BASE_URL.isBlank()) {
      throw IllegalStateException("BASE_URL cannot be empty.")
    }

    val logging = HttpLoggingInterceptor().apply {
      setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    val authInterceptor = okhttp3.Interceptor { chain ->
      val request = chain.request().newBuilder()
        .build()
      chain.proceed(request)
    }

    val client = OkHttpClient.Builder()
      .addInterceptor(authInterceptor)
      .addInterceptor(logging)
      .build()

    Retrofit.Builder()
      .baseUrl(BASE_URL)
      .addConverterFactory(GsonConverterFactory.create())
      .client(client)
      .build()
      .create(CallApiService::class.java)
  }

}
