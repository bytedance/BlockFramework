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
package com.bytedance.blockframework.framework.base

import android.view.View
import android.view.ViewGroup.LayoutParams
import com.bytedance.blockframework.framework.task.BlockInflater
import com.bytedance.blockframework.framework.task.DefaultLayoutInflater
import kotlinx.android.extensions.LayoutContainer

/**
 * @Description:
 *
 * @Author: Created by zhoujunjie on 2023/8/27
 * @mail zhoujunjie.9743@bytedance.com
 **/

// UI配置
data class UIBlockConfig(
    var parentId: Int = -1, // Block对应的View的占位ID
    var layoutParams: LayoutParams? = null, // Block对应的View的布局参数LayoutParams
    var createUIOnMainThread: Boolean = false, // 是否在主线程创建UI
    var replaceParent: Boolean = false, // 是否替换父View
    var viewInflater: BlockInflater = DefaultLayoutInflater // 自定义ViewInflater
)

interface IUIBlock : LayoutContainer {
    val uiConfig: UIBlockConfig // UIConfig
    override var containerView: View // Block容器View
    fun onCreateView(parent: View?): View
    fun onViewCreated(view: View)
    fun getView(): View?
    fun customAssembleView(currentView: View, parent: View?): Boolean {
        return false
    }
}