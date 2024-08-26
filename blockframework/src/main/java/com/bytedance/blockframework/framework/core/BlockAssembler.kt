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
package com.bytedance.blockframework.framework.core

import android.view.ViewGroup
import com.bytedance.blockframework.framework.base.BlockBuilder
import com.bytedance.blockframework.framework.base.IUIBlock
import com.bytedance.blockframework.framework.core.message.BlockContractManager
import com.bytedance.blockframework.framework.task.BlockViewBuildTask
import com.bytedance.blockframework.framework.utils.BlockDsl
import com.bytedance.blockframework.framework.utils.findSupervisor

/**
 * Block组装器
 *
 * @author: Created by zhoujunjie on 2023/8/7
 * @mail zhoujunjie.9743@bytedance.com
 **/

interface BlockAssembler {
    fun assemble(action: SubBlocksAdder.() -> Unit)
}

interface SubBlocksAdder {
    @BlockDsl
    fun addBlock(builder: BlockBuilder.() -> Unit)
}

internal class BlockAssemblerImpl(
    private val supervisor: BlockSupervisor?,
    private val contractManager: BlockContractManager,
    private val uiTasks: MutableList<BlockViewBuildTask>
) : BlockAssembler {
    override fun assemble(action: SubBlocksAdder.() -> Unit) {
        val adder = object : SubBlocksAdder {
            override fun addBlock(builder: BlockBuilder.() -> Unit) {
                supervisor?.apply {
                    val config = BlockBuilder().apply(builder)
                    if (!config.condition()) {
                        return@apply
                    }
                    val block = config.build()
                    block.immediateBind = config.immediateBind
                    if (block is IUIBlock) {
                        block.uiConfig.parentId = config.parentId
                        block.uiConfig.layoutParams = config.layoutParams
                        block.uiConfig.replaceParent = config.replaceParent
                        block.uiConfig.createUIOnMainThread = config.createUIOnMainThread
                        // 子线程创建UI的Block不能进行immediateBind
                        if (!config.createUIOnMainThread) {
                            block.immediateBind = false
                        }
                        if (config.viewInflater != null) {
                            block.uiConfig.viewInflater = config.viewInflater!!
                        } else {
                            block.uiConfig.viewInflater = block.blockContext.getLayoutInflater()
                        }

                        var parentView = getAttachView()
                        if (config.parentId != -1) {
                            parentView = parentView?.findViewById(config.parentId)
                        }
                        if (block.lazyActive) {
                            block.activeTask = {
                                val task = BlockViewBuildTask(block, parentView as? ViewGroup)
                                task.run()
                                val target = task.result.value
                                block.findSupervisor().apply {
                                    installView(target)
                                    activeView()
                                }
                            }
                        } else {
                            uiTasks.add(BlockViewBuildTask(block, parentView as? ViewGroup))
                        }
                    }
                    loadBlock(block)
                    contractManager.registerBlock(block)
                    block.assembleSubBlocks(BlockAssemblerImpl(block.findSupervisor(), contractManager, uiTasks))
                }
            }
        }
        adder.action()
    }
}