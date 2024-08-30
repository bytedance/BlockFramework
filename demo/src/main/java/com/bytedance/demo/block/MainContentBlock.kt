package com.bytedance.demo.block

import android.widget.TextView
import com.bytedance.blockframework.framework.async.AsyncUIBlock
import com.bytedance.blockframework.framework.async.SyncInvoke
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.utils.findDepend
import com.bytedance.blockframework.interaction.Event
import com.bytedance.demo.R
import com.bytedance.demo.data.DemoCardData
import com.bytedance.demo.data.DemoModel
import com.bytedance.demo.depend.IHolderBlockDepend
import com.bytedance.demo.event.AvatarClickEvent
import com.bytedance.demo.service.IMainContentBlockService

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/29
 * @mail zhoujunjie.9743@bytedance.com
 **/

class MainContentBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext),
    IMainContentBlockService
{

    private val holderDepend by findDepend<IHolderBlockDepend>()

    override fun layoutResource(): Int {
        return R.layout.demo_main_content_block_layout
    }

    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }

    override fun changeMainContent(content: String) {
        findViewById<TextView>(R.id.content)?.text = content
    }

    override fun defineBlockService(): Class<*>? {
        return IMainContentBlockService::class.java
    }

    override fun syncBind(model: DemoModel?) {

    }
}