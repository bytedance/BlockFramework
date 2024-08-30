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

/**
 *
 * @author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

object BlockInit {

    private var config: IBlockInitConfig = IBlockInitConfig.DefaultInitConfig()

    fun useTreeMessageCenterEnable(): Boolean {
        return config.useTreeMessageCenterEnable()
    }

    fun allTaskMustRunOnMain(): Boolean {
        return config.allTaskMustRunOnMain()
    }

    fun recordEnable(): Boolean {
        return config.recordEnable()
    }

    fun logOptEnable(): Boolean {
        return config.logOptEnable()
    }

    fun enableDebugDependencyCheck(): Boolean {
        return config.enableDebugDependencyCheck()
    }

    fun init(config: IBlockInitConfig) {
        this.config = config
        BlockLogger.setLogger(config.getBlockLogger())
    }

    fun recordException(e: Throwable?) {
        config.recordException(e)
    }
}