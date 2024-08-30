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

/**
 * @Author Wei Zijie
 * @Date 2020/12/28
 * @Description
 */
abstract class StateAndEventModel {

    open lateinit var blockBlockMessageCenter: IBlockMessageCenter

    abstract fun onPrepared()

    abstract fun onUnRegister()

    //<editor-fold desc="Share State">
    val providers: MutableList<IStatusProvider<out State>> = mutableListOf()
    fun <T : State> shareState(provider: IStatusProvider<in T>) {
        if (providers.contains(provider)) {
            throw RuntimeException("provider $provider already exists")
        }
        providers.add(provider as IStatusProvider<State>)
        blockBlockMessageCenter.registerStateProvider(provider)
    }
    fun <T : State> queryStatus(stateClass: Class<T>): T? {
        return blockBlockMessageCenter.queryState(stateClass = stateClass)
    }
    //</editor-fold>

    //<editor-fold desc="Drive Event">
    val observers: MutableList<IObserver<Event>> = mutableListOf()
    open fun subscribe(observer: IObserver<Event>, eventClass: Class<out Event>) {
        if (!observers.contains(observer)) {
            observers.add(observer as IObserver<Event>)
        }
        blockBlockMessageCenter.subscribeEvent(observer, eventClass)
    }

    fun notifyEvent(event: Event) {
        blockBlockMessageCenter.notifyEvent(this, event = event)
    }
    //</editor-fold>
}