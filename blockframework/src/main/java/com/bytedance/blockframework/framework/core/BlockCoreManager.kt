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

import android.content.Context
import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.base.IUIBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.core.message.BlockContractManager
import com.bytedance.blockframework.framework.event.AllBlockViewCreatedEvent
import com.bytedance.blockframework.framework.join.IBlockScene
import com.bytedance.blockframework.framework.monitor.BlockMonitor
import com.bytedance.blockframework.framework.monitor.TYPE_BLOCK_TREE_BIND
import com.bytedance.blockframework.framework.monitor.TYPE_BLOCK_TREE_CREATE
import com.bytedance.blockframework.framework.monitor.TYPE_BLOCK_TREE_INIT
import com.bytedance.blockframework.framework.monitor.TYPE_BLOCK_VIEW_CREATE
import com.bytedance.blockframework.framework.monitor.currentTime
import com.bytedance.blockframework.framework.task.BlockViewBuildTask
import com.bytedance.blockframework.framework.task.ITaskManager
import com.bytedance.blockframework.framework.task.TaskManager
import com.bytedance.blockframework.framework.utils.getDefaultCenter
import com.bytedance.blockframework.framework.utils.traverseBlock
import com.bytedance.blockframework.interaction.Event
import com.bytedance.blockframework.interaction.IBlockMessageCenter

