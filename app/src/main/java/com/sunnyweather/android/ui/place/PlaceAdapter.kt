package com.sunnyweather.android.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.WeatherActivity
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

class PlaceAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>): RecyclerView.Adapter<PlaceAdapter.ViewHolder>(){

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        val placeName: TextView  = view.findViewById(R.id.placeName)
        val placeAddress: TextView = view.findViewById(R.id.placeAddress)
    }


    // 在onCreateViewHolder中设置监听事件
    // adapter中跳转activity，需要项adapter传入被点击的组件的context,这里即fragment
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent,false)
        val viewHolder = ViewHolder(view)

        // 设置监听事件
        viewHolder.itemView.setOnClickListener{
            // position
            val position = viewHolder.adapterPosition
            val place = placeList[position]
            val activity = fragment.activity
            if (activity is WeatherActivity) {
                // 关闭滑动菜单
                activity.binding.drawerLayout.closeDrawers()
                // 更新activity的 viewModel lng, lat, placeName数据
                activity.viewModel.locationLng = place.location.lng
                activity.viewModel.locationLat = place.location.lat
                activity.viewModel.placeName = place.name
                // 网络请求，刷新天气数据
                activity.refreshWeather()

            } else {

                val intent = Intent(parent.context, WeatherActivity::class.java).apply {
                    putExtra("place_name", place.name)
                    putExtra("location_lng", place.location.lng)
                    putExtra("location_lat", place.location.lat)
                }
                fragment.startActivity(intent)
                activity?.finish()

            }
            //  保存地点
            fragment.viewModel.savePlace(place)

        }

        return viewHolder
    }

    override fun getItemCount(): Int {
        return placeList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // 根据RecycleView的滑动，加载控件数据
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address
    }
}