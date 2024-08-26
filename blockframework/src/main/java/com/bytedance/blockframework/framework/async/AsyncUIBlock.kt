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
package com.bytedance.blockframework.framework.async

import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import com.bytedance.blockframework.framework.base.IUIBlock
import com.bytedance.blockframework.framework.base.UIBlockConfig
import com.bytedance.blockframework.framework.core.IBlockModel
import com.bytedance.blockframework.framework.join.IBlockContext

/**
 * @Description:
 *
 * @Author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

abstract class AsyncUIBlock<DATA, MODEL : IBlockModel<DATA>>(blockContext: IBlockContext) : AsyncBaseBlock<DATA, MODEL>(blockContext), IUIBlock {

    companion object {
        const val USE_PARENT_LAYOUT = -1
    }

    override val uiConfig: UIBlockConfig = UIBlockConfig()

    override lateinit var containerView: View

    override fun onCreateView(parent: View?): View {
        if (layoutResource() == USE_PARENT_LAYOUT) {
            return parent!!
        }
        return uiConfig.viewInflater.getView(layoutResource(), parent!!.context, parent as ViewGroup)
    }

    /**
     * UIBlock 必须返回layoutResource()，若复用parentView，可返回 USE_PARENT_LAYOUT
     */
    abstract fun layoutResource(): Int

    override fun onViewCreated(view: View) {}

    override fun getView(): View? {
        return if (this::containerView.isInitialized) {
            containerView
        } else {
            null
        }
    }

    fun <T : View> findViewById(@IdRes id: Int): T? {
        return getView()?.findViewById(id)
    }
}