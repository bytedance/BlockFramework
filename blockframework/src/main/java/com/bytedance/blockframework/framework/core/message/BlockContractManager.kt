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
package com.bytedance.blockframework.framework.core.message

import android.content.Context
import com.bytedance.blockframework.contract.AbstractBlock
import com.bytedance.blockframework.contract.AbstractBlockManager
import com.bytedance.blockframework.interaction.BaseBlockMessageCenter
import com.bytedance.blockframework.interaction.IBlockMessageCenter

/**
 * @Description: Block通信管理器
 *
 * @Author: Created by zhoujunjie on 2023/8/4
 * @mail zhoujunjie.9743@bytedance.com
 **/

class BlockContractManager(context: Context, var messageCenter: IBlockMessageCenter = BaseBlockMessageCenter()) : AbstractBlockManager(context, messageCenter) {
    override fun onRegisterBlock(block: AbstractBlock) {}

    override fun onUnregisterBlock(block: AbstractBlock) {}
}