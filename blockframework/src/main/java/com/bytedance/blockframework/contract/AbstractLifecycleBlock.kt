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
package com.bytedance.blockframework.contract

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.bytedance.blockframework.framework.monitor.BlockLogger

/**
 * @Author Wei Zijie
 * @Date 2020/12/8
 * @Description
 */
abstract class AbstractLifecycleBlock() : AbstractBlock(), LifecycleOwner {

    private val TAG = this.javaClass.simpleName
    private val DEBUG = BlockLogger.debug()

    private val mLifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }

    //<editor-fold desc="onCreate">
    fun performCreate() {
        onCreate()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    open fun onCreate() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onCreate")
        }
    }
    //</editor-fold>

    //<editor-fold desc="onStart">
    fun performStart() {
        onStart()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
    }

    open fun onStart() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onStart")
        }
    }
    //</editor-fold>

    //<editor-fold desc="onResume">
    fun performResume() {
        onResume()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    open fun onResume() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onResume")
        }
    }
    //</editor-fold>

    //<editor-fold desc="onPause">
    fun performPause() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        onPause()
    }

    open fun onPause() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onPause")
        }
    }
    //</editor-fold>

    //<editor-fold desc="onStop">
    fun performStop() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        onStop()
    }

    open fun onStop() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onStop")
        }
    }
    //</editor-fold>

    //<editor-fold desc="onDestroy">
    fun performDestroy() {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        onDestroy()
    }

    open fun onDestroy() {
        if (DEBUG) {
            BlockLogger.log(TAG, "onDestroy")
        }
    }
    //</editor-fold>
}