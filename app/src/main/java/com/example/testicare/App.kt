package com.example.testicare

import android.app.Application
import cc.cicare.sdkcall.CiCareSdkCall

/**
 * Created by Kharozim
 * 02/03/26 - kharozim.wrk@gmail.com
 * Copyright (c) 2026. Test Icare
 * All Rights Reserved
 */
class App : Application() {
 override fun onCreate() {
  super.onCreate()
  CiCareSdkCall.init(this)
  CiCareSdkCall.setAPI(
    baseUrl = "https://sdk-gateway.c-icare.cc",
    token = "da57cceb27dda58f263c7b31d370e5350cf23afd5140f0fc9f2fe76713c1b562.3b84ad65bc5368c494d59c58fdca6a0971ad3d2453ad4f55f45ddd17"
  )
 }
}