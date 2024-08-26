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
import com.bytedance.blockframework.framework.utils.uploadException

/**
 * @Author Wei Zijie
 * @Date 2020/12/28
 * @Description
 */
open class BaseBlockMessageCenter : IBlockMessageCenter {

    val TAG = "BaseBlockMessageCenter"

    //<editor-fold desc="StateProvide">
    override var stateProviderMap: MutableMap<Class<out State>, IStatusProvider<out State>> = mutableMapOf()
    override fun <T : State> registerStateProvider(provider: IStatusProvider<T>) {
        val stateClass = provider.stateClass
        if (!stateProviderMap.contains(stateClass)) {
            stateProviderMap[stateClass] = provider
        } else {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("registerStateProvider $provider for state $stateClass already exists"), true)
            }
        }
    }

    override fun <T : State> unregisterStateProvider(provider: IStatusProvider<T>) {
        val stateClass = provider.stateClass
        if (stateProviderMap.contains(stateClass)) {
            stateProviderMap.remove(stateClass)
        }
    }

    override fun <T : State> queryState(stateClass: Class<T>): T? {
        return if (stateProviderMap.contains(stateClass)) {
            stateProviderMap[stateClass]?.state as T?
        } else {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("queryState $stateClass not find"), true)
            }
            null
        }
    }
    //</editor-fold>

    //<editor-fold desc="event">
    open protected var eventManager: EventManager = EventManager()
    override fun <T : Event> subscribeEvent(observer: IObserver<in T>, eventClass: Class<T>) {
        eventManager.registerObserver(
                observer = observer as IObserver<Event>,
                eventClass = eventClass as Class<Event>
        )
    }

    override fun unregisterObserver(observer: IObserver<Event>) {
        eventManager.unregisterObserver(observer)
    }

    override fun <T : Event> notifyEvent(block: StateAndEventModel, event: T): Boolean {
        return eventManager.notifyEvent(block, event)
    }

    @Deprecated("ignore")
    override fun <T : Event> notifyEvent(event: T): Boolean {
        return eventManager.notifyEvent(event = event)
    }

    override fun <T : Event> notifyEventForResult(event: T): Boolean {
        return eventManager.notifyEventForResult(event)
    }

    override fun <T : Event> notifyEventForResult(block: StateAndEventModel, event: T): Boolean {
        return notifyEventForResult(event)
    }
    //</editor-fold>

    override var blockServiceMap: MutableMap<Class<*>, BlockImplWrapper> = mutableMapOf()
    override fun <T> registerService(klass: Class<T>, blockImplWrapper: BlockImplWrapper) {
        if (blockServiceMap.containsKey(klass)) {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("registerService $klass already exists"), true)
            }
        }
        blockServiceMap[klass] = blockImplWrapper
    }

    override fun <T> queryService(block: AbstractBlock, klass: Class<T>, activeIfNeed: Boolean): T? {
        return queryService(klass)
    }

    @Deprecated("ignore")
    override fun <T> queryService(klass: Class<T>, activeIfNeed: Boolean): T? {
        if (blockServiceMap.contains(klass)) {
            val target = blockServiceMap[klass]?.impl
            if(activeIfNeed) (target as? BaseBlock<*,*>)?.activeIfNeed()
            return target as T?
        }

        for (implWrapper in blockServiceMap.values) {
            if (klass.isInstance(implWrapper.impl)) {
                return implWrapper.impl as T?
            }
        }

        return null
    }
}