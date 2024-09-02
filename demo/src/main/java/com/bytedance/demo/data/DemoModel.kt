package com.bytedance.demo.data

import com.bytedance.blockframework.framework.core.IBlockModel

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/28
 * @mail zhoujunjie.9743@bytedance.com
 **/

data class DemoModel(
    override val data: DemoCardData
) : IBlockModel<DemoCardData>