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
package com.bytedance.blockframework.framework.monitor

import android.os.SystemClock
import android.util.Log

/**
 *
 * @Author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

interface BlockLogger {

    fun debug(): Boolean

    fun log(tag: String, info: String)

    fun report(tag: String, params: Map<String, String>)

    companion object : BlockLogger {
        private var logger: BlockLogger = DefaultBlockLogger()
        fun setLogger(logger: BlockLogger) {
            this.logger = logger
        }

        override fun debug(): Boolean {
            return logger.debug()
        }

        override fun log(tag: String, info: String) {
            logger.log(tag, info)
        }

        override fun report(tag: String, params: Map<String, String>) {
            logger.report(tag, params)
        }
    }
}

class DefaultBlockLogger : BlockLogger {

    override fun debug(): Boolean {
        return false
    }

    override fun log(tag: String, info: String) {
        if (debug()) {
            Log.d(tag, info)
        }
    }

    override fun report(tag: String, params: Map<String, String>) {

    }
}

fun logger(tag: String, info: String) {
    BlockLogger.log(tag, info)
}

fun currentTime(): Long = SystemClock.elapsedRealtime()

fun currentThread(): Thread = Thread.currentThread()