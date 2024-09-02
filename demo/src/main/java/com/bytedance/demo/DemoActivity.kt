package com.bytedance.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.demo.data.DemoCardData
import com.bytedance.demo.util.DemoTestUtil


/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/26
 * @mail zhoujunjie.9743@bytedance.com
 **/

class DemoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var adapter: DemoAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.demo_activity_layout)
        initView()
    }

    private fun initView() {
        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)
        adapter = DemoAdapter(recyclerView)
        recyclerView.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter?.setData(DemoTestUtil.getTestData())
    }
}