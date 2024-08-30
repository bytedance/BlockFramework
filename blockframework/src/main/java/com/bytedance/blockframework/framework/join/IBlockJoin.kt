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
package com.bytedance.blockframework.framework.join

import android.content.Context
import android.view.View
import androidx.lifecycle.Lifecycle
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.core.IBlockModel
import com.bytedance.blockframework.framework.utils.getDefaultCenter
import com.bytedance.blockframework.interaction.IBlockMessageCenter

/**
 *
 * @Author: Created by zhoujunjie on 2023/8/9
 * @mail zhoujunjie.9743@bytedance.com
 **/

interface IBlockJoin {
    val blockContext: IBlockContext
    val blockScene: IBlockScene
}

fun IBlockJoin.initRootBlock(context: Context, rootBlock: BaseBlock<*, *>) {
    (blockContext as? BlockContextImpl)?.initRootBlock(context, rootBlock, getDefaultCenter(), null)
}

fun IBlockJoin.initRootBlock(context: Context, rootBlock: BaseBlock<*, *>, messageCenter: IBlockMessageCenter = getDefaultCenter(), parent: View? = null) {
    (blockContext as? BlockContextImpl)?.initRootBlock(context, rootBlock, messageCenter, parent)
}

inline fun <reified DATA, MODEL : IBlockModel<DATA>> IBlockJoin.bindBlockModel(model: MODEL) {
    (blockContext as? BlockContextImpl)?.bindBlockModel(model)
}

fun IBlockJoin.dispatchBlockLifecycle(state: Lifecycle.State) {
    (blockContext as? BlockContextImpl)?.dispatchLifecycleState(state)
}

fun IBlockJoin.dispatchOnCreate() {
    dispatchBlockLifecycle(Lifecycle.State.CREATED)
}

fun IBlockJoin.dispatchOnStart() {
    dispatchBlockLifecycle(Lifecycle.State.STARTED)
}

fun IBlockJoin.dispatchOnResume() {
    dispatchBlockLifecycle(Lifecycle.State.RESUMED)
}

fun IBlockJoin.dispatchOnPause() {
    dispatchBlockLifecycle(Lifecycle.State.STARTED)
}

fun IBlockJoin.dispatchOnStop() {
    dispatchBlockLifecycle(Lifecycle.State.CREATED)
}

fun IBlockJoin.dispatchOnDestroy() {
    dispatchBlockLifecycle(Lifecycle.State.DESTROYED)
}

inline fun <reified T : IBlockDepend> IBlockJoin.registerDepend(depend: T) {
    blockContext.registerDepend(T::class.java, depend)
}

inline fun <reified T> IBlockJoin.blockService(): T? {
    return blockContext.getBlockService(T::class.java)
}

inline fun <reified T : IBlockDepend> IBlockJoin.findDepend(): T {
    return blockContext.findDepend(T::class.java)
}

inline fun <reified T : IBlockDepend> IBlockJoin.findDependOrNull(): T? {
    return blockContext.findDependOrNull(T::class.java)
}

fun IBlockJoin.getBlockContext(scene: IBlockScene): IBlockContext = BlockContextImpl(scene)

