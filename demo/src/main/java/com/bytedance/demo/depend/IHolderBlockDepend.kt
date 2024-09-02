package com.bytedance.demo.depend

import android.view.View
import com.bytedance.blockframework.framework.join.IBlockDepend

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/29
 * @mail zhoujunjie.9743@bytedance.com
 **/

interface IHolderBlockDepend : IBlockDepend {
    fun enableAsyncBind(): Boolean
    fun getHolderView(): View
}