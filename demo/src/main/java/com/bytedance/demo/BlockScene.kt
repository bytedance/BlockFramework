package com.bytedance.demo

import com.bytedance.blockframework.framework.join.IBlockScene

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/28
 * @mail zhoujunjie.9743@bytedance.com
 **/

enum class BlockScene : IBlockScene {
    DEMO_HOLDER_SCENE;

    override fun getName(): String {
        return name
    }
}