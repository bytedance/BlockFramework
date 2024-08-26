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
import androidx.annotation.WorkerThread
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

class TaskManagerOpt(private val scene: IBlockScene, private val allTaskMustRunOnMain: Boolean): ITaskManager {

    private val TAG = "TaskManagerOpt"

    private val managerName by lazy { this::class.java.simpleName + "_" + this.hashCode() }

    private var main: ExecutorHandler = ExecutorHandler(Executor.main(), false)
    private var work: ExecutorHandler = ExecutorHandler(Executor.work(), false)

    private val mainThreadTasks = PriorityQueue(20,
        Comparator<ScheduleTask> { o1, o2 -> o2.taskPriority.compareTo(o1.taskPriority) })
    private val subThreadTasks = PriorityQueue(20,
        Comparator<ScheduleTask> { o1, o2 -> o2.taskPriority.compareTo(o1.taskPriority) })

    private var allFinished: TasksFinished? = null

    private var isMainTasksFinish = false
    private var isSubTasksFinish = false

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
        isMainTasksFinish = mainThreadTasks.isEmpty()
        isSubTasksFinish = subThreadTasks.isEmpty()
    }

    @MainThread
    override fun handleTasks(mainFinished: TasksFinished?, subFinished: TasksFinished?, allFinished: TasksFinished?) {
        this.allFinished = allFinished
        val start = currentTime()
        BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "start_handle_tasks")
        if (!subThreadTasks.isEmpty()) {
            work.post {
                processTaskInSubThread(subFinished, mainFinished, start)
            }
        }
        if (!mainThreadTasks.isEmpty()) {
            processMainThreadTasks(mainFinished, start)
        }
    }

    @MainThread
    private fun processMainThreadTasks(mainFinished: TasksFinished?, start: Long) {
        val resultTasks = mutableListOf<ScheduleTask>()
        val startRun = currentTime()
        BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "start_handle_main_tasks")
        do {
            val targetTask: ScheduleTask? = mainThreadTasks.poll()
            targetTask?.let {
                it.run()
                resultTasks.add(it)
            }
            if (mainThreadTasks.isEmpty()) {
                BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "mainTasks_end", currentTime() - startRun)
                mainFinished?.invoke(resultTasks)
                isMainTasksFinish = true
                // mainTasks执行完成后，如果subTask已经执行完毕，则回调allFinished
                if (isSubTasksFinish) {
                    allFinished?.invoke(allTasks)
                    BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "allTask_main_end", currentTime() - start)
                }
            }
        } while (!mainThreadTasks.isEmpty())
    }

    @WorkerThread
    private fun processTaskInSubThread(subFinished: TasksFinished?, mainFinished: TasksFinished?, start: Long) {
        val resultTasks = mutableListOf<ScheduleTask>()
        val failTasks = mutableListOf<ScheduleTask>()
        val startRun = currentTime()
        BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "start_handle_sub_tasks")
        do {
            val targetTask: ScheduleTask? = subThreadTasks.poll()
            runCatching {
                targetTask?.let {
                    it.run()
                    resultTasks.add(it)
                }
            }.onFailure {
                // 子线程任务出现异常时，将任务移到主线程的队列中
                targetTask?.let { it1 ->
                    failTasks.add(it1)
                }
                handleException(targetTask, it)
            }
            if (subThreadTasks.isEmpty()) {
                if (failTasks.size > 0) {
                    main.post {
                        mainThreadTasks.addAll(failTasks)
                        isMainTasksFinish = false
                        processMainThreadTasks(mainFinished, -1)
                    }
                }
                // 子线程任务执行完成
                BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "subTasks_end", currentTime() - startRun)
                main.post {
                    subFinished?.invoke(resultTasks)
                    isSubTasksFinish = true
                    // subTasks执行完成后，如果mainTask已经执行完毕，则回调allFinished
                    if (isMainTasksFinish) {
                        BlockMonitor.record(scene.getName(), managerName, TYPE_BLOCK_RUN_TASK, "allTask_sub_end", currentTime() - start)
                        allFinished?.invoke(allTasks)
                    }
                }
            }
        } while (!subThreadTasks.isEmpty())
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