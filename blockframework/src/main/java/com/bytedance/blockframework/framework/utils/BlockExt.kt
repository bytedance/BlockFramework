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
package com.bytedance.blockframework.framework.utils

import android.os.Looper
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.core.BlockUnitHandler
import com.bytedance.blockframework.framework.core.message.TreeBlockMessageCenter
import com.bytedance.blockframework.framework.join.IBlockDepend
import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.framework.performance.ThreadProcessor
import com.bytedance.blockframework.interaction.BaseBlockMessageCenter
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 *
 * @author Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

fun getDefaultCenter() = if (BlockInit.useTreeMessageCenterEnable()) TreeBlockMessageCenter() else BaseBlockMessageCenter()

fun BaseBlock<*, *>.blockHandler(): BlockUnitHandler {
    return blockContext.findBlockHandler(this)
}

inline fun <reified T> BaseBlock<*, *>.blockService(): ReadOnlyProperty<BaseBlock<*, *>, T?> {
    return object : ReadOnlyProperty<BaseBlock<*, *>, T?> {
        private var cacheV: T? = null

        override fun getValue(thisRef: BaseBlock<*, *>, property: KProperty<*>): T? {
            if (cacheV == null) {
                cacheV = getBlockService(T::class.java)
            }
            return cacheV
        }
    }
}

inline fun <reified T : IBlockDepend> BaseBlock<*, *>.findDepend(): ReadOnlyProperty<BaseBlock<*, *>, T> {
    return object : ReadOnlyProperty<BaseBlock<*, *>, T> {
        private lateinit var cacheV: T

        override fun getValue(thisRef: BaseBlock<*, *>, property: KProperty<*>): T {
            if (!::cacheV.isInitialized) {
                cacheV = blockContext.findDepend(T::class.java)
            }
            return cacheV
        }
    }
}

fun BaseBlock<*, *>.traverseBlock(action: (block: BaseBlock<*, *>) -> Unit) {
    action(this)
    this.blockHandler().getChildBlocks().forEach {
        it.traverseBlock(action)
    }
}

fun BaseBlock<*, *>.traverseSubBlock(action: (block: BaseBlock<*, *>) -> Unit) {
    this.blockHandler().getChildBlocks().forEach {
        it.traverseBlock(action)
    }
}

fun BaseBlock<*, *>.afterBind(action: () -> Unit) {
    blockContext.addAfterBindTask(action)
}

fun syncInvoke(action: () -> Unit) {
    if (Looper.getMainLooper() == Looper.myLooper()) {
        action()
    } else {
        ThreadProcessor.main().post(action)
    }
}

fun uploadException(exception: Throwable, needThrow: Boolean) {
    if (!BlockLogger.debug()) {
        return
    }

    if (needThrow) {
        throw exception
    }
}