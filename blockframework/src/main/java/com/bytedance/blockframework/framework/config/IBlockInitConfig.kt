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
package com.bytedance.blockframework.framework.config

import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.framework.monitor.DefaultBlockLogger

/**
 *
 * @Author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

interface IBlockInitConfig {

    fun recordEnable(): Boolean

    fun useTreeMessageCenterEnable(): Boolean

    fun getBlockLogger(): BlockLogger

    fun allTaskMustRunOnMain(): Boolean

    fun logOptEnable(): Boolean

    fun enableDebugDependencyCheck(): Boolean

    fun recordException(e: Throwable?)

    class DefaultInitConfig : IBlockInitConfig {

        override fun recordEnable(): Boolean {
            return false
        }

        override fun useTreeMessageCenterEnable(): Boolean {
            return true
        }

        override fun getBlockLogger(): BlockLogger = DefaultBlockLogger()

        override fun allTaskMustRunOnMain(): Boolean {
            return true
        }

        override fun logOptEnable(): Boolean {
            return false
        }

        override fun enableDebugDependencyCheck(): Boolean {
            return true
        }

        override fun recordException(e: Throwable?) {

        }
    }
}