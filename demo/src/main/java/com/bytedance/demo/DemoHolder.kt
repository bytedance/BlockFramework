/*
 * Copyright (C) 2024 Bytedance Ltd. and/or its affiliates
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bytedance.demo

import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.join.IBlockJoin
import com.bytedance.blockframework.framework.join.IBlockScene
import com.bytedance.blockframework.framework.join.bindBlockModel
import com.bytedance.blockframework.framework.join.dispatchOnCreate
import com.bytedance.blockframework.framework.join.dispatchOnDestroy
import com.bytedance.blockframework.framework.join.dispatchOnPause
import com.bytedance.blockframework.framework.join.dispatchOnResume
import com.bytedance.blockframework.framework.join.dispatchOnStart
import com.bytedance.blockframework.framework.join.dispatchOnStop
import com.bytedance.blockframework.framework.join.getBlockContext
import com.bytedance.blockframework.framework.join.initRootBlock
import com.bytedance.blockframework.framework.join.registerDepend
import com.bytedance.demo.block.DemoCardRootBlock
import com.bytedance.demo.data.DemoCardData
import com.bytedance.demo.data.DemoModel
import com.bytedance.demo.depend.IHolderBlockDepend

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/28
 * @mail zhoujunjie.9743@bytedance.com
 **/

class DemoHolder(itemView: View) : RecyclerView.ViewHolder(itemView), IBlockJoin {

    private var ownerRecyclerView: RecyclerView? = null
    private lateinit var rootBlock: DemoCardRootBlock

    override val blockContext: IBlockContext by lazy {
        getBlockContext(blockScene)
    }

    override val blockScene: IBlockScene by lazy {
        BlockScene.DEMO_HOLDER_SCENE
    }

    private val holderDepend: IHolderBlockDepend = object : IHolderBlockDepend {
        override fun enableAsyncBind(): Boolean {
            return true
        }

        override fun getHolderView(): View {
            return itemView
        }
    }

    fun initCardBlock(recyclerView: RecyclerView) {
        ownerRecyclerView = recyclerView
        rootBlock = DemoCardRootBlock(itemView, blockContext).apply {
            registerDepend(holderDepend)
        }
        initRootBlock(itemView.context, rootBlock)
        dispatchOnCreate()
        dispatchOnStart()
        dispatchOnResume()
        observeLifeCycle()
    }

    private fun observeLifeCycle() {
        (itemView.context as? LifecycleOwner)?.lifecycle?.addObserver(
            object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    when (event) {
                        Lifecycle.Event.ON_CREATE -> dispatchOnCreate()
                        Lifecycle.Event.ON_START -> dispatchOnStart()
                        Lifecycle.Event.ON_RESUME -> dispatchOnResume()
                        Lifecycle.Event.ON_PAUSE -> dispatchOnPause()
                        Lifecycle.Event.ON_STOP -> dispatchOnStop()
                        Lifecycle.Event.ON_DESTROY -> dispatchOnDestroy()
                        else -> {}
                    }
                }
            }
        )
    }

    fun onDetachedFromWindow() {
        dispatchOnStop()
        dispatchOnDestroy()
    }

    fun bindData(data: DemoCardData) {
        bindBlockModel(DemoModel(data))
    }

}