package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

class PlaceViewModel: ViewModel() {
    private val searchLiveData = MutableLiveData<String>()

    // 用来缓存place数据
    val placeList = ArrayList<Place>()

    // 通过监听searchLiveData的值变化，执行Repository.searchPlaces获取不可变的List<Place>的LiveData对象，供activity观察
    val placeLiveData = Transformations.switchMap(searchLiveData){ query ->
        // 返回一个List<Place>的LiveData对象
        Repository.searchPlaces(query)
    }


    fun searchPlaces(query: String){
        searchLiveData.value = query
    }

    // 对Repository的savePlace,getSavePlace, isSavedPlace进行封装
    fun savePlace(place: Place) = Repository.savePlace(place)
    fun getSavePlace() = Repository.getSavePlace()
    fun isPlaceSaved() = Repository.isPlaceSaved()
}