package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.dao.PlaceDao
import com.sunnyweather.android.logic.model.DailResponse
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

object Repository {

    //  对placeDao操作进行一层封装

    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavePlace(): Place = PlaceDao.getSavePlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()

    // 返回的时Result<List<Place>> liveData数据;
    // 要获取List<Place>内容,则Result.getOrNull()

    fun searchPlaces(query:String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if(placeResponse.status == "ok"){
            val places = placeResponse.places
            Result.success(places)
        }else{
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    // 获取实时天气+全体天气，并返回LiveData
   fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {
       coroutineScope {
           // 异步， 并行执行
           val deferredRealtime = async {
               SunnyWeatherNetwork.getRealtimeWeather(lng, lat)

           }

           val deferredDaily = async {
               SunnyWeatherNetwork.getDailyWeather(lng, lat)
           }

           // 阻塞，直到两个网络请求全部执行完毕
           val realtimeResponse = deferredRealtime.await()
           val dailyResponse = deferredDaily.await()
           if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
               val weather = Weather(realtimeResponse.result.realtime,
                   dailyResponse.result.daily)
               Result.success(weather)

           } else {
               Result.failure(
                   RuntimeException(
                       "realtime response status is ${realtimeResponse.status}" +
                       "daily response status is ${dailyResponse.status}"
                   )
               )
           }
       }
   }


   fun <T> fire(coroutineContext: CoroutineContext, block: suspend () -> Result<T> ) =
       liveData<Result<T>>(coroutineContext){

       val result = try {
           block()
       } catch (e: Exception) {

           Result.failure<T>(e)
       }
       emit(result)



   }


}