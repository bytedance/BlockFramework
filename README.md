# BlockFramework

[![GitHub license](https://img.shields.io/github/license/bytedance/scene)](/LICENSE)
[![API](https://img.shields.io/badge/api-14%2B-green)](https://developer.android.com/about/dashboards)

## 项目简介
BlockFramework是一套具备解耦、分层、组装和协同能力的客户端业务架构框架，能够在业务愈来愈庞大情况下有效降低架构复杂度，从而提升业务迭代效率，主要特性有：
- 极致的业务解耦机制：支持将冗杂的多业务逻辑拆解并实现代码上的物理隔离，同时实现开发模式的解耦，不同业务方聚焦于所属业务模块，提升人效；
- 高性能的UI组装能力：支持各种粒度的UI组装能力（Activity、Fragment、Holder、Container等），实现UI布局层面的解耦，同时内置异步Inflate等高性能手段，极致优化页面性能；
- 丰富的通信机制：支持一对一、一对多、多对一三种通信机制；
- 统一的开发范式：BlockFramework的接入和开发均提供了标准规范，帮助多开发者建立统一认知，降低维护成本。

[下载 Demo >>>]

## 正在使用 BlockFramework 的应用
| <img src="misc/xigua.png" alt="xigua" width="100"/> |
|:-----------:|
| 西瓜视频 | 

## 快速接入

```gradle
allprojects {
	repositories {
		...
		maven { url 'https://artifact.bytedance.com/repository/releases/' }
	}
}
```

在依赖中添加：

```gradle
implementation 'com.github.bytedance:block-framework:$latest_version'
```
在具体业务场景接入BlockFramework搭建一个页面，只需要简单4步：
1. 在主Activity/Fragment/Holder在实现IBlockJoin接口，实例化IBlockContext和IBlockScene
```kotlin
class DemoHolder(itemView: View) : RecyclerView.ViewHolder(itemView), IBlockJoin {
    // ...
    override val blockContext: IBlockContext by lazy {
        getBlockContext(blockScene)
    }

    override val blockScene: IBlockScene by lazy {
        BlockScene.DEMO_HOLDER_SCENE
    }
    // ...
}
```
2. 根据不同的逻辑业务创建对应的业务Block，可根据是否为UI场景分别继承自UIBlock或BaseBlock
- 1. UIBlock需实现layoutResource()返回Block对应的布局XML
```kotlin
class BottomInfoBlock(blockContext: IBlockContext): UIBlock<DemoCardData, DemoModel>(blockContext) {

    override fun layoutResource(): Int {
        return R.layout.demo_bottom_info_block_layout
    }

}
```
3. 创建rootBlock，组装各子Block并生成Block树
```kotlin
class DemoCardRootBlock(private val rootView: View, blockContext: IBlockContext) :
    UIBlock<DemoCardData, DemoModel>(blockContext) {

    override fun layoutResource(): Int {
        return IUIBlock.USE_PARENT_LAYOUT
    }

    override fun onCreateView(parent: View?): View {
        return rootView
    }

    override fun assembleSubBlocks(assembler: BlockAssembler) {
        assembler.assemble {
            addBlock {
                instance = {
                    MainContentBlock(blockContext)
                }
                parentId = R.id.main_content_block_container
            }
            addBlock {
                instance = {
                    BottomInfoBlock(blockContext)
                }
                parentId = R.id.bottom_info_block_container
            }
            addBlock {
                instance = {
                    RightInteractBlock(blockContext)
                }
                parentId = R.id.right_interact_block_container
            }
        }
    }
}
```
4. 初始化rootBlock，即可生成一个完整页面
```kotlin
class DemoHolder(itemView: View) : RecyclerView.ViewHolder(itemView), IBlockJoin {

    private lateinit var rootBlock: DemoCardRootBlock

    override val blockContext: IBlockContext by lazy {
        getBlockContext(blockScene)
    }

    override val blockScene: IBlockScene by lazy {
        BlockScene.DEMO_HOLDER_SCENE
    }

    fun onCreateViewHolder() {
        rootBlock = DemoCardRootBlock(itemView, blockContext)
        initRootBlock(itemView.context, rootBlock)
    }
}
```
## 生命周期同步
Block的默认生命周期与Android的Lifecycle设计一致，因此可以直接监听对应页面的生命周期分发到各Block
- 生命周期方法已封装为相应的拓展方法，可直接调用
```kotlin
private fun observeLifeCycle() {
    lifecycleOwner?.lifecycle?.addObserver(
        object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> dispatchOnCreate()
                    Lifecycle.Event.ON_START -> dispatchOnStart()
                    Lifecycle.Event.ON_RESUME -> dispatchOnResume()
                    Lifecycle.Event.ON_PAUSE -> dispatchOnPause()
                    Lifecycle.Event.ON_STOP -> dispatchOnStop()
                    Lifecycle.Event.ON_DESTROY -> dispatchOnDestroy()
                    else -> {}
                }
            }
        }
    )
}
```

## Block通信机制
Block提供了丰富的通信机制，用于实现不同Block之间、Block与非Block场景之间的逻辑通信
### Block内外通信
Block内部需要调用非Block场景的外部逻辑时，可以通过在rootBlock中注册blockDepend的方式实现内外通信
1. 实现外部能力接口，需实现自IBlockDepend
2. 在业务场景实现depend接口，并在rootBlock中调用registerDepend，注册depend
3. Block内部可直接通过findDepend()获得到depend能力
```kotlin
interface IHolderBlockDepend : IBlockDepend {
    fun enableAsyncBind(): Boolean
}

class DemoHolder(itemView: View) : RecyclerView.ViewHolder(itemView), IBlockJoin {

    private lateinit var rootBlock: DemoCardRootBlock

    private val holderDepend: IBlockDepend = object : IHolderBlockDepend {
        override fun enableAsyncBind(): Boolean {
            return true
        }
    }

    fun initCardBlock() {
        rootBlock = DemoCardRootBlock(itemView, blockContext).apply {
            // 注册depend
            registerDepend(holderDepend)
        }
        initRootBlock(itemView.context, rootBlock)
    }

}

class MainContentBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext){
    
    // 获取外部depend
    private val holderDepend by findDepend<IHolderBlockDepend>()

    override fun layoutResource(): Int {
        return R.layout.demo_main_content_block_layout
    }

    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }
}
```

## Block内部通信
1. query-serviece机制：一对一通信机制，各Block可以提供对应的service接口，供其他Block调用
```kotlin
// 定义功能接口
interface IMainContentBlockService {
    fun changeMainContent(content: String)
}

// 实现service接口
class MainContentBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext),
    IMainContentBlockService
{

    override fun changeMainContent(content: String) {
        findViewById<TextView>(R.id.content)?.text = content
    }

    // 定义Service
    override fun defineBlockService(): Class<*>? {
        return IMainContentBlockService::class.java
    }
}

class RightInfoBlock(blockContext: IBlockContext): AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {
    // 获取mainContentBlock的service
    private val mainContentBlock: IMainContentBlockService? by blockService()

    private fun initView() {
        val diggView = findViewById<View>(R.id.digg_container)
        diggView?.setOnClickListener { 
            mainContentBlock?.changeMainContent("Digg Click")
        }
    }
    
}
```
2. subscribe-event机制：一对多机制，Block可以发送event，监听该event的其他Block均能接收到event并处理
```kotlin
// 定义Event
class AvatarClickEvent(): Event()

class BottomInfoBlock(blockContext: IBlockContext): AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {

    private fun initView() {
        val avatar = findViewById<View>(R.id.avatar)
        avatar?.setOnClickListener {
            // 发送Event
            notifyEvent(AvatarClickEvent())
        }
    }

}

class RightInteractBlock(blockContext: IBlockContext): AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {
    
    override fun onRegister() {
        // 订阅Event
        subscribe(this, AvatarClickEvent::class.java)
    }

    // 监听Event
    override fun onEvent(event: Event): Boolean {
        when (event) {
            is AvatarClickEvent -> {
                onAvatarClick()
            }
        }
        return super.onEvent(event)
    }
}
```

## Block高性能特性
BlockFramework内置了不少高性能优化能力，包括异步组装View，异步绑定数据等能力
1. 异步组装View
   在addBlock时，设置`createUIOnMainThread = false`表示该Block创建View的时会切换到子线程执行，创建完成后切换回到主线程组装View，相较于整体在主线程创建View耗时更短，经过对比分析，异步组装View能缩短约20%的耗时。
```kotlin
override fun assembleSubBlocks(assembler: BlockAssembler) {
    assembler.assemble {
        addBlock {
            instance = {
                BottomInfoBlock(blockContext)
            }
            parentId = R.id.bottom_info_block_container
            createUIOnMainThread = false
        }
        addBlock {
            instance = {
                RightInteractBlock(blockContext)
            }
            parentId = R.id.right_interact_block_container
            createUIOnMainThread = false
        }
    }
}
```
2. 减少布局层级
   在addBlock时，通过设置`replaceParent = true`属性，能够在组装View时，将子Block的View直接替换到父Block中的占位View上，减少布局层级。\
```xml
<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/root"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:splitMotionEvents="false"
    android:background="@color/background_card_2_dark"
    tools:ignore="ResourceName">
    
    ...

    <!--父Block中bottomInfoBlock的占位View-->
    <LinearLayout
        android:id="@+id/bottom_info_block_container"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="left|bottom"
        android:layout_marginBottom="36dp"
        android:gravity="left"
        android:orientation="vertical"
        tools:background="@color/assist_1"
        tools:layout_height="100dp"
        tools:layout_width="200dp" />

    ...

</FrameLayout>
```
```kotlin
override fun assembleSubBlocks(assembler: BlockAssembler) {
    assembler.assemble {
        addBlock {
            instance = {
                BottomInfoBlock(blockContext)
            }
            parentId = R.id.bottom_info_block_container
            replaceParent = true
        }
    }
}
```
3. 异步绑定数据
   当继承自AsyncBaseBlock/AsyncUIBlock时，可以获得异步绑定数据能力，支持开发者将耗时逻辑放到子线程执行，并提供了切换回主线程的回调，让开发者更方便的执行耗时逻辑。
```kotlin
class MainContentBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext)
{

    private val holderDepend by findDepend<IHolderBlockDepend>()

    // 表示Block支持异步Bind的状态，支持动态变化
    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }

    // 同步Bind，执行在主线程
    override fun syncBind(model: DemoModel?) {
        setPageState()
    }

    // 异步Bind，根据[enableAsyncBind]的返回值决定是否执行在子线程
    override fun asyncBind(model: DemoModel?, syncInvoke: SyncInvoke) {
        queryPageInfo()
        // 切换会主线程，用于执行UI逻辑
        syncInvoke {
            showPageLoading()
        }
    }
}
```

## License
```text
    Copyright (C) 2024 Bytedance Ltd. and/or its affiliates
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
        http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
```