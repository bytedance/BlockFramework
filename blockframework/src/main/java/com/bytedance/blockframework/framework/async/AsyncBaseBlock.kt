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
import com.bytedance.blockframework.framework.performance.ThreadProcessor

/**
 *
 * @Author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

abstract class AsyncBaseBlock<DATA, MODEL : IBlockModel<DATA>>(blockContext: IBlockContext) :
    BaseBlock<DATA, MODEL>(blockContext), IAsyncBind<MODEL> {

    override fun bindModel(model: MODEL?) {
        if (enableAsyncBind()) {
            val startSync = currentTime()
            recordPref(TYPE_BLOCK_BIND, "sync_bind_start")
            kotlin.runCatching {
                syncBind(model)
            }.onFailure {
                BlockInit.recordException(it)
            }
            recordPref(TYPE_BLOCK_BIND, "sync_bind_end", currentTime() - startSync)
            ThreadProcessor.work().post {
                val startAsync = currentTime()
                recordPref(TYPE_BLOCK_BIND, "async_bind_start")
                kotlin.runCatching {
                    asyncBind(model) {
                        ThreadProcessor.main().post {
                            kotlin.runCatching {
                                it.invoke()
                            }.onFailure {
                                BlockInit.recordException(it)
                            }
                        }
                    }
                }.onFailure {
                    BlockInit.recordException(it)
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
                BlockInit.recordException(it)
            }
            recordPref(TYPE_BLOCK_BIND, "sync_bind_end", currentTime() - start)
        }
    }

    override fun syncBind(model: MODEL?) {

    }

    override fun asyncBind(model: MODEL?, syncInvoke: SyncInvoke) {

    }
    //</editor-fold>

    private fun recordPref(type: String, message: String, cost: Long = 0) {
        BlockMonitor.record(blockContext.getScene().getName(), getBlockKey(), type, message, cost)
    }
}