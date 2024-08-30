package com.bytedance.demo.util

import com.bytedance.demo.R
import com.bytedance.demo.data.DemoCardData

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/8/29
 * @mail zhoujunjie.9743@bytedance.com
 **/

object DemoTestUtil {


    fun getTestData(): ArrayList<DemoCardData> {
        return ArrayList<DemoCardData>().apply {
            add(DemoCardData(
                avatarImg = R.drawable.avatar_img1,
                userName = "Mary",
                title = "这是用户Mary的一条内容",
                praiseCount = "101",
                commentCount = "56",
                collectCount = "292"
            ))
            add(DemoCardData(
                avatarImg = R.drawable.avatar_img2,
                userName = "Jerry",
                title = "这是用户Jerry的一条内容",
                praiseCount = "1.3w",
                commentCount = "6342",
                collectCount = "2.5w"
            ))
            add(DemoCardData(
                avatarImg = R.drawable.avatar_img1,
                userName = "Bob",
                title = "这是用户Bob的一条内容",
                praiseCount = "2.4w",
                commentCount = "1.1w",
                collectCount = "3.2w"
            ))
            add(DemoCardData(
                avatarImg = R.drawable.avatar_img1,
                userName = "Cindy",
                title = "这是用户Cindy的一条内容",
                praiseCount = "2.8w",
                commentCount = "9876",
                collectCount = "1.5w"
            ))
            add(DemoCardData(
                avatarImg = R.drawable.avatar_img1,
                userName = "Jack",
                title = "这是用户Jack的一条内容",
                praiseCount = "11.4w",
                commentCount = "1.1w",
                collectCount = "6.8w"
            ))
            add(DemoCardData(
                avatarImg = R.drawable.avatar_img1,
                userName = "Rem",
                title = "这是用户Rem的一条内容",
                praiseCount = "9300",
                commentCount = "2345",
                collectCount = "7542"
            ))
        }
    }
}