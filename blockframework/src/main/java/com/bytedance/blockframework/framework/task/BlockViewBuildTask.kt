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
package com.bytedance.blockframework.framework.task

import android.view.View
import android.view.ViewGroup
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.base.IUIBlock
import com.bytedance.blockframework.framework.monitor.BlockMonitor
import com.bytedance.blockframework.framework.monitor.TYPE_BLOCK_VIEW_CREATE
import com.bytedance.blockframework.framework.monitor.currentThread
import com.bytedance.blockframework.framework.monitor.currentTime
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description:
 *
 * @Author: Created by zhoujunjie on 2023/7/18
 * @mail zhoujunjie.9743@bytedance.com
 **/

class BlockViewBuildTask(
    val uiBlock: IUIBlock,
    private val parent: ViewGroup?,
    override val result: TaskResult<View> = TaskResult()
) : Task<View> {

    companion object {
        var sAtomViewTypeCreator = AtomicInteger(100)
    }

    override val mustMainThread: Boolean = uiBlock.uiConfig.createUIOnMainThread

    override fun run() {
        if (parent != null) {
            val startCreate = currentTime()
            BlockMonitor.record(getBlockScene(), (uiBlock as BaseBlock<*, *>).getBlockKey(), TYPE_BLOCK_VIEW_CREATE, "[${currentThread().name}] view_create_start")
            result.value = uiBlock.onCreateView(parent)
            BlockMonitor.record(getBlockScene(), (uiBlock as BaseBlock<*, *>).getBlockKey(), TYPE_BLOCK_VIEW_CREATE, "[${currentThread().name}] view_create_end", currentTime() - startCreate)
        }
    }

    /**
     * uiBlock按照添加的顺序定义优先级，最先添加的Block最先onCreate()
     */
    override val taskPriority: Int = sAtomViewTypeCreator.decrementAndGet()

    fun getBlockScene(): String {
        return (uiBlock as? BaseBlock<*, *>)?.blockContext?.getScene()?.getName() ?: ""
    }
}