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

import com.bytedance.blockframework.contract.BlockImplWrapper
import com.bytedance.blockframework.framework.base.BaseBlock
import com.bytedance.blockframework.interaction.BaseBlockMessageCenter

/**
 *
 * @author Created by zhoujunjie on 2023/8/4
 * @mail zhoujunjie.9743@bytedance.com
 **/

open class TreeBlockMessageCenter : BaseBlockMessageCenter() {

    /**
     * 每次注册service时，将对应的service绑定到当前Block的父Block的SuperVisor中
     */
    override fun <T> registerService(klass: Class<T>, blockImplWrapper: BlockImplWrapper) {
        var block = blockImplWrapper.impl as? BaseBlock<*,*>
        while (block != null) {
            block.parent?.registerService(klass, blockImplWrapper)
            block = block.parent?.attachBlock
        }
    }
}