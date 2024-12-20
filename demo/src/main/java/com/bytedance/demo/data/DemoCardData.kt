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