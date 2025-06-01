package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object SunnyWeatherNetwork {

    private val placeService = ServiceCreator.create<PlaceService>()


    // 调用网络API服务，执行await()方法解析获取服务器的json数据
    suspend fun searchPlaces(query:String) = placeService.searchPlaces(query).await()

    // 网络接口回调获取json解析后的数据，通过协程简化接口回调代码频繁重写问题；
    private suspend fun <T> Call<T>.await(): T{
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if(body != null)
                        continuation.resume(body)
                    else
                        continuation.resumeWithException(
                            RuntimeException("response body is null")
                        )
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }

            })
        }

    }
}