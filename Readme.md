# 关于脚本项目近期情况说明

近期较忙，本仓库近期不会更新（但我本地会有更新，之后等我近期稳定下来之后再一次性上传）。

本版本之前的代码bug较多，之后会对整体进行重构，项目预计在24年下半年才开始正式的持续更新。

<hr/>

由于大漠插件是32位，故需要使用32位的JDK。

### 脚本导入

1. 将 `jacob-1.20-x86.dll` 放置JRE的bin目录下
2. 使用 `RegDll.dll` 将大漠插件注册


### 脚本使用了下列开源库

[快捷键监听库](https://github.com/melloware/jintellitype)

[Java与COM组件通信库](https://github.com/freemansoft/jacob-project)

### 脚本说明

后端Spring Boot，前端thymeleaf模板技术（前后端不分离）。

项目启动后每成功标记一个合格的游戏句柄则都会创建守护线程 `DDTankCoreThread coreThread`，该线程内部管理了一个运行的脚本 `DDTankCoreTask task`，`task` 会执行自动选图、自动攻击等操作。

#### 核心类

`cn.windor.ddtank.base.Library`：第三方接口，本项目使用大漠作为实现。

`cn.windor.ddtank.core.DDTankCoreTaskProperties`：基本所有主要的配置内容都在这个类中。

`cn.windor.ddtank.core.DDTankPic`：定义了弹弹堂自动挂机需要用到的所有图像识别方法，计划**之后的自定义功能围绕该接口实现全端通用**。

`cn.windor.ddtank.core.DDTankOperate`：定义了弹弹堂自动挂机需要用的所有操作的方法，例如不同版本下如何选地图、如何调整角度、如何攻击、如何获取角度力度。

`cn.windor.ddtank.core.DDTankCoreTask implements Runnable `：启动线程后执行的任务，负责调用图像识别、操作游戏窗口内一切内容。（Runnable接口作为线程对象的参数，在线程启动后会执行Runnable接口的run方法）

`cn.windor.ddtank.core.DDTankCoreScript extends Thread`：负责守护管理已启动的 `DDTankCoreTask`，启动或停止脚本、实时截图、自动重连、定点重启等功能在此实现。

`cn.windor.ddtank.service.DDTankThreadService`：负责包装已启动的`DDTankCoreThread`，对外提供线程服务。

#### 包

`cn.windor.ddtank.controller`：提供http接口，和前端对接

`cn.windor.ddtank.service`：为controller提供服务

`cn.windor.ddtank.handler`：脚本中使用到的一些具体功能的处理接口，例如如何攻击、找不到敌人时如何走位、调整不到角度时如何走位、如何找到游戏窗口句柄等等。
