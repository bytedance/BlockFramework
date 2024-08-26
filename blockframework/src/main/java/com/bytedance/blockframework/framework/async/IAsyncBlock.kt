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
package com.bytedance.blockframework.framework.async

import androidx.annotation.MainThread


/**
 * @Description:
 *
 * @Author: Created by zhoujunjie on 2023/3/7
 * @mail zhoujunjie.9743@bytedance.com
 **/

/**
 * 切换到主线程
 */
typealias SyncInvoke = (() -> Unit) -> Unit



interface IAsyncBind<MODEL> {
    /**
     * 表示Block是否支持asyncBind，子Block必须实现
     */
    fun enableAsyncBind(): Boolean

    /**
     * 同步Bind，主线程执行
     */
    @MainThread
    fun syncBind(model: MODEL?)

    /**
     * 异步Bind，根据[enableAsyncBind()]判断是否在子线程执行
     * @param syncInvoke: 用于切换到主线程
     */
    fun asyncBind(model: MODEL?, syncInvoke: SyncInvoke)
}

/**
 * 可以由外界设置的任务调度器
 */
interface ICustomTaskScheduler {
    /**
     * asyncBind的时候把消息抛回主线程
     */
    fun asyncBindChangeToMain(caller: Any?, task: (() -> Unit))

    /**
     * 提交一个任务到主线程，该任务跟别的类似的任务会被打散执行
     * @param onlyForHiddenWhenBindCard 是否只有在卡片bind的时候不可见才post执行
     *                                  如果传入true，并且卡片在bind的时候是可见的，那么任务会被直接执行
     */
    fun summitBreakUpMainTask(onlyForHiddenWhenBindCard: Boolean, simpleLogInfo: String?, task: Runnable)

    /**
     * 提交延迟任务到主线程，任务会在闲时执行
     */
    fun summitDelayMainTask(simpleLogInfo: String?, task: Runnable)

    /**
     * 提交一个延迟任务到子线程，任务会在闲时执行
     */
    fun summitDelayWorkerThreadTask(simpleLogInfo: String?, task: Runnable)

    /**
     * 卡片在bind的时候是否不可见
     */
    fun isHiddenWhenBind(): Boolean

    fun getOriginScheduler(): Any?
}