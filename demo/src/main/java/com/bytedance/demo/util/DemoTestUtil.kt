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
            add(
                DemoCardData(
                    avatarImg = R.drawable.avatar_img1,
                    userName = "Mary",
                    title = "This is a message from Mary",
                    praiseCount = "101",
                    commentCount = "56",
                    collectCount = "292",
                    mainContentBg = R.color.content_2
                )
            )
            add(
                DemoCardData(
                    avatarImg = R.drawable.avatar_img2,
                    userName = "Jerry",
                    title = "This is an essay from Jerry",
                    praiseCount = "1.3w",
                    commentCount = "6342",
                    collectCount = "2.5w",
                    mainContentBg = R.color.assist_1_dark
                )
            )
            add(
                DemoCardData(
                    avatarImg = R.drawable.avatar_img1,
                    userName = "Bob",
                    title = "This is a picture from Bob",
                    praiseCount = "2.4w",
                    commentCount = "1.1w",
                    collectCount = "3.2w",
                    mainContentBg = R.color.assist_2_dark
                )
            )
            add(
                DemoCardData(
                    avatarImg = R.drawable.avatar_img1,
                    userName = "Cindy",
                    title = "This is a diary from Cindy",
                    praiseCount = "2.8w",
                    commentCount = "9876",
                    collectCount = "1.5w",
                    mainContentBg = R.color.assist_3_dark
                )
            )
            add(
                DemoCardData(
                    avatarImg = R.drawable.avatar_img1,
                    userName = "Jack",
                    title = "这是用户Jack的一条内容",
                    praiseCount = "11.4w",
                    commentCount = "1.1w",
                    collectCount = "6.8w",
                    mainContentBg = R.color.assist_4_dark
                )
            )
            add(
                DemoCardData(
                    avatarImg = R.drawable.avatar_img1,
                    userName = "Rem",
                    title = "这是用户Rem的一条内容",
                    praiseCount = "9300",
                    commentCount = "2345",
                    collectCount = "7542",
                    mainContentBg = R.color.assist_5_dark
                )
            )
        }
    }
}