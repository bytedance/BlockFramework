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

import android.view.ViewGroup
import androidx.annotation.IdRes
import com.bytedance.blockframework.framework.task.BlockInflater
import com.bytedance.blockframework.framework.task.DefaultLayoutInflater

/**
 * Block配置
 *
 * @author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

open class BaseBlockBuilder<Block : BaseBlock<*,*>> {

    /**
     * Block对象实例
     */
    lateinit var instance: () -> Block

    /**
     * 表示Block能否添加的条件，一般用于同一个Block被复用到不同场景时判断
     *  如：condition = { scene == InnerStream }，表示只有内流场景才会添加该Block，其他场景不会添加
     */
    var condition: () -> Boolean = { true }

    /**
     * UIBlock对应的占位ID，该Block对于的View会添加到父Block中对应的parentId上
     */
    @IdRes var parentId: Int = -1

    /**
     * UIBlock中，View的布局参数LayoutParams
     */
    var layoutParams: ViewGroup.LayoutParams? = null

    /**
     * UIBlock中，View是否替换占坑的parentView，结合[parentId]使用
     *  replaceParent = true，表示该Block对于的View会直接替换掉父Block中[parentId]对应的View，用于减少布局层级
     */
    var replaceParent = false

    /**
     * UIBlock自定义布局inflate，默认使用[DefaultLayoutInflater]，可自定义实现
     */
    var viewInflater: BlockInflater? = null

    /**
     * UIBlock创建View时是否必须在主线程
     */
    var createUIOnMainThread = false

    /**
     * Block是否立即Bind，默认false，表示会等到View树创建完成后触发Bind
     * 需结合[createUIOnMainThread]使用
     */
    var immediateBind = false

    /**
     * Block是否懒加载
     */
    var lazyActive = false

    internal fun build(): Block = instance()
}

class BlockBuilder : BaseBlockBuilder<BaseBlock<*,*>>()
