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
 *
 * @author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

open class BaseBlockBuilder<Block : BaseBlock<*,*>> {

    /**
     * Block Instance
     */
    lateinit var instance: () -> Block

    /**
     * Conditions indicating whether a Block can be added, generally used to determine
     *  when the same Block is reused in different scenarios
     *
     * For example：condition = { scene == InnerStream }，means that only the Block
     *  will be added in InnerStream scenarios, and not in other scenarios
     */
    var condition: () -> Boolean = { true }

    /**
     * The placeholder ID corresponding to UIBlock, and the View to which
     *  the Block is opposed will be added to the corresponding parentId in the parent Block
     */
    @IdRes var parentId: Int = -1

    /**
     * In UIBlock, View is added to the layout parameters of the parent Block
     */
    var layoutParams: ViewGroup.LayoutParams? = null

    /**
     * In UIBlock, whether View replaces the parentView occupying the pit,
     *  please uses it in conjunction with [parentId].
     *  This is mainly used to reduce layout levels.
     */
    var replaceParent = false

    /**
     * UIBlock custom layout inflate, using [DefaultLayoutInflater] by default
     */
    var viewInflater: BlockInflater? = null

    /**
     * UIBlock must create a View on the main thread or not
     */
    var createUIOnMainThread = false

    /**
     * Whether Block Bind immediately, the default is false,
     *  which means that Bind will be triggered after the View tree is created.
     *  please use in conjunction with [createUIOnMainThread]
     */
    var immediateBind = false

    /**
     * Means Block delayed loading
     */
    var lazyActive = false

    internal fun build(): Block = instance()
}

class BlockBuilder : BaseBlockBuilder<BaseBlock<*,*>>()
