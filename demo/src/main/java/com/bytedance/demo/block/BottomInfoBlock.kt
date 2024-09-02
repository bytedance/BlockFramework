package com.bytedance.demo.block

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bytedance.blockframework.framework.async.AsyncUIBlock
import com.bytedance.blockframework.framework.async.SyncInvoke
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.utils.blockService
import com.bytedance.blockframework.framework.utils.findDepend
import com.bytedance.blockframework.interaction.Event
import com.bytedance.demo.R
import com.bytedance.demo.data.DemoCardData
import com.bytedance.demo.data.DemoModel
import com.bytedance.demo.depend.IHolderBlockDepend
import com.bytedance.demo.event.ChangFontThemeEvent
import com.bytedance.demo.service.IMainContentBlockService
import com.bytedance.demo.util.FontType

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/29
 * @mail zhoujunjie.9743@bytedance.com
 **/

class BottomInfoBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {

    private val holderDepend by findDepend<IHolderBlockDepend>()

    private val mainContentBlock : IMainContentBlockService? by blockService()

    private lateinit var avatarView: ImageView
    private lateinit var userView: TextView
    private lateinit var titleView: TextView

    override fun layoutResource(): Int {
        return R.layout.demo_bottom_info_block_layout
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        initView(view)
    }

    private fun initView(view: View) {
        avatarView = view.findViewById(R.id.avatar)
        userView = view.findViewById(R.id.user_name)
        titleView = view.findViewById(R.id.title)
        avatarView.setOnClickListener {
            mainContentBlock?.changeMainContent("On Avatar Click!")
        }
        userView.setOnClickListener {
            mainContentBlock?.changeMainContent("On UserName Click!")
        }
        titleView.setOnClickListener {
            mainContentBlock?.changeMainContent("On Title Click!")
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

    override fun onRegister() {
        subscribe(this, ChangFontThemeEvent::class.java)
    }

    override fun onEvent(event: Event): Boolean {
        when (event) {
            is ChangFontThemeEvent -> {
                when (event.type) {
                    FontType.Normal -> {
                        userView.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                        titleView.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    }
                    FontType.Bold -> {
                        userView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        titleView.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    }
                }
            }
        }
        return super.onEvent(event)
    }
}