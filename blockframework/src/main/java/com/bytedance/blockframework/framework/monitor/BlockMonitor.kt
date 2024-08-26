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

import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.join.IBlockScene
import java.util.Vector

/**
 * description: Block框架品质打点
 *  * 1. 支持线下环境统计行为、任务耗时
 *  * 2. 支持线上埋点分析，和线下调试Log
 *
 * @author Created by zhoujunjie on 2024/3/7
 * @mail zhoujunjie.9743@bytedance.com
 **/

data class JobRecord(
    val scene: String = "",
    val key: String = "",
    val type: String = "",
    val message: String = "",
    val cost: Long = 0L
) {
    override fun toString(): String {
        if (cost <= 0) {
            return "$scene -- $key -- $type -- $message"
        }
        return "$scene -- $key -- $type -- $message -- cost: ${cost}ms"
    }
}

const val TYPE_BLOCK_TREE_INIT = "pref_init_block_tree" // 初始化Block树
const val TYPE_BLOCK_TREE_CREATE = "pref_create_block_tree" // 构建完成整颗Block树
const val TYPE_BLOCK_TREE_BIND = "pref_bind_block_tree" // Bind完成整颗Block树
const val TYPE_BLOCK_VIEW_CREATE = "pref_create_block_view" // 每个Block创建View
const val TYPE_BLOCK_RUN_TASK = "pref_create_run_task" // Task执行
const val TYPE_BLOCK_BIND = "pref_bind_block" // 每个Block Bind

object BlockMonitor {

    private val recordMap = HashMap<String, Vector<JobRecord>>()

    fun record(scene: String, key: String, type: String, message: String = "", cost: Long = 0L) {
        if (!BlockInit.recordEnable()) return
        if (cost <= 0) {
            logger(scene, "$scene -- $key -- $type -- $message")
        } else {
            logger(scene, "$scene -- $key -- $type -- $message -- cost: ${cost}ms")
        }
    }

    /**
     * 线下分析各个阶段
     */
    fun doctor(scene: IBlockScene) {
        recordMap[scene.getName()]?.let {
            it.forEach { record ->
                logger(record.key, record.toString())
            }
        } ?: logger(scene.getName(), "There no BlockTree in this scene!")
    }

    /**
     * 遍历map，上报各个scene中，每颗Block树的init耗时、buildView耗时、bind耗时
     */
    fun report() {
        recordMap.forEach { record ->
            val treeInitTime = record.value.first { it.type == TYPE_BLOCK_TREE_INIT }?.cost ?: 0L
            val treeBuildViewTime = record.value.first { it.type == TYPE_BLOCK_TREE_CREATE }?.cost
                ?: 0L
            val treeBindTime = record.value.first { it.type == TYPE_BLOCK_TREE_BIND }?.cost ?: 0L
            BlockLogger.report(record.key, hashMapOf(
                "block_tree_init_cost" to treeInitTime.toString(),
                "block_tree_create_view_cost" to treeBuildViewTime.toString(),
                "block_tree_bind_cost" to treeBindTime.toString()
            ))
        }
    }

}

