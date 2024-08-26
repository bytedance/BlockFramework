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
package com.bytedance.blockframework.interaction

import com.bytedance.blockframework.contract.AbstractBlock
import com.bytedance.blockframework.contract.BlockImplWrapper
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.framework.utils.findSupervisor
import com.bytedance.blockframework.framework.utils.uploadException

/**
 * 以CoreTreeLayerBlock为层级节点，限制能力依赖流向的MessageCenter
 */
abstract class TreeConstrainBlockMessageCenter: BaseBlockMessageCenter() {


    override fun <T> registerService(klass: Class<T>, blockImplWrapper: BlockImplWrapper) {
        var block = blockImplWrapper.impl as? BaseBlock<*, *>
        if (block?.parent == null) {
            super.registerService(klass, blockImplWrapper)
            return
        } else {
            block.parent?.registerService(klass, blockImplWrapper)
        }
    }

    override fun <T> queryService(block: AbstractBlock, klass: Class<T>, activeIfNeed: Boolean): T? {
        if (block !is BaseBlock<*, *>) return super.queryService(klass, activeIfNeed)

        var res: T? = null
        //root节点无parent，直接判断
        if (block.parent == null) {
            res = queryService(klass, activeIfNeed)
            if(res != null) return res
        }

        res = block.parent?.queryService(klass, activeIfNeed)
        if(res != null) return res


        var nextTargetCoreTreeBlock: CoreTreeLayerBlock? = null
        (block.parent ?: block.findSupervisor()).getChildBlocks().forEach {
            if(it is CoreTreeLayerBlock) {
                nextTargetCoreTreeBlock = it
                return@forEach
            }
        }
        res = (nextTargetCoreTreeBlock as? BaseBlock<*, *>)?.findSupervisor()?.queryService(klass, activeIfNeed)
        if(res != null) return res

        val result = queryTargetCoreTreeService((nextTargetCoreTreeBlock as? BaseBlock<*, *>), klass, activeIfNeed)
        if (result == null) {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("TreeConstrainBlockMessageCenter queryService $klass not find"), false)
            }
            if (BlockLogger.debug() && BlockInit.enableDebugDependencyCheck()) {
                //测试环境抛出不合理的依赖
                var rootBlock = block.parent?.attachBlock
                while (rootBlock?.parent != null) {
                    rootBlock = rootBlock.parent?.attachBlock
                }
                val resultFromAllBlock = rootBlock?.let { queryService(it, klass) }
                if (resultFromAllBlock != null) {
                    handleUnexpectedServiceDepend(block, klass)
                }
            }
        }
        if (activeIfNeed) (result as? BaseBlock<*, *>)?.activeIfNeed()
        return result
    }

    open fun handleUnexpectedServiceDepend(block: BaseBlock<*, *>?, klass: Class<*>) {}

    private fun <T> queryTargetCoreTreeService(block: BaseBlock<*, *>?, klass: Class<T>, activeIfNeed: Boolean): T? {
        block ?: return null
        var res: T? = null
        var nextTargetCoreTreeBlock: CoreTreeLayerBlock? = null
        block.findSupervisor()?.getChildBlocks()?.forEach {
            if(it is CoreTreeLayerBlock) {
                nextTargetCoreTreeBlock = it
                return@forEach
            }
        }
        nextTargetCoreTreeBlock ?: return null
        res = (nextTargetCoreTreeBlock as? BaseBlock<*, *>)?.findSupervisor()?.queryService(klass, activeIfNeed)
        if(res != null) return res

        return queryTargetCoreTreeService(nextTargetCoreTreeBlock as? BaseBlock<*, *>, klass, activeIfNeed)
    }

}