package com.bytedance.demo.block

import android.view.View
import android.widget.TextView
import com.bytedance.blockframework.framework.async.AsyncUIBlock
import com.bytedance.blockframework.framework.async.SyncInvoke
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.utils.findDepend
import com.bytedance.demo.R
import com.bytedance.demo.data.DemoCardData
import com.bytedance.demo.data.DemoModel
import com.bytedance.demo.depend.IHolderBlockDepend

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/29
 * @mail zhoujunjie.9743@bytedance.com
 **/

class RightInteractBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {

    private val holderDepend by findDepend<IHolderBlockDepend>()

    private lateinit var diggCount: TextView
    private lateinit var commentCount: TextView
    private lateinit var collectCount: TextView

    override fun layoutResource(): Int {
        return R.layout.demo_right_interact_block_layout
    }

    override fun onViewCreated(view: View) {
        diggCount = view.findViewById(R.id.digg_text)
        commentCount = view.findViewById(R.id.comment_text)
        collectCount = view.findViewById(R.id.collect_text)
    }

    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }

    override fun asyncBind(model: DemoModel?, syncInvoke: SyncInvoke) {
        syncInvoke {
            diggCount.text = model?.data?.praiseCount
            commentCount.text = model?.data?.commentCount
            collectCount.text = model?.data?.collectCount
        }
    }
}