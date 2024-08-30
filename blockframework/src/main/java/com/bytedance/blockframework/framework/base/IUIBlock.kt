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
 *
 * @Author: Created by zhoujunjie on 2023/8/27
 * @mail zhoujunjie.9743@bytedance.com
 **/

data class UIBlockConfig(
    var parentId: Int = -1,
    var layoutParams: LayoutParams? = null,
    var createUIOnMainThread: Boolean = false,
    var replaceParent: Boolean = false,
    var viewInflater: BlockInflater = DefaultLayoutInflater
)

interface IUIBlock : LayoutContainer {

    companion object {
        const val USE_PARENT_LAYOUT = -1
    }

    val uiConfig: UIBlockConfig
    override var containerView: View
    fun onCreateView(parent: View?): View
    fun onViewCreated(view: View)
    fun getView(): View?
    fun customAssembleView(currentView: View, parent: View?): Boolean {
        return false
    }
}