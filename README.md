### 目录

src		 ——	源码（方便看的）

import  ——	导入使用（包含源码、附件、界面）

build      ——	按键小精灵（.exe可执行文件）

### 关于DDtank项目

每个项目可以分为打手号、挂机号两部分，若您是土豪体验生活直接购买了全翻，那么只需要关心打手号即可，不然就多建几个小号，使用挂机号一键翻牌挂机即可。

由于在你玩游戏时程序并不知道你想让哪个号去当大号（打手）打副本，所以在脚本启动时鼠标需要放在打手的窗口内。其余的号直接使用小号一键挂机即可。虽然脚本可以直接设计为全打手号，只不过出手时按p，但在小号很多的情况下需要每个窗口去设置、绑定。

如果不小心在小号窗口内启动了打手的脚本，那么需要刷新一下小号窗口，因为此时程序已经认定了这个窗口是打手窗口。

### 脚本使用入门

1. 在 [按键精灵 (myanjian.com)](http://download.myanjian.com/) 下载PC端按键精灵，并安装。
2. 打开按键精灵并在脚本列表空白处右键-导入项目 **`/import`** 下后缀为 **`.Q`** 的脚本。

### 待改进的地方

- 研究角度力度公式
- 研究获取小地图屏障位置
- 自动获取屏距（我认为不亲自搞搞图像识别这个是搞不出来的）

### 脚本使用说明

除去基本的按键精灵代码外，脚本内使用的最多的就是大漠的插件了，大漠插件可以用来后台挂机、识图、找色等。大漠的API说明文档可以在[[旧帖\]插件分享----PC按键----常用插件整理[第五章][2020.12.17更新] _ 【集结令●英雄归来】 - 按键精灵论坛 (anjian.com)](http://bbs.anjian.com/showtopic-686179-1.aspx#11474138)中找到（dm31233.rar）

### 各端测试介绍&GM联系方式

- 随时要跑路的3.6
  Q群：861019748

  > 看着挺好玩的，就是蚂蚁打的有点费劲，后面未测试，之所以叫随时要跑路的3.6是因为该端没有名字。
  >
  > 有迷宫寻宝和大厅挂机机器人
  >
  > 微变，不好玩

- 星落
  Q群：418106411

  > 带有魂环的端，两三套装备栏，忘了叫啥名字了，该端是从那个端照搬来的。
  >
  > 多了两套装备栏的特性还是看起来不错的，但是看样子也是微变。

- 君临（20230822前已关服）
  Q群：418106411

  > 这是我见过最多副本的端，40个本里34个本是除了掉落物外完全一样的，后面的6个本是10年前的本了（GM自制图，需要飞好几回合的那种），最坑爹的是打完后面的本会崩溃！装备方面的话从1级到15级，宝珠也是。
  >
  > 该本的攻略是攒够勋章购买穿甲宝珠，4开直接打后面的本。
  >
  > 算是中变，GM坑爹的把道具栏体力提高了，敏捷最高到10w。

- 冷梦（20230820前已关服）
  Q群：418106411

  > 大残端，GM坑爹的把副本里原先的掉落物删了一半，而且还没有删干净！泡点给的东西还是原先的！
  >
  > 为什么叫大残端呢？因为原先的端就是残端（按正常流程打个几百把强化石本将三件套强化到50后叠怒打后面的本了，但是！任务有bug，任务栏显示+66的武器（可强化到75）领取后变成了+60，这导致后面的本打不过！）
  
- 屌丝（20230906因Bug太多而关服）

  Q群：232956131

  > 副本很多，很多图有bug，由于尝试在完全自动化小地图识别1屏的距离时发现每个地图小地图的检测区域不同，故自动识别无法进行。

- 云海（旧版本，20230907再开服）

  Q群：232956131

  该端由于旧版本写过，不再更新，详细bug见该文件夹import下的readme。

  > 材料非常多的端，本人在完全通关时已建立4个小号的仓库，其中分别存放了1-7费卡牌，森林、刺客卡牌，职业联赛掉落物，新手全套（炼狱、海洋、山脉、云霄）装备等其他道具。
  >
  > 主线合成部分就是基本每个本刷出武器，然后合成最终神器。在GM不干预的情况下大部分玩家会被4精粹（1-1~1-4掉落）劝退，再有就是刺客密集（5-1~5-3）。
  
- 
