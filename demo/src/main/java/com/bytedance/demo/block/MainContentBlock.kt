package com.bytedance.demo.block

import android.annotation.SuppressLint
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.bytedance.blockframework.framework.async.AsyncUIBlock
import com.bytedance.blockframework.framework.join.IBlockContext
import com.bytedance.blockframework.framework.utils.findDepend
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

class MainContentBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext),
    IMainContentBlockService
{

    private val holderDepend by findDepend<IHolderBlockDepend>()

    private var mainContentRoot: View? = null
    private var mainContentText: TextView? = null
    private var mainButton: Button? = null
    private var currentFontType: FontType = FontType.Normal

    override fun layoutResource(): Int {
        return R.layout.demo_main_content_block_layout
    }

    override fun onViewCreated(view: View) {
        mainContentRoot = findViewById(R.id.main_content_root)
        mainContentText = findViewById(R.id.content)
        mainButton = findViewById(R.id.button)
        mainButton?.setOnClickListener {
            when (currentFontType) {
                FontType.Normal -> {
                    notifyEvent(ChangFontThemeEvent(FontType.Bold))
                    currentFontType = FontType.Bold
                }
                FontType.Bold -> {
                    notifyEvent(ChangFontThemeEvent(FontType.Normal))
                    currentFontType = FontType.Normal
                }
            }
            Toast.makeText(context, "Switch Font Theme!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }

    override fun changeMainContent(content: String) {
        mainContentText?.text = content
    }

    @SuppressLint("SetTextI18n")
    override fun syncBind(model: DemoModel?) {
        mainContentRoot?.setBackgroundColor(context.resources.getColor(model?.data?.mainContentBg ?: 0))
        mainContentText?.text = "This is Main Content!"
    }

    override fun defineBlockService(): Class<*>? {
        return IMainContentBlockService::class.java
    }
}