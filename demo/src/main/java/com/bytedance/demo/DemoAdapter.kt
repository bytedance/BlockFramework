package com.bytedance.demo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.demo.data.DemoCardData

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/27
 * @mail zhoujunjie.9743@bytedance.com
 **/

class DemoAdapter(val recyclerView: RecyclerView) : RecyclerView.Adapter<DemoHolder>() {

    private var data = arrayListOf<DemoCardData>()

    @SuppressLint("NotifyDataSetChanged")
    fun setData(data: ArrayList<DemoCardData>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.demo_card_holder_layout, parent, false)
        return DemoHolder(itemView).apply {
            initCardBlock(recyclerView)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: DemoHolder, position: Int) {
        holder.bindData(data[position])
    }

    override fun onViewDetachedFromWindow(holder: DemoHolder) {
        holder.onDetachedFromWindow()
    }
}