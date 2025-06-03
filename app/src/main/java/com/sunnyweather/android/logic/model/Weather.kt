package com.sunnyweather.android.logic.model


// Weather封装实时天气数据和全体天气数据
data class Weather(val realtime: RealtimeResponse.Realtime, val daily: DailResponse.Daily)