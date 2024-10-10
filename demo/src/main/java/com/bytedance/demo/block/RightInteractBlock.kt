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

import android.graphics.Typeface
import android.view.View
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

class RightInteractBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {

    private val holderDepend by findDepend<IHolderBlockDepend>()

    private val mainContentBlock: IMainContentBlockService? by blockService()

    private lateinit var diggContainer: View
    private lateinit var commentContainer: View
    private lateinit var collectContainer: View
    private lateinit var moreContainer: View
    private lateinit var diggCount: TextView
    private lateinit var commentCount: TextView
    private lateinit var collectCount: TextView

    override fun layoutResource(): Int {
        return R.layout.demo_right_interact_block_layout
    }

    override fun onViewCreated(view: View) {
        diggContainer = view.findViewById(R.id.digg_container)
        commentContainer = view.findViewById(R.id.comment_container)
        collectContainer = view.findViewById(R.id.collect_container)
        moreContainer = view.findViewById(R.id.more_container)
        diggCount = view.findViewById(R.id.digg_text)
        commentCount = view.findViewById(R.id.comment_text)
        collectCount = view.findViewById(R.id.collect_text)
        diggContainer.setOnClickListener {
            mainContentBlock?.changeMainContent("On Click Praise!")
        }
        commentContainer.setOnClickListener {
            mainContentBlock?.changeMainContent("On Click Comment!")
        }
        collectContainer.setOnClickListener {
            mainContentBlock?.changeMainContent("On Click Collect!")
        }
        moreContainer.setOnClickListener {
            mainContentBlock?.changeMainContent("On Click More!")
        }
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

    override fun onRegister() {
        subscribe(this, ChangFontThemeEvent::class.java)
    }

    override fun onEvent(event: Event): Boolean {
        when (event) {
            is ChangFontThemeEvent -> {
                when (event.type) {
                    FontType.Normal -> {
                        diggCount.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                        commentCount.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                        collectCount.typeface = Typeface.defaultFromStyle(Typeface.NORMAL)
                    }

                    FontType.Bold -> {
                        diggCount.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        commentCount.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                        collectCount.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    }
                }
            }
        }
        return super.onEvent(event)
    }
}