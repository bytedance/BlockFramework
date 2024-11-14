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

import com.bytedance.blockframework.contract.AbstractLifecycleBlock
import com.bytedance.blockframework.framework.core.BlockGenerator
import com.bytedance.blockframework.framework.core.BlockUnitHandler
import com.bytedance.blockframework.framework.core.IBlockModel
import com.bytedance.blockframework.framework.core.message.TreeBlockMessageCenter
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.utils.blockHandler

/**
 *
 * @Author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

open class BaseBlock<DATA, MODEL : IBlockModel<DATA>>(var blockContext: IBlockContext) : AbstractLifecycleBlock() {

    private val blockName by lazy { this::class.java.simpleName + "_" + this.hashCode() }

    internal var parent: BlockUnitHandler? = null
    internal var immediateBind = false

    open var lazyActive = false
    open var isBlockActivated = true
    open var activeTask: (() -> Unit)? = null

    open fun activeIfNeed() {
        if (!isActive()) {
            activeTask?.invoke()
        }
    }

    open fun getBlockKey(): String = blockName

    //<editor-fold desc="Block Lifecycle">
    @Deprecated(message = "please use onRegister()", replaceWith = ReplaceWith("onRegister()"))
    override fun onPrepared() {
        onRegister()
    }
    open fun onRegister() {}
    override fun onCreate() {}
    override fun onStart() {}
    override fun onResume() {}
    override fun onPause() {}
    override fun onStop() {}
    override fun onDestroy() {}
    override fun onUnRegister() {}
    //</editor-fold>

    override fun defineBlockService(): Class<*>? = null
    override fun isActive(): Boolean = isBlockActivated

    open fun bindModel(model: MODEL?) {}

    open fun generateSubBlocks(generator: BlockGenerator) {}

    override fun <T> getBlockService(klass: Class<T>, activeIfNeed: Boolean): T? {
        if (blockBlockMessageCenter is TreeBlockMessageCenter) {
            var handler = blockHandler()
            var impl = handler.queryService(klass, activeIfNeed)
            while (impl == null) {
                handler = handler.attachBlock.parent ?: break
                impl = handler.queryService(klass, activeIfNeed)
            }
            return impl
        }
        return blockBlockMessageCenter.queryService(this, klass, activeIfNeed)
    }

}