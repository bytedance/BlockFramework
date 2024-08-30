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
package com.bytedance.blockframework.framework.join

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.core.BlockCoreManager
import com.bytedance.blockframework.framework.core.BlockSupervisor
import com.bytedance.blockframework.framework.core.IBlockModel
import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.framework.task.BlockInflater
import com.bytedance.blockframework.framework.task.DefaultInflaterProvider
import com.bytedance.blockframework.framework.utils.uploadException
import com.bytedance.blockframework.interaction.Event
import com.bytedance.blockframework.interaction.IBlockMessageCenter

/**
 * Block framework context, establishing internal and external connections within the Block framework
 *
 * @author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

interface IBlockContext {
    fun getScene(): IBlockScene
    fun setMessageCenter(center: IBlockMessageCenter)
    fun getMessageCenter(): IBlockMessageCenter
    fun findBlockSupervisor(block: BaseBlock<*,*>): BlockSupervisor
    fun setLayoutInflater(inflater: BlockInflater)
    fun getLayoutInflater(): BlockInflater
    fun <T : IBlockDepend> registerDepend(clazz: Class<T>, depend: T)
    fun <T : IBlockDepend> findDepend(clazz: Class<T>): T
    fun <T : IBlockDepend> findDependOrNull(clazz: Class<T>): T?
    fun <T> getBlockService(clazz: Class<T>) : T?
    fun notifyEvent(event: Event)
    fun addAfterBindTask(action: ()->Unit)
}

class BlockContextImpl(private val blockScene: IBlockScene) : IBlockContext {

    private val blockCore = BlockCoreManager()
    private var inflater: BlockInflater? = null
    private val dependMap : MutableMap<Class<*>, IBlockDepend> = mutableMapOf()

    internal fun initRootBlock(context: Context, rootBlock: BaseBlock<*,*>, messageCenter: IBlockMessageCenter, parent: View? = null) {
        blockCore.initScene(blockScene)
        blockCore.initRootBlock(context, rootBlock, messageCenter, parent)
    }

    override fun getScene(): IBlockScene {
        return blockScene
    }

    override fun <T : IBlockDepend> registerDepend(clazz: Class<T>, depend: T) {
        if (dependMap.containsKey(clazz)) {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("registerService $clazz already exists"), true)
            }
        }
        dependMap[clazz] = depend
    }

    fun <D, M : IBlockModel<D>> bindBlockModel(model: M) {
        blockCore.bindBlockModel(model)
    }

    internal fun dispatchLifecycleState(state: Lifecycle.State) {
        blockCore.dispatchLifecycleState(state)
    }

    override fun <T : IBlockDepend> findDepend(clazz: Class<T>): T {
        return if (!dependMap.containsKey(clazz)) {
            throw Exception()
        } else {
            dependMap[clazz] as T
        }
    }

    override fun <T : IBlockDepend> findDependOrNull(clazz: Class<T>): T? {
        return dependMap[clazz] as? T
    }

    override fun <T> getBlockService(clazz: Class<T>): T? {
        return blockCore.getBlockService(clazz)
    }

    override fun notifyEvent(event: Event) {
        blockCore.notifyEvent(event)
    }

    override fun setMessageCenter(center: IBlockMessageCenter) {
        blockCore.setMessageCenter(center)
    }

    override fun getMessageCenter(): IBlockMessageCenter {
        return blockCore.getMessageCenter()
    }

    override fun findBlockSupervisor(block: BaseBlock<*,*>): BlockSupervisor {
        return blockCore.findBlockSupervisor(block)
    }

    override fun setLayoutInflater(inflater: BlockInflater) {
        this.inflater = inflater
    }

    override fun getLayoutInflater(): BlockInflater {
        return inflater ?: DefaultInflaterProvider.getInflater()
    }

    override fun addAfterBindTask(action: ()->Unit) {
        return blockCore.addAfterBindTask(action)
    }
}