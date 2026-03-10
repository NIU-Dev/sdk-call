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
      baseUrl = "<base url>",
      token = "<token adna>"
    )
  }
}