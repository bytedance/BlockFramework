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

import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.core.IBlockModel
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.monitor.BlockMonitor
import com.bytedance.blockframework.framework.monitor.TYPE_BLOCK_BIND
import com.bytedance.blockframework.framework.monitor.currentTime
import com.bytedance.blockframework.framework.performance.Executor

/**
 * Description: 支持异步Bind的Block
 *
 * @Author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

abstract class AsyncBaseBlock<DATA, MODEL : IBlockModel<DATA>>(blockContext: IBlockContext) : BaseBlock<DATA, MODEL>(blockContext), IAsyncBind<MODEL> {

    override fun bindModel(model: MODEL?) {
        if (enableAsyncBind()) {
            val startSync = currentTime()
            recordPref(TYPE_BLOCK_BIND, "sync_bind_start")
            kotlin.runCatching {
                syncBind(model)
            }.onFailure {
                val params = hashMapOf(
                    "block_key" to getBlockKey(),
                    "enable_async" to "1",
                    "msg" to "sync_bind_exception"
                )
                BlockInit.uploadExceptionOnline(params, it)
            }
            recordPref(TYPE_BLOCK_BIND, "sync_bind_end", currentTime() - startSync)
            Executor.work().post {
                val startAsync = currentTime()
                recordPref(TYPE_BLOCK_BIND, "async_bind_start")
                kotlin.runCatching {
                    asyncBind(model) {
                        val customScheduler = blockContext.getCustomTaskScheduler()
                        if (customScheduler != null) {
                            customScheduler.asyncBindChangeToMain(this@AsyncBaseBlock, it)
                        } else {
                            Executor.main().post {
                                kotlin.runCatching {
                                    it.invoke()
                                }.onFailure { e ->
                                    val params = hashMapOf(
                                        "block_key" to getBlockKey(),
                                        "enable_async" to "1",
                                        "msg" to "async_sync_bind_exception"
                                    )
                                    BlockInit.uploadExceptionOnline(params, e)
                                }
                            }
                        }
                    }
                }.onFailure {
                    val params = hashMapOf(
                        "block_key" to getBlockKey(),
                        "enable_async" to "1",
                        "msg" to "async_bind_exception"
                    )
                    BlockInit.uploadExceptionOnline(params, it)
                }
                recordPref(TYPE_BLOCK_BIND, "async_bind_end", currentTime() - startAsync)
            }
        } else {
            val start = currentTime()
            recordPref(TYPE_BLOCK_BIND, "sync_bind_start")
            kotlin.runCatching {
                syncBind(model)
                asyncBind(model) {
                    it.invoke()
                }
            }.onFailure {
                val params = hashMapOf(
                    "block_key" to getBlockKey(),
                    "enable_async" to "0",
                    "msg" to "sync_bind_exception"
                )
                BlockInit.uploadExceptionOnline(params, it)
            }
            recordPref(TYPE_BLOCK_BIND, "sync_bind_end", currentTime() - start)
        }
    }

    /**
     * 同步Bind
     */
    override fun syncBind(model: MODEL?) {

    }


    /**
     * 异步Bind，根据[enableAsyncBind()]判断是否在子线程执行
     */
    override fun asyncBind(model: MODEL?, syncInvoke: SyncInvoke) {

    }
    //</editor-fold>

    private fun recordPref(type: String, message: String, cost: Long = 0) {
        BlockMonitor.record(blockContext.getScene().getName(), getBlockKey(), type, message, cost)
    }
}