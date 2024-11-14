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
package com.bytedance.demo.block

import android.view.View
import com.bytedance.blockframework.framework.base.IUIBlock
import com.bytedance.blockframework.framework.base.UIBlock
import com.bytedance.blockframework.framework.core.BlockGenerator
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.demo.R
import com.bytedance.demo.data.DemoCardData
import com.bytedance.demo.data.DemoModel

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/29
 * @mail zhoujunjie.9743@bytedance.com
 **/

class DemoCardRootBlock(private val rootView: View, blockContext: IBlockContext) :
    UIBlock<DemoCardData, DemoModel>(blockContext) {

    override fun layoutResource(): Int {
        return IUIBlock.USE_PARENT_LAYOUT
    }

    override fun onCreateView(parent: View?): View {
        return rootView
    }

    override fun generateSubBlocks(generator: BlockGenerator) {
        generator.generate {
            addBlock {
                instance = {
                    MainContentBlock(blockContext)
                }
                parentId = R.id.main_content_block_container
                // Indicates that the UI must be created on the main thread
                createUIOnMainThread = true
            }
            addBlock {
                instance = {
                    BottomInfoBlock(blockContext)
                }
                parentId = R.id.bottom_info_block_container
                // reduce layout levels
                replaceParent = true
            }
            addBlock {
                instance = {
                    RightInteractBlock(blockContext)
                }
                parentId = R.id.right_interact_block_container
                replaceParent = true
            }
        }
    }
}