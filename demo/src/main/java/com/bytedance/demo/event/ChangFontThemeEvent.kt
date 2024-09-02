package com.bytedance.demo.event

import com.bytedance.blockframework.interaction.Event
import com.bytedance.demo.util.FontType

/**
 * description:
 *
 * @author Created by zhoujunjie on 2024/9/2
 * @mail zhoujunjie.9743@bytedance.com
 **/

class ChangFontThemeEvent(val type: FontType): Event()