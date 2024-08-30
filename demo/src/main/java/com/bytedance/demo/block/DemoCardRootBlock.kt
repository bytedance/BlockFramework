package com.bytedance.demo.block

import android.view.View
import com.bytedance.blockframework.framework.base.IUIBlock
import com.bytedance.blockframework.framework.base.UIBlock
import com.bytedance.blockframework.framework.core.BlockAssembler
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

    override fun assembleSubBlocks(assembler: BlockAssembler) {
        assembler.assemble {
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