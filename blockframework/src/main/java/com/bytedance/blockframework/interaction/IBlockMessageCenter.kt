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


/**
 * @Author Wei Zijie
 * @Date 2020/12/20
 * @Description
 */
interface IBlockMessageCenter {

    val stateProviderMap: MutableMap<Class<out State>, IStatusProvider<out State>>
    fun <T : State> registerStateProvider(provider: IStatusProvider<T>)
    fun <T : State> unregisterStateProvider(provider: IStatusProvider<T>)
    fun <T : State> queryState(stateClass: Class<T>): T?
    fun <T : Event> subscribeEvent(observer: IObserver<in T>, eventClass: Class<T>)
    fun unregisterObserver(observer: IObserver<Event>)

    @Deprecated("use Block.notifyEvent(event: T) or notifyEvent(block: StateAndEventModel, event: T)")
    fun <T : Event> notifyEvent(event: T): Boolean
    fun <T : Event> notifyEvent(block: StateAndEventModel, event: T): Boolean
    fun <T : Event> notifyEventForResult(event: T): Boolean
    fun <T : Event> notifyEventForResult(block: StateAndEventModel, event: T): Boolean

    var blockServiceMap: MutableMap<Class<*>, BlockImplWrapper>
    fun <T> registerService(klass: Class<T>, blockImplWrapper: BlockImplWrapper)
    @Deprecated("use block.getBlockService(klass: Class<T>) or queryService(block: AbstractBlock, klass: Class<T>)")
    fun <T> queryService(klass: Class<T>, activeIfNeed: Boolean = true): T?
    fun <T> queryService(block: AbstractBlock, klass: Class<T>, activeIfNeed: Boolean = true): T?
}