/**
 *
 * @author Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

internal class BlockCoreManager {

    private val tagName by lazy { this::class.java.simpleName + "_" + this.hashCode() }

    private lateinit var context: Context
    private lateinit var rootBlock: BaseBlock<*, *>
    private lateinit var scene: IBlockScene
    private var rootView: View? = null

    private lateinit var blockContractManager: BlockContractManager
    private var messageCenter: IBlockMessageCenter? = null
    private lateinit var taskManager: ITaskManager
    private var uiTasks: MutableList<BlockViewBuildTask> = mutableListOf()
    private var blockHandlerMap: MutableMap<BaseBlock<*, *>, BlockUnitHandler> = mutableMapOf()

    private var isViewCreateCompleted = false
    private var pendingBindTask: (() -> Unit)? = null
    private var hasBind = false
    private val afterBindTasks = ArrayList<() -> Unit>()

    fun initScene(scene: IBlockScene) {
        this.scene = scene
    }

    @MainThread
    fun initRootBlock(context: Context, rootBlock: BaseBlock<*, *>, messageCenter: IBlockMessageCenter, parentView: View? = null) {
        val startInit = currentTime()
        BlockMonitor.record(scene.getName(), tagName, TYPE_BLOCK_TREE_INIT, "init_start")
        check(rootBlock.parent == null) { "${rootBlock::class.java.simpleName} is rootBlock and can't have a parent" }
        this.context = context
        this.rootBlock = rootBlock
        rootBlock.immediateBind = true
        taskManager = TaskManager(scene, BlockInit.allTaskMustRunOnMain())
        blockContractManager = BlockContractManager(context, messageCenter ?: getDefaultCenter())
        blockContractManager.registerBlock(rootBlock)
        if (rootBlock is IUIBlock) {
            val startCreate = currentTime()
            BlockMonitor.record(scene.getName(), rootBlock.getBlockKey(), TYPE_BLOCK_VIEW_CREATE, "view_create_start")
            rootBlock.containerView = rootBlock.onCreateView(parentView)
            BlockMonitor.record(scene.getName(), rootBlock.getBlockKey(), TYPE_BLOCK_VIEW_CREATE, "view_create_end", currentTime() - startCreate)
            rootBlock.onViewCreated(rootBlock.containerView)
            rootView = rootBlock.containerView
        }
        rootBlock.isBlockActivated = true
        rootBlock.generateSubBlocks(BlockGeneratorImpl(findBlockHandler(rootBlock), blockContractManager, uiTasks))
        handleBlockTasks()
        BlockMonitor.record(scene.getName(), tagName, TYPE_BLOCK_TREE_INIT, "init_end", currentTime() - startInit)
    }

    private fun handleBlockTasks() {
        taskManager.addTask(uiTasks)
        val startBuild = currentTime()
        BlockMonitor.record(scene.getName(), tagName, TYPE_BLOCK_TREE_CREATE, "build_start")
        taskManager.handleTasks(
            mainFinished = { result ->
                result.forEach {
                    if (it is BlockViewBuildTask) {
                        val target = it.result.value
                        buildUIBlock(target, it.uiBlock)
                    }
                }
            },
            subFinished = { result ->
                result.forEach {
                    if (it is BlockViewBuildTask) {
                        val target = it.result.value
                        buildUIBlock(target, it.uiBlock)
                    }
                }
            },
            allFinished = {
                isViewCreateCompleted = true
                BlockMonitor.record(scene.getName(), tagName, TYPE_BLOCK_TREE_CREATE, "build_end", currentTime() - startBuild)
                pendingBindTask?.invoke()
                pendingBindTask = null
                rootView?.let {
                    blockContractManager.notifyEvent(AllBlockViewCreatedEvent(it))
                }
            }
        )
    }

    private fun buildUIBlock(target: View, uiBlock: IUIBlock) {
        findBlockHandler(uiBlock as BaseBlock<*, *>).apply {
            installView(target)
            activeView()
        }
    }

    @MainThread
    fun <D, M : IBlockModel<D>> bindBlockModel(model: M) {
        val startBind = currentTime()
        BlockMonitor.record(scene.getName(), tagName, TYPE_BLOCK_TREE_BIND, "bind_start")
        hasBind = false
        immediateBindInner(rootBlock as BaseBlock<D, M>, model)
        if (isViewCreateCompleted) {
            pendingBindTask = null
            bindInner(rootBlock as BaseBlock<D, M>, model)
            BlockMonitor.record(scene.getName(), tagName, TYPE_BLOCK_TREE_BIND, "bind_end", currentTime() - startBind)
        } else {
            pendingBindTask = {
                bindInner(rootBlock as BaseBlock<D, M>, model)
                BlockMonitor.record(scene.getName(), tagName, TYPE_BLOCK_TREE_BIND, "bind_end", currentTime() - startBind)
            }
        }
    }

    private fun <D, M : IBlockModel<D>> immediateBindInner(block: BaseBlock<D, M>, model: M) {
        block.traverseBlock {
            if (it.immediateBind) {
                if (it is IUIBlock) {
                    if (it.getView() != null) {
                        (it as? BaseBlock<D, M>)?.bindModel(model)
                    } else {
                        it.immediateBind = false
                    }
                } else {
                    (it as? BaseBlock<D, M>)?.bindModel(model)
                }
            }
        }
    }

    private fun <D, M : IBlockModel<D>> bindInner(block: BaseBlock<D, M>, model: M) {
        block.traverseBlock {
            if (!it.immediateBind) {
                (it as? BaseBlock<D, M>)?.bindModel(model)
            }
        }
        hasBind = true
        afterBindTasks.forEach {
            it.invoke()
        }
        afterBindTasks.clear()
    }

    fun addAfterBindTask(action: () -> Unit) {
        if (hasBind) {
            action()
        } else {
            afterBindTasks.add(action)
        }
    }

    fun findBlockHandler(block: BaseBlock<*, *>): BlockUnitHandler {
        var handler = blockHandlerMap[block]
        if (handler == null) {
            if (!this::context.isInitialized) {
                context = block.context
            }
            handler = BlockUnitHandler.create(context, block)
            blockHandlerMap[block] = handler
            handler.attachLifecycle()
        }
        return handler
    }

    fun setMessageCenter(center: IBlockMessageCenter) {
        this.messageCenter = center
    }

    fun getMessageCenter(): IBlockMessageCenter {
        return blockContractManager.messageCenter
    }

    fun <T> getBlockService(clazz: Class<T>): T? {
        return rootBlock.getBlockService(clazz)
    }

    fun notifyEvent(event: Event) {
        blockContractManager.notifyEvent(event)
    }

    fun dispatchLifecycleState(state: Lifecycle.State) {
        findBlockHandler(rootBlock).handleLifecycleState(state, rootBlock)
    }

}