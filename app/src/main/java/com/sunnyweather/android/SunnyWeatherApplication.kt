package com.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.media.session.MediaSession.Token

class SunnyWeatherApplication: Application() {
    companion object {
        // 调用api的token放入Application的静态变量中
        const val TOKEN = "ESSW47Oq6tqFzsoW"

        @SuppressLint("StaticFieldLeak")
        lateinit var context:Context;
    }

    override fun onCreate(){
        super.onCreate()
        context = applicationContext

    }
}