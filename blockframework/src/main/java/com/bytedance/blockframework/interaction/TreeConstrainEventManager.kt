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

import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.framework.utils.findSupervisor

/**
 * EventManager that restricts event flow
 */
open class TreeConstrainEventManager:  EventManager() {

    override fun registerObserver(observer: IObserver<Event>, eventClass: Class<Event>) {
        if (observer !is BaseBlock<*, *>) {
            super.registerObserver(observer, eventClass)
            return
        }
        if (observer.parent == null) {
            observer.findSupervisor().registerObserver(observer, eventClass)
            return
        } else {
            observer.parent?.registerObserver(observer, eventClass)
        }

    }

    override fun notifyEvent(block: StateAndEventModel, event: Event): Boolean {
        if (block !is BaseBlock<*, *>) return super.notifyEvent(block, event)
        var pointerBlock = block as? BaseBlock<*, *>
        while (pointerBlock?.parent != null) {
            pointerBlock.parent?.notifyEvent(event)
            pointerBlock = pointerBlock.parent?.attachBlock
        }
        if (BlockLogger.debug() && BlockInit.enableDebugDependencyCheck()) {
            //测试环境抛出不合理的Event依赖
            if (findLowerObserver(block, event)){
                handleUnexpectedEventDepend(block, event)
            }
        }
        return true
    }

    open fun handleUnexpectedEventDepend(block: StateAndEventModel?, event: Event) {}

    private fun findLowerObserver(block: BaseBlock<*, *>?, event: Event): Boolean {
        block ?: return false
        var nextTargetCoreTreeBlock: CoreTreeLayerBlock? = null
        val supervisor = if (block is CoreTreeLayerBlock) block.findSupervisor() else block.parent
        supervisor?.getChildBlocks()?.forEach {
            if(it is CoreTreeLayerBlock) {
                nextTargetCoreTreeBlock = it
                return@forEach
            }
        }
        nextTargetCoreTreeBlock ?: return false
        val observers = (nextTargetCoreTreeBlock as? BaseBlock<*, *>)?.findSupervisor()?.eventToObserverMap?.get(event.javaClass)
        if (observers?.isNotEmpty() == true)  return true

        return findLowerObserver(nextTargetCoreTreeBlock as? BaseBlock<*, *>, event)
    }
}