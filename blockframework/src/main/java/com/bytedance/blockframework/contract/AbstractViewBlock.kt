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
package com.bytedance.blockframework.contract

import android.view.View
import androidx.annotation.IdRes
import com.bytedance.blockframework.interaction.BaseBlockMessageCenter

/**
 * @Author Wei Zijie
 * @Date 2020/12/28
 * @Description
 */
abstract class AbstractViewBlock<M : BaseBlockMessageCenter, T : View>(private val mContentView: T) : AbstractBlock() {

    protected val contentView: T
        get() = mContentView

    protected fun <T : View> findViewById(@IdRes id: Int): T {
        return mContentView.findViewById(id)
    }

    abstract fun getView(): View

}