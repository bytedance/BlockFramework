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

import android.content.Context
import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.interaction.Event
import com.bytedance.blockframework.interaction.IBlockMessageCenter
import com.bytedance.blockframework.interaction.IObserver
import com.bytedance.blockframework.interaction.StateAndEventModel

/**
 * @Author Wei Zijie
 * @Date 2020/12/8
 * @Description
 */
abstract class AbstractBlock: StateAndEventModel(), IObserver<Event> {

    private val TAG = this.javaClass.simpleName
    private val DEBUG = BlockLogger.debug()

    /**
     * 1. block在context在registerBlock()时赋值，因而在Block的初始化中无法使用
     * 2. block内的初始化操作如需使用context，可以在onPrepared()方法中进行初始化，可参考简介Tab {@link BriefTabBlock}, 评论Tab {@link DetailCommentBlock}
     */
    open lateinit var context: Context

    abstract fun defineBlockService(): Class<*>?

    abstract fun isActive(): Boolean

    fun performInstall(_context: Context, messageCenter: IBlockMessageCenter) {
        context = _context
        this.blockBlockMessageCenter = messageCenter
        defineBlockService()?.let {
            this.blockBlockMessageCenter.registerService(it, BlockImplWrapper(this))
        }
    }

    override fun onPrepared() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onActive")
        }
    }

    fun performUnRegister() {
        clearState()
        clearEvent()
        clearService()
        onUnRegister()
    }

    override fun onEvent(event: Event): Boolean {
        return false
    }

    open fun <T> getBlockService(klass: Class<T>, activeIfNeed: Boolean = true): T? {
        return blockBlockMessageCenter.queryService(this, klass, activeIfNeed)
    }

    private fun clearState() {
        providers.forEach {
            blockBlockMessageCenter.unregisterStateProvider(it)
        }
        providers.clear()
    }

    private fun clearEvent() {
        observers.forEach {
            blockBlockMessageCenter.unregisterObserver(it)
        }
        observers.clear()
    }

    private fun clearService() {
        blockBlockMessageCenter.blockServiceMap.remove(defineBlockService())
    }

    override fun onUnRegister() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onInactive")
        }
    }
}

class BlockImplWrapper {
    @Volatile
    var impl: Any

    internal constructor(impl: Any) {
        this.impl = impl
    }
}
