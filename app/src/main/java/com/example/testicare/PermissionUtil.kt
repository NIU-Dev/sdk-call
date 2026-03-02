package com.example.testicare

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Created by Kharozim
 * 02/03/26 - kharozim.wrk@gmail.com
 * Copyright (c) 2026. Test Icare
 * All Rights Reserved
 */
object PermissionUtil {


  private val requiredPermissions = arrayOf(
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.READ_PHONE_STATE,
  )

  @RequiresApi(Build.VERSION_CODES.P)
  private val requiredPermissions28 = arrayOf(
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.FOREGROUND_SERVICE,
    android.Manifest.permission.READ_PHONE_STATE,
  )

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  private val requiredPermissionsTirmaisu = arrayOf(
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.FOREGROUND_SERVICE,
    android.Manifest.permission.POST_NOTIFICATIONS,
    android.Manifest.permission.READ_PHONE_STATE,
  )

  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
  private val requiredPermissionsUpsideDownCake = arrayOf(
    android.Manifest.permission.RECORD_AUDIO,
    android.Manifest.permission.FOREGROUND_SERVICE,
    android.Manifest.permission.POST_NOTIFICATIONS,
    android.Manifest.permission.READ_PHONE_STATE,
    android.Manifest.permission.FOREGROUND_SERVICE_MICROPHONE,
    android.Manifest.permission.FOREGROUND_SERVICE_PHONE_CALL
  )

  fun checkAndRequestPermissions(context : Context, activity: Activity): Boolean {
    val ctx = context

    val permissions = when {
      Build.VERSION.SDK_INT < Build.VERSION_CODES.P -> requiredPermissions
      Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU -> requiredPermissions28
      Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> requiredPermissionsTirmaisu
      else -> requiredPermissionsUpsideDownCake
    }

    val notGranted = permissions.filter {
      ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
    }

    return if (notGranted.isEmpty()) {
      // Semua permission sudah diberikan
      true
    } else {
      // Masih ada yang belum diberikan → request ke user
      ActivityCompat.requestPermissions(activity, notGranted.toTypedArray(), 1001)
      false
    }
  }
}