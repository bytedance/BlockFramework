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

/**
 * @Author Wei Zijie
 * @Date 2020/12/23
 * @Description
 */
abstract class Event(val canIntercept: Boolean = false)

interface IObserver<T : Event> {
    fun onEvent(event: T): Boolean
}

interface IEventManager {
    fun registerObserver(observer: IObserver<Event>, eventClass: Class<Event>)
    fun unregisterObserver(observer: IObserver<Event>)
    fun notifyEvent(event: Event): Boolean
    fun notifyEvent(block: StateAndEventModel, event: Event): Boolean
    fun notifyEventForResult(event: Event): Boolean
}

open class EventManager: IEventManager {
    private val TAG: String = "EventManager"

    protected val eventToObserverMap: MutableMap<Class<Event>, MutableList<IObserver<Event>>> = mutableMapOf()
    protected val observerToEventMap: MutableMap<IObserver<Event>, MutableList<Class<Event>>> = mutableMapOf()

    /**
     * Make observer observe event of Class<Event> type
     */
    override fun registerObserver(observer: IObserver<Event>, eventClass: Class<Event>) {
        // update event to observer map
        val observers = eventToObserverMap[eventClass] ?: run {
            mutableListOf<IObserver<Event>>().apply {
                eventToObserverMap[eventClass] = this
            }
        }
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
        // update observer to event map
        val events = observerToEventMap[observer] ?: run {
            mutableListOf<Class<Event>>().apply {
                observerToEventMap[observer] = this
            }
        }
        if (!events.contains(eventClass)) {
            events.add(eventClass)
        }
    }

    /**
     * Cancel observer observe any event
     */
    override fun unregisterObserver(observer: IObserver<Event>) {
        if (!observerToEventMap.containsKey(observer)) {
            return
        }
        observerToEventMap[observer]?.forEach {
            eventToObserverMap[it]?.remove(observer)
        }
        observerToEventMap.remove(observer)
    }

    /**
     * Notify event
     */
    override fun notifyEvent(event: Event): Boolean {
        val observers = eventToObserverMap[event.javaClass] ?: return false
        for (observer in observers) {
            if (observer is AbstractBlock) {
                if (!observer.isActive()) {
                    continue
                }
            }
            val intercept = observer.onEvent(event)
            if (intercept && event.canIntercept) {
                break
            }
        }
        return true
    }

    override fun notifyEvent(block: StateAndEventModel, event: Event): Boolean {
        return notifyEvent(event)
    }

    override fun notifyEventForResult(event: Event): Boolean {
        val observers = eventToObserverMap[event.javaClass] ?: return false
        for (observer in observers) {
            if (observer is AbstractBlock) {
                if (!observer.isActive()) {
                    continue
                }
            }
            val intercept = observer.onEvent(event)
            if (intercept && event.canIntercept) {
                return true
            }
        }
        return false
    }
}