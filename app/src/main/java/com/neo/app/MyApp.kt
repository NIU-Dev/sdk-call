package com.neo.app

import android.app.Application
import com.neo.sdkcall.NeoSdkCall

/**
 * Created by Kharozim
 * 05/03/26 - kharozim.wrk@gmail.com
 * Copyright (c) 2026. Test Icare
 * All Rights Reserved
 */
class MyApp : Application() {
 override fun onCreate() {
  super.onCreate()
   NeoSdkCall.init(this)
   NeoSdkCall.setAPI(
   baseUrl = "https://sdk-gateway.c-icare.cc",
//    token = "da57cceb27dda58f263c7b31d370e5350cf23afd5140f0fc9f2fe76713c1b562.3b84ad65bc5368c494d59c58fdca6a0971ad3d2453ad4f55f45ddd17"
   token = "da57cceb27dda58f263c7b31d370e5350cf23afd5140f0fc9f2fe76713c1b562.3b84ad65bc5368c494d59c58fdca6a0971ad3d2453ad4f55f45ddd17bcdef0123456789abcdef"
  )
 }
}