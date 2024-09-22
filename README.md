# BlockFramework

[简体中文版说明 >>>](/README_cn.md)

[![GitHub license](https://img.shields.io/github/license/bytedance/scene)](/LICENSE)
[![API](https://img.shields.io/badge/api-14%2B-green)](https://developer.android.com/about/dashboards)

## Project Overview
BlockFramework is a set of client business architecture frameworks with decoupling, layering, assembly, and collaboration capabilities. It can effectively reduce the complexity of architecture in the case of increasingly large businesses, thereby improving the efficiency of business iterations. Its main features include:
- Ultimate business decoupling mechanism: Support the disassembly of complex multi-business logic and the physical isolation of code, while achieving the decoupling of the development model. Different business parties focus on their respective business modules to improve human efficiency.
- High-performance UI assembly capability: Support various granularity UI assembly capabilities (Activity, Fragment, Holder, Container, etc.), realize the decoupling of the UI layout level, and at the same time built-in asynchronous Inflate and other high-performance methods to optimize page performance to the extreme.
- Rich communication mechanisms: Support one-to-one, one-to-many, and many-to-one three communication mechanisms.
- Unified development paradigm: The access and development of BlockFramework provide standard specifications to help multiple developers establish a unified understanding and reduce maintenance costs.


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
		maven { url 'https://artifact.bytedance.com/repository/releases/' }
	}
}
```

Add in dependencies:
```gradle
implementation 'com.github.bytedance:block-framework:$latest_version'

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