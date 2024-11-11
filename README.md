# BlockFramework

[简体中文版说明 >>>](/README_cn.md)

[![GitHub license](https://img.shields.io/github/license/bytedance/scene)](/LICENSE)
[![API](https://img.shields.io/badge/api-14%2B-green)](https://developer.android.com/about/dashboards)

## Project Overview
In the development process of large-scale APPs, it is not uncommon for multiple business directions or teams to jointly develop the same page. Without good architectural support, the logic between different businesses is extremely likely to be coupled with each other, which in turn leads to rapid deterioration of the architecture. This will undoubtedly increase the cost of business development and maintenance. As a client-side business decoupling framework, BlockFramework has the capabilities of business layering, assembly, and collaboration. Based on this framework, business parties can easily achieve business decoupling and independently carry out logical iterations, thereby improving the stability of the architecture, reducing maintenance costs, and improving business iteration efficiency. BlockFramework mainly has four major characteristics:
- **Clear business decoupling mechanism**: Developers can easily disassemble complex business logic into multiple independent sub-"Blocks" using BlockFramework, achieving physical isolation in code. Developers of different businesses only need to focus on the development of their own business Blocks, reducing code complexity and thereby improving human efficiency. At the same time, Blocks support cross-scenario reuse. Developers can extract basic capabilities into independent Blocks and then add them to different scenarios, reducing redundant code and lowering maintenance costs.
- **High-performance UI assembly ability**: BlockFramework builds the interface UI based on a tree structure, which perfectly matches the layout tree structure of the Android system. Developers only need to create simple sub-layout Blocks one by one and then construct the parent-child relationship between Blocks according to business needs, and they can easily build a complex page. At the same time, compared with the commonly used UI assembly methods, BlockFramework integrates performance optimization methods such as asynchronous inflate and asynchronous createView in the UI assembly process to optimize page performance to the extreme.
- **Rich communication mechanism**: BlockFramework provides multiple communication mechanisms between Blocks (one-to-one, one-to-many, many-to-one), which are used to achieve the linkage and interaction ability between Blocks. The communication mechanism avoids direct interaction between Blocks through interface abstraction and event subscription/distribution, ensuring the independence and reusability of Blocks.
- **Unified development paradigm**: The access and development process of BlockFramework both provide standard specifications. Developers of different businesses can establish a unified development cognition, reduce cross-line research and development costs, and improve overall research and development efficiency.

## Applications using BlockFramework
| <img src="misc/xigua.png" alt="xigua" width="100"/> | <img src="misc/ott.png" alt="xigua" width="100"/> | <img src="misc/jingxuan.png" alt="xigua" width="100"/> |
|:---------------------------------------------------:|:-------------------------------------------------:|:------------------------------------------------------:|
|                     XiGuaVideo                      |                   XianShiGuang                    |                     DouYinJingXuan                     |

## Access Quickly
Add repository source:
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://artifact.bytedance.com/repository/Volcengine/' }
	}
}
```

Add in dependencies:
```gradle
implementation 'com.github.bytedance:block:$latest_version'

```
In a specific business scenario, accessing BlockFramework to build a page only requires a simple 4 steps:
1. In the main Activity/Fragment/Holder, implement the `IBlockJoin` interface, instantiate `IBlockContext` and `IBlockScene` fields
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
2. Create the corresponding business Block according to different logical businesses, which can inherit from `UIBlock` or `BaseBlock` respectively according to whether it is a UI scene
- 1. `UIBlock` needs to implement `layoutResource()` to return the layout XML corresponding to the Block
```kotlin
class BottomInfoBlock(blockContext: IBlockContext): UIBlock<DemoCardData, DemoModel>(blockContext) {

    override fun layoutResource(): Int {
        return R.layout.demo_bottom_info_block_layout
    }

}
```
3. Create the rootBlock, assemble each subBlock and generate the Block tree
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
4. Initialize the rootBlock to generate a complete page
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
## Lifecycle Synchronization
The default lifecycle of a Block is consistent with the Lifecycle design in Android, so the lifecycle can be directly listened to and distributed to each Block corresponding to the page.
- Lifecycle methods have been encapsulated as corresponding extension methods and can be called directly.
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

## Block Communication Mechanism
Block provides a rich communication mechanism for implementing logical communication between different Blocks and between Block and non-Block scenarios.
### Communication inside and outside the Block
When a Block needs to call external logic in a non-Block scenario, it can achieve internal and external communication by registering a blockDepend in the rootBlock.
1. Implement the external capability interface, which needs to implement the `IBlockDepend` interface.
2. Implement the depend interface in the business scenario and call `registerDepend()` in the rootBlock to register the depend.
3. Inside the Block, the depend capability can be obtained directly through `findDepend()`.
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
            registerDepend(holderDepend)
        }
        initRootBlock(itemView.context, rootBlock)
    }

}

class MainContentBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext){
    
    // Find depend
    private val holderDepend by findDepend<IHolderBlockDepend>()

    override fun layoutResource(): Int {
        return R.layout.demo_main_content_block_layout
    }

    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }
}
```

## Communication inside the Block
1. **query-service** mechanism: a one-to-one communication mechanism, each Block can provide a corresponding service interface for other Blocks to call.
```kotlin
// Define the functional interface
interface IMainContentBlockService {
   fun changeMainContent(content: String)
}

// Implement the service interface
class MainContentBlock(blockContext: IBlockContext) :
   AsyncUIBlock<DemoCardData, DemoModel>(blockContext),
   IMainContentBlockService
{
   override fun changeMainContent(content: String) {
      findViewById<TextView>(R.id.content)?.text = content
   }
   // Define the Service
   override fun defineBlockService(): Class<*>? {
      return IMainContentBlockService::class.java
   }
}

class RightInfoBlock(blockContext: IBlockContext): AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {
   // Obtain the service of the mainContentBlock
   private val mainContentBlock: IMainContentBlockService? by blockService()
   private fun initView() {
      val diggView = findViewById<View>(R.id.digg_container)
      diggView?.setOnClickListener {
         mainContentBlock?.changeMainContent("Digg Click")
      }
   }

}
```
2. **subscribe-event** mechanism: a one-to-many mechanism, a Block can send an event, and other Blocks listening to the event can receive and process the event.
```kotlin
// Define Event
class AvatarClickEvent() : Event()

class BottomInfoBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {

    private fun initView() {
        val avatar = findViewById<View>(R.id.avatar)
        avatar?.setOnClickListener {
            // Send Event
            notifyEvent(AvatarClickEvent())
        }
    }

}

class RightInteractBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext) {

    override fun onRegister() {
        // Subscribe Event
        subscribe(this, AvatarClickEvent::class.java)
    }

    // Observe Event
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

## Block high performance features
BlockFramework has built-in many high performance optimization capabilities, including asynchronous assembly View, asynchronous data binding and other capabilities.
1. Build layout asynchronously
   When adding Block, setting createUIOnMainThread = false means that when the Block creates the View, it will switch to the sub-thread for execution, and after the creation is completed, it will switch back to the main thread to assemble the View. Compared with the overall creation of the View in the main thread, the asynchronous assembly View can shorten the time by about 20%.

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
2. Reduce the layout hierarchy
      When adding Block, by setting the replaceParent = true attribute, when assembling the View, the View of the child Block can be directly replaced on the placeholder View in the parent Block, reducing the layout hierarchy.
```xml
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
    <!--Placeholder View for bottomInfoBlock in the parent Block-->
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
3. Asynchronous data binding
   When inheriting from AsyncBaseBlock/AsyncUIBlock, you can get the ability of asynchronous data binding, which supports developers putting time-consuming logic into the sub-thread for execution, and provides a callback to switch back to the main thread, making it more convenient for developers to execute time-consuming logic.
```kotlin
class MainContentBlock(blockContext: IBlockContext) :
    AsyncUIBlock<DemoCardData, DemoModel>(blockContext)
{
    private val holderDepend by findDepend<IHolderBlockDepend>()
    // Indicates the state of the Block supporting asynchronous Bind, which can change dynamically
    override fun enableAsyncBind(): Boolean {
        return holderDepend.enableAsyncBind()
    }
    // Synchronous Bind, executed in the main thread
    override fun syncBind(model: DemoModel?) {
        setPageState()
    }
    // Asynchronous Bind, depending on the return value of [enableAsyncBind], decides whether to execute in the sub-thread
    override fun asyncBind(model: DemoModel?, syncInvoke: SyncInvoke) {
        queryPageInfo()
        // Switch back to the main thread for executing UI logic
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