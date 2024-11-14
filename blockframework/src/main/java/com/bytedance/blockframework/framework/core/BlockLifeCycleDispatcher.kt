package com.bytedance.blockframework.framework.core

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.utils.LifecycleUtil.handleLifecycleState
import com.bytedance.blockframework.framework.utils.blockHandler

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/11/14
 * @mail zhoujunjie.9743@bytedance.com
 **/

class BlockLifeCycleDispatcher(private val parentBlock: BaseBlock<*, *>) : LifecycleEventObserver {

    private val TAG = "BlockLifeCycleDispatcher"

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> onCreate()
            Lifecycle.Event.ON_START -> onStart()
            Lifecycle.Event.ON_RESUME -> onResume()
            Lifecycle.Event.ON_PAUSE -> onPause()
            Lifecycle.Event.ON_STOP -> onStop()
            Lifecycle.Event.ON_DESTROY -> onDestroy()
            else -> Unit
        }
    }

    private fun onCreate() {
        parentBlock.blockHandler().getChildBlocks().forEach {
            handleLifecycleState(Lifecycle.State.CREATED, it)
        }
    }

    private fun onStart() {
        parentBlock.blockHandler().getChildBlocks().filter { !it.lazyActive }.forEach {
            handleLifecycleState(Lifecycle.State.STARTED, it)
        }
    }

    private fun onResume() {
        parentBlock.blockHandler().getChildBlocks().filter { !it.lazyActive }.forEach {
            handleLifecycleState(Lifecycle.State.RESUMED, it)
        }
    }

    private fun onPause() {
        parentBlock.blockHandler().getChildBlocks().filter { !it.lazyActive }.forEach {
            handleLifecycleState(Lifecycle.State.STARTED, it)
        }
    }

    private fun onStop() {
        parentBlock.blockHandler().getChildBlocks().filter { !it.lazyActive }.forEach {
            handleLifecycleState(Lifecycle.State.CREATED, it)
        }
    }

    private fun onDestroy() {
        parentBlock.blockHandler().getChildBlocks().filter { !it.lazyActive }.forEach {
            handleLifecycleState(Lifecycle.State.DESTROYED, it)
        }
        parentBlock.lifecycle.removeObserver(this)
    }

}