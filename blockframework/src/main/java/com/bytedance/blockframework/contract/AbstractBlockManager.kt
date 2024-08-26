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
import com.bytedance.blockframework.interaction.IBlockMessageCenter
import com.bytedance.blockframework.interaction.StateAndEventModel

/**
 * @Author Wei Zijie
 * @Date 2020/12/28
 * @Description
 */
abstract class AbstractBlockManager(var context: Context, override var blockBlockMessageCenter: IBlockMessageCenter) : StateAndEventModel() {

    var blockList: MutableList<AbstractBlock> = mutableListOf()

    abstract fun onRegisterBlock(block: AbstractBlock)
    abstract fun onUnregisterBlock(block: AbstractBlock)

    override fun onPrepared() {}
    override fun onUnRegister() {}

    fun registerBlock(block: AbstractBlock) {
        if (blockList.contains(block)) {
            return
        }
        blockList.add(block)
        block.performInstall(context, blockBlockMessageCenter)
        onRegisterBlock(block)
        block.onPrepared()
    }

    open fun unregisterAllBlock() {
        val mIterator = blockList.iterator()
        while (mIterator.hasNext()) {
            val next = mIterator.next()
            next.performUnRegister()
            onUnregisterBlock(next)
            mIterator.remove()
        }
    }

    fun unregisterBlock(block: AbstractBlock) {
        blockList.remove(block)
        block.performUnRegister()
        onUnregisterBlock(block)
    }

    fun <T> getBlockService(klass: Class<T>): T? {
        return blockBlockMessageCenter.queryService(klass)
    }
}