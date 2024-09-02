package com.bytedance.demo.data

import androidx.annotation.DrawableRes

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/28
 * @mail zhoujunjie.9743@bytedance.com
 **/


data class DemoCardData(
    @DrawableRes val avatarImg: Int = -1,
    val userName: String = "",
    val title: String = "",
    val praiseCount: String = "",
    val commentCount: String = "",
    val collectCount: String = "",
    val mainContentBg: Int = 0,
)