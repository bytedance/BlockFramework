package com.bytedance.blockframework.framework.utils

import androidx.lifecycle.Lifecycle
import com.bytedance.blockframework.contract.AbstractLifecycleBlock
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.monitor.logger

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/11/15
 * @mail zhoujunjie.9743@bytedance.com
 **/

object LifecycleUtil {

    fun handleLifecycleState(state: Lifecycle.State, block: BaseBlock<*, *>) {
        logger("LifecycleUtil", "$state --- ${block.getBlockKey()}---${block.lifecycle.currentState}")
        when (state) {
            Lifecycle.State.CREATED -> {
                if (block.lifecycle.currentState < Lifecycle.State.CREATED) {
                    block.performCreate()
                } else {
                    if (block.lifecycle.currentState > Lifecycle.State.STARTED) {
                        block.performPause()
                    }
                    if (block.lifecycle.currentState > Lifecycle.State.CREATED) {
                        block.performStop()
                    }
                }
            }

            Lifecycle.State.STARTED -> {
                if (block.lifecycle.currentState < Lifecycle.State.STARTED) {
                    if ((block as AbstractLifecycleBlock).lifecycle.currentState < Lifecycle.State.CREATED) {
                        block.performCreate()
                    }
                    block.performStart()
                } else if ((block as AbstractLifecycleBlock).lifecycle.currentState > Lifecycle.State.STARTED) {
                    block.performPause()
                }
            }

            Lifecycle.State.RESUMED -> {
                if (block.lifecycle.currentState < Lifecycle.State.RESUMED) {
                    if (block.lifecycle.currentState < Lifecycle.State.CREATED) {
                        block.performCreate()
                    }
                    if (block.lifecycle.currentState < Lifecycle.State.STARTED) {
                        block.performStart()
                    }
                    block.performResume()
                }
            }

            Lifecycle.State.DESTROYED -> {
                if (block.lifecycle.currentState >= Lifecycle.State.RESUMED) {
                    block.performPause()
                }
                if (block.lifecycle.currentState >= Lifecycle.State.STARTED) {
                    block.performStop()
                }
                if (block.lifecycle.currentState >= Lifecycle.State.CREATED) {
                    block.performDestroy()
                }
            }

            else -> Unit
        }
    }
}