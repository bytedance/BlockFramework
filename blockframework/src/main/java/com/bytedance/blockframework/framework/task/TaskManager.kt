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
package com.bytedance.blockframework.framework.task

import androidx.annotation.MainThread
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.framework.config.BlockInit
import com.bytedance.blockframework.framework.join.IBlockScene
import com.bytedance.blockframework.framework.monitor.BlockLogger
import com.bytedance.blockframework.framework.monitor.BlockMonitor
import com.bytedance.blockframework.framework.monitor.TYPE_BLOCK_RUN_TASK
import com.bytedance.blockframework.framework.monitor.currentTime
import com.bytedance.blockframework.framework.monitor.logger
import com.bytedance.blockframework.framework.performance.Executor
import com.bytedance.blockframework.framework.performance.ExecutorHandler
import com.bytedance.blockframework.framework.utils.uploadException
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicInteger

/**
 * Description: Handle Block Task
 *
 * @Author: Created by zhoujunjie on 2023/7/18
 * @mail zhoujunjie.9743@bytedance.com
 **/

class TaskManager(private val scene: IBlockScene, private val allTaskMustRunOnMain: Boolean): ITaskManager {

    private val TAG = "TaskManager"

    private val managerName by lazy { this::class.java.simpleName + "_" + this.hashCode() }

    private var main: ExecutorHandler = ExecutorHandler(Executor.main(), false)
    private var work: ExecutorHandler = ExecutorHandler(Executor.work(), false)

    private val mainThreadTasks = PriorityQueue(1000,
        Comparator<ScheduleTask> { o1, o2 -> o2.taskPriority.compareTo(o1.taskPriority) })
    private val subThreadTasks = PriorityQueue(1000,
        Comparator<ScheduleTask> { o1, o2 -> o2.taskPriority.compareTo(o1.taskPriority) })

    private var allFinished: TasksFinished? = null

    private var taskCount: AtomicInteger? = null

    private var allTasks = listOf<ScheduleTask>()

    @Synchronized
    override fun addTask(tasks: List<ScheduleTask>) {
        allTasks = tasks
        tasks.forEach {
            if (allTaskMustRunOnMain || it.mustMainThread) {
                mainThreadTasks.add(it)
            } else {
                subThreadTasks.add(it)
            }
        }
        taskCount = AtomicInteger(tasks.size)
    }

    @MainThread
    override fun handleTasks(mainFinished: TasksFinished?, subFinished: TasksFinished?, allFinished: TasksFinished?) {
        this.allFinished = allFinished
        val start = currentTime()
        BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "start_handle_tasks")
        processMainThreadTasks(mainFinished, start)
        work.post {
            processTaskInSubThread(subFinished, mainFinished, start)
        }
    }

    private fun processMainThreadTasks(mainFinished: TasksFinished?, start: Long) {
        if (mainThreadTasks.size <= 0) {
            return
        }
        val resultTasks = mutableListOf<ScheduleTask>()
        val startRun = currentTime()
        BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "start_handle_main_tasks")
        do {
            var workEnd: Boolean
            var targetTask: ScheduleTask?
            synchronized(this) {
                targetTask = mainThreadTasks.poll()
                workEnd = mainThreadTasks.isEmpty()
            }
            targetTask?.let {
                it.run()
                resultTasks.add(it)
            }
            if (workEnd) {
                BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "mainTasks_end", currentTime() - startRun)
                mainFinished?.invoke(resultTasks)
            }
            if (taskCount?.decrementAndGet() == 0) {
                // allTask执行完成
                BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "allTask_main_end", currentTime() - start)
                allFinished?.invoke(allTasks)
            }
        } while (!workEnd)
    }

    private fun processTaskInSubThread(subFinished: TasksFinished?, mainFinished: TasksFinished?, start: Long) {
        if (subThreadTasks.size <= 0) {
            return
        }
        val resultTasks = mutableListOf<ScheduleTask>()
        val startRun = currentTime()
        BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "start_handle_sub_tasks")
        do {
            var workEnd: Boolean
            var targetTask: ScheduleTask?
            synchronized(this) {
                targetTask = subThreadTasks.poll()
                workEnd = subThreadTasks.isEmpty()
            }
            runCatching {
                targetTask?.let {
                    it.run()
                    resultTasks.add(it)
                }
            }.onFailure {
                // 子线程任务出现异常时，将任务移到主线程的队列中
                synchronized(this) {
                    mainThreadTasks.add(targetTask)
                }
                taskCount?.incrementAndGet()
                handleException(targetTask, it)
            }
            if (workEnd) {
                // 子线程任务执行完成
                BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "subTasks_end", currentTime() - startRun)
                main.post {
                    subFinished?.invoke(resultTasks)
                    // 子线程执行完成后，如果task数量不为0，则兜底触发主线程执行
                    if ((taskCount?.get() ?: 0) > 0) {
                        processMainThreadTasks(mainFinished, -1)
                    }
                }
            }
            if (taskCount?.decrementAndGet() == 0) {
                // allTask执行完成
                BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "allTask_sub_end", currentTime() - start)
                main.post {
                    allFinished?.invoke(allTasks)
                }
            }
        } while (!workEnd)
    }

    override fun clear() {
        mainThreadTasks.clear()
        subThreadTasks.clear()
    }

    fun handleException(task: ScheduleTask?, t: Throwable) {
        if (task is BlockViewBuildTask) {
            if (!BlockInit.logOptEnable() || BlockLogger.debug()) {
                logger(TAG, "${task.getBlockScene()} build UI Exception in ${Thread.currentThread().name}")
            }
            uploadException(t, true)
            val params = hashMapOf(
                "block_key" to (task.uiBlock as BaseBlock<*, *>).getBlockKey(),
                "msg" to "task_run_exception"
            )
            BlockInit.uploadExceptionOnline(params, t)
        }
    }
}