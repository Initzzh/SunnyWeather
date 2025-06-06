package com.sunnyweather.android

import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import com.sunnyweather.android.ui.weather.WeatherViewModel
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    lateinit var binding: ActivityWeatherBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 设置背景图与状态融合在一起
        // 1. 设置系统状态栏UI标签
        val decorView = window.decorView
        decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        // 设置状态栏颜色为透明色
        window.statusBarColor = Color.TRANSPARENT

        // layout布局
        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // 滑动菜单监听事件
        binding.includeNow.navBtn.setOnClickListener {
            // 根据系统方向滑动出现
            binding.drawerLayout.openDrawer(GravityCompat.START)

            // 添加drawLayout监听事件
            binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {

                }

                override fun onDrawerOpened(drawerView: View) {

                }
                // drawLayout 点击关闭事件，关闭输入法键盘
                override fun onDrawerClosed(drawerView: View) {
                   val manager = getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
                    // 隐藏输入法
                    manager.hideSoftInputFromWindow(drawerView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS)
                }

                override fun onDrawerStateChanged(newState: Int) {
                }

            })

        }


        // 获取lng, lat
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }

        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }

        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        // 将获取的天气数据更新到UI上
        viewModel.weatherLiveData.observe(this, Observer { result ->
           val weather =  result.getOrNull()
            if (weather != null) {
                // 将数据展示到UI上
                showWeatherInfo(weather)
                Toast.makeText(this,"天气数据加载完毕", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this,"无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            // 当刷新结束后，隐藏刷新条
            binding.swipeRefresh.isRefreshing = false
        })

        // 设置刷新条颜色
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary)
        // 根据lng, lat 刷新天气数据，执行网络请求
        refreshWeather()
        // 监听下拉刷新
        binding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

    }
    fun refreshWeather(){
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        // 显示刷新条
        binding.swipeRefresh.isRefreshing = true
    }
    private fun showWeatherInfo(weather: Weather) {
        binding.includeNow.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // realtime UI data
        binding.includeNow.currentTemp.text = "${realtime.temperature.toInt()} ℃"
        binding.includeNow.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        binding.includeNow.currentAQI.text = currentPM25Text
        binding.includeNow.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // daily UI data
        // first remove all view data
        binding.includeForecast.forecastLayout.removeAllViews()
        // for-in fill data to forecast_item
        val days = daily.skycon.size
        for (i in 0 until days) {
            // day skycon temperature
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]

            // 动态创建forecast_item控件
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item,
                binding.includeForecast.forecastLayout, false)

            val dataInfo = view.findViewById(R.id.dataInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView

            // date simpleDateFormat
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dataInfo.text = simpleDateFormat.format(skycon.date)
            // sky content: skyInfo, skyIcon, skyBg
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            temperatureInfo.text = "${temperature.min.toInt()} ~ ${temperature.max.toInt()}"

            // forecast_item add to forecastLayout
            binding.includeForecast.forecastLayout.addView(view)
        }
        // fill data to life_index.xml
        val lifeIndex = daily.lifeIndex
        binding.includeLifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        binding.includeLifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        binding.includeLifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        binding.includeLifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc

        // set weatherLayout visible
        binding.weatherLayout.visibility  = View.VISIBLE

    }
}