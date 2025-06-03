package com.sunnyweather.android.ui.place

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.WeatherActivity
import com.sunnyweather.android.databinding.FragmentPlaceBinding

class PlaceFragment: Fragment() {

    private lateinit var binding: FragmentPlaceBinding


    // PlaceViewModel

    // 共有的，可以通过placeFragment获取viewModel
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }
    private lateinit var placeAdapter: PlaceAdapter


    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // 创建Fragment的View
        binding = FragmentPlaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("FragmentLiveDataObserve")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // 当Fragment关联的Activity创建时，加载Fragment里的控件

        // 当Sharedpreferences保存了地点，则直接跳转该地点的天气详情
        if ( viewModel.isPlaceSaved() ) {
            val place = viewModel.getSavePlace()
            val intent = Intent(activity, WeatherActivity::class.java).apply {
                putExtra("place_name", place.name)
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
            }
            startActivity(intent)
            activity?.finish()
            return

        }

        // 设置recycleView的layoutManager和adapter
        // activity: getActivity()获取Fragment关联的Activity
        val layoutManager = LinearLayoutManager(activity)
        layoutManager.orientation=LinearLayoutManager.VERTICAL
        binding.recycleView.layoutManager = layoutManager
        // 设置adapter
        placeAdapter = PlaceAdapter(this, viewModel.placeList)
        binding.recycleView.adapter = placeAdapter

        // searchEdit 监听text变化
        binding.searchPlaceEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                viewModel.searchPlaces(content)
            } else {
                // 输入框为空,则不显示RecyclView
                binding.recycleView.visibility = View.GONE
                binding.bgImageView.visibility = View.VISIBLE
                // content为空,清空所有placeList
                viewModel.placeList.clear()
                placeAdapter.notifyDataSetChanged()
            }
        }

        // 检测ViewModel数据变化,更新UI数据
        viewModel.placeLiveData.observe(this, Observer { result ->
            val places = result.getOrNull()
            if (places != null){
                // 给RecycleView的dapter的placeList赋值
                binding.recycleView.visibility = View.VISIBLE
                binding.bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                placeAdapter.notifyDataSetChanged()
            } else {
                // 网络请求结果place为空,则说明未能查询到地址,不显示RecyclView
                Toast.makeText(activity, "未能查询到任务地点", Toast.LENGTH_SHORT).show()
                // result的exception不为空,则执行printStackTrace, 否则不执行
                result.exceptionOrNull()?.printStackTrace()

            }


        })


    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onDetach() {
        super.onDetach()
    }
}

