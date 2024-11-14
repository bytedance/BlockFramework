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
import android.view.ViewGroup
import com.bytedance.blockframework.contract.AbstractBlock
import com.bytedance.blockframework.contract.BlockImplWrapper
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.base.IUIBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.framework.utils.syncInvoke
import com.bytedance.blockframework.framework.utils.uploadException
import com.bytedance.blockframework.interaction.Event
import com.bytedance.blockframework.interaction.IObserver

/**
 *
 * @Author: Created by zhoujunjie on 2023/7/17
 * @mail zhoujunjie.9743@bytedance.com
 **/

class BlockUnitHandler internal constructor(
    val context: Context,
    val attachBlock: BaseBlock<*, *>,
    private val lifecycleHandler: BlockLifeCycleDispatcher
) {

    companion object {
        fun create(context: Context, block: BaseBlock<*, *>) = BlockUnitHandler(context, block, BlockLifeCycleDispatcher(block))
    }

    private val childList: MutableList<BaseBlock<*, *>> = mutableListOf()
    private val serviceMap: MutableMap<Class<*>, BlockImplWrapper> = mutableMapOf()
    val eventToObserverMap: MutableMap<Class<Event>, MutableList<IObserver<Event>>> = mutableMapOf()

    fun registerObserver(observer: IObserver<Event>, eventClass: Class<Event>) {
        // update event to observer map
        val observers = eventToObserverMap[eventClass] ?: run {
            mutableListOf<IObserver<Event>>().apply {
                eventToObserverMap[eventClass] = this
            }
        }
        if (!observers.contains(observer)) {
            observers.add(observer)
        }
    }

    fun notifyEvent(event: Event): Boolean {
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

    fun attachLifecycle() {
        val blockLifecycle = attachBlock.lifecycle
        syncInvoke {
            blockLifecycle.removeObserver(lifecycleHandler)
            blockLifecycle.addObserver(lifecycleHandler)
        }
    }

    fun loadBlock(subBlock: BaseBlock<*, *>) {
        subBlock.parent = this
        childList.add(subBlock)
    }

    fun installView(target: View) {
        val block = attachBlock as? IUIBlock ?: return
        val parentView = getAttachView()
        if (block.customGenerateView(target, parentView)) {
            block.containerView = target
            return
        }
        if (block.uiConfig.parentId == -1) {
            block.containerView = target
            return
        }

        val placeHolderView = parentView?.findViewById<View>(block.uiConfig.parentId)
        if (placeHolderView == null) {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("${(block as? BaseBlock<*, *>)?.getBlockKey() ?: ""} placeHolder is Null"), true)
            }
            return
        }
        if (block.uiConfig.replaceParent) {
            replaceSelfWithView(target, placeHolderView)
        } else {
            addViewInPlaceholder(target, placeHolderView, block.uiConfig.layoutParams)
        }
        block.containerView = target
    }

    private fun replaceSelfWithView(view: View, placeHolder: View) {
        val parent = placeHolder.parent as? ViewGroup ?: return
        val index = parent.indexOfChild(placeHolder)
        parent.removeViewInLayout(placeHolder)
        val layoutParams = placeHolder.layoutParams
        if (layoutParams != null) {
            parent.addView(view, index, layoutParams)
        } else {
            parent.addView(view, index)
        }
    }

    private fun addViewInPlaceholder(view: View, placeHolder: View, layoutParams: ViewGroup.LayoutParams? = null) {
        val containerView = placeHolder as? ViewGroup ?: return
        if (layoutParams != null) {
            containerView.addView(view, layoutParams)
        } else {
            containerView.addView(view)
        }
    }

    fun activeView() {
        val block = attachBlock as? IUIBlock ?: return
        block.getView()?.let {
            block.onViewCreated(it)
            (block as BaseBlock<*, *>).isBlockActivated = true
        }
    }

    fun getAttachView(): View? {
        var block: BaseBlock<*, *>? = attachBlock
        var attachView: View? = null
        var find = false
        while (!find) {
            attachView = (block as? IUIBlock)?.getView()
            block = block?.parent?.attachBlock
            find = attachView != null || block == null
        }
        return attachView
    }

    fun <T> registerService(klass: Class<T>, blockImplWrapper: BlockImplWrapper) {
        if (serviceMap.containsKey(klass)) {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("registerService $klass already exists"), true)
            }
        }
        serviceMap[klass] = blockImplWrapper
    }

    fun <T> queryService(klass: Class<T>, activeIfNeed: Boolean = true): T? {
        return if (serviceMap.contains(klass)) {
            val target = serviceMap[klass]?.impl
            if(activeIfNeed) (target as? BaseBlock<*,*>)?.activeIfNeed()
            serviceMap[klass]?.impl as T?
        } else {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                uploadException(RuntimeException("queryService $klass not find"), false)
            }
            null
        }
    }

    fun getChildBlocks(): List<BaseBlock<*, *>> = childList
}