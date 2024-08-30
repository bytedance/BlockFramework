package com.bytedance.demo.block

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.blockframework.framework.async.AsyncUIBlock
import com.bytedance.blockframework.framework.async.SyncInvoke
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.utils.findDepend
import com.bytedance.demo.R
import com.bytedance.demo.data.DemoCardData
import com.bytedance.demo.data.DemoModel
import com.bytedance.demo.depend.IHolderBlockDepend
import com.bytedance.demo.event.AvatarClickEvent

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/29
 * @mail zhoujunjie.9743@bytedance.com
 **/

class BottomInfoBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {

    private val holderDepend by findDepend<IHolderBlockDepend>()

    private lateinit var avatarView: ImageView
    private lateinit var userView: TextView
    private lateinit var titleView: TextView

    override fun layoutResource(): Int {
        return R.layout.demo_bottom_info_block_layout
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        avatarView = view.findViewById(R.id.avatar)
        userView = view.findViewById(R.id.user_name)
        titleView = view.findViewById(R.id.title)
    }

    private fun initView() {
        val avatar = findViewById<View>(R.id.avatar)
        avatar?.setOnClickListener {
            notifyEvent(AvatarClickEvent())
        }
    }

    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun asyncBind(model: DemoModel?, syncInvoke: SyncInvoke) {
        syncInvoke {
            avatarView.setImageDrawable(
                context.resources.getDrawable(
                    model?.data?.avatarImg ?: R.color.content_1
                )
            )
            userView.text = model?.data?.userName
            titleView.text = model?.data?.title
        }
    }
}