package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.account.DDTankAccountSignHandler;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.base.impl.LibraryFactory;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.handler.DDTankCoreRefindHandler;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static cn.windor.ddtank.util.ThreadUtils.delay;
import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

@Slf4j
public class DDTankRefindByNewWindow implements DDTankCoreRefindHandler, Serializable {
    private static final long serialVersionUID = 1L;

    // 解决多线程下创建新窗口时的归属问题
    private static final List<Long> usedHwnds = Collections.synchronizedList(new ArrayList<>());

    private DDTankLog ddTankLog;

    private DDTankConfigProperties properties;

    private DDTankAccountSignHandler accountSignHandler;

    private Library dm;

    public DDTankRefindByNewWindow(Library dm, DDTankLog ddTankLog, DDTankAccountSignHandler accountSignHandler, DDTankConfigProperties properties) {
        this.dm = dm;
        this.ddTankLog = ddTankLog;
        this.properties = properties;
        this.accountSignHandler = accountSignHandler;
    }

    @Override
    public long refindHwnd(long gameHwnd) {
        if(StringUtils.isEmpty(properties.getWebsite())) {
            log.error("未设置该端的网址，自动重连失败，请将网址设置到配置中。");
            delay(100, true);
            // 停止当前线程
            Thread.currentThread().interrupt();
            return 0;
        }
        // 糖果浏览器默认类名
        String hwndClassName = "Afx:00400000:8:00010003:00000006:00000000";
        if (dm.getWindowState(gameHwnd, 0)) {
            // 如果窗口存在则获取gameHwnd的顶层窗口
            long deskTopHwnd = dm.getWindow(gameHwnd, 7);
            hwndClassName = dm.getWindowClass(deskTopHwnd);
            shutdown(deskTopHwnd);
        }

        // 创建一个新窗口并找出多出的那个新的窗口，作为当前游戏窗口的顶层父窗口
        long hwnd = findNewHwnd(hwndClassName);
        // 处理糖果强制关闭窗口后的小尾巴、进行加速等操作
        TangoGcHandler.gc();

        if (hwnd == 0) {
            log.error("[自动重连]：未找到新打开的窗口");
            ddTankLog.error("[自动重连]：未找到新打开的窗口");
            return 0;
        }else {
            ddTankLog.success("[自动重连]：已打开新的浏览器窗口");
        }

        // 找到可以操作网页的窗口
        long htmlHwnd = findHwnd(hwnd, "Internet Explorer_Server");
        if (htmlHwnd == 0) {
            // 未在糖果浏览器下找到网页窗口，理论来说是不可能的。
            shutdown(hwnd);
            return 0;
        }

        if (!bindWindow(htmlHwnd)) {
            log.error("[自动重连]：重绑定失败，尝试绑定窗口失败！错误代码：{}", dm.getLastError());
            ddTankLog.error("[自动重连]：重绑定失败，尝试绑定窗口失败！");
            shutdown(hwnd);
            return 0;
        }
        // 已绑定网页窗口，调用各种自定义登录接口
        accountSignHandler.login();

        // 搜索游戏窗口
        gameHwnd = findGameHwnd(htmlHwnd);
        if (gameHwnd == 0) {
            shutdown(hwnd);
            log.error("[自动重连]：调用登录接口后未找到游戏窗口，请考虑登录接口是否和当前游戏端相匹配。");
            ddTankLog.error("[自动重连]：调用登录接口后未找到游戏窗口");
        }
        dm.unbindWindow();
        return gameHwnd;
    }

    private void shutdown(long hwnd) {
        // 关闭指定窗口
        long pid = dm.getWindowProcessId(hwnd);
        try {
            Runtime.getRuntime().exec("taskkill /F /PID " + pid);
        } catch (IOException e) {
            log.error("[自动重连]：尝试关闭旧窗口失败!");
            ddTankLog.error("[自动重连]：尝试关闭旧窗口失败!");
        }
    }

    /**
     * 绑定窗口
     *
     * @param hwnd
     */
    private boolean bindWindow(long hwnd) {
        int failTimes = 0;
        while (!dm.bindWindowEx(hwnd, "dx2", "windows3", "dx", "dx.public.active.message|dx.public.input.ime", 0)) {
            delay(1000, true);
            failTimes++;
            if (failTimes % 5 == 0) {
                // 5次绑定失败，返回0，一般不会在该步骤出错
                return false;
            }
        }
        return true;
    }

    /**
     * 打开新窗口
     */
    private void openNewWindow() {
        try {
            // 在所有顶层窗口中查找已经存在的，和游戏顶层窗口一样的窗口
            Runtime.getRuntime().exec("\"C:\\Program Files (x86)\\TGGame\\Tango3.exe\" " + properties.getWebsite());
        } catch (IOException e) {
            log.error("尝试创建窗口失败");
            ddTankLog.error("[自动重连]：尝试创建窗口失败!");
            throw new RuntimeException(e);
        }
    }

    /**
     * 打开新窗口并返回该窗口的句柄
     *
     * @param hwndClassName 想打开窗口的类名
     * @return 新打开的窗口，未找到则返回0
     */
    private long findNewHwnd(String hwndClassName) {
        // 移除掉不存在的句柄
        usedHwnds.removeIf(usedHwnd -> !dm.getWindowState(usedHwnd, 0));
        List<Long> oldHwnds = dm.enumWindow(0, "", "Afx:00400000:8:00010003:00000006:00000000", 2 | 8 | 16);
        openNewWindow();
        long startTime = System.currentTimeMillis();
        long hwnd = 0;
        do {
            List<Long> newHwnds = dm.enumWindow(0, "", "Afx:00400000:8:00010003:00000006:00000000", 2 | 8 | 16);
            for (Long oldHwnd : oldHwnds) {
                newHwnds.remove(oldHwnd);
            }
            if (newHwnds.size() > 0) {
                hwnd = newHwnds.remove(0);
                synchronized (DDTankRefindByNewWindow.class) {
                    while (usedHwnds.contains(hwnd)) {
                        if (newHwnds.size() == 0) {
                            // 理论上来说不会出现这个错误日志，除非能够在很短的时间内
                            log.error("[自动重连]：已打开的窗口不足以分配给现有的游戏窗口！");
                            ddTankLog.error("[自动重连]：已打开的窗口不足以分配给现有的游戏窗口！");
                            hwnd = 0;
                            // 继续去外部找新打开的窗口
                            break;
                        }
                        hwnd = newHwnds.remove(0);
                    }
                    usedHwnds.add(hwnd);
                }
            }
            // 未找到窗口，可能调用完cmd还未弹出糖果浏览器窗口
            delay(1000, true);
        } while (hwnd == 0 && System.currentTimeMillis() - startTime < 10000);
        return hwnd;
    }

    /**
     * 查找游戏窗口
     *
     * @param htmlHwnd 指定的上层窗口我
     * @return 如果找到则返回游戏窗口，否则返回0
     */
    private long findGameHwnd(long htmlHwnd) {
        long start = System.currentTimeMillis();
        long hwnd = 0;
        // 寻找一分钟后若还未找到，则认为当前并没有登录成功
        do {
            hwnd = findHwnd(htmlHwnd, "MacromediaFlashPlayerActiveX");
            delay(1000, true);
        } while (hwnd == 0 && System.currentTimeMillis() - start < 10000);

        if(hwnd != 0) {
            // 找到游戏窗口后不着急返回，先进行一段时间的延迟，好让第七大道的过场动画跳过，然后在脚本启动后点击屏幕就可以直接跳过过场动画了。
            ddTankLog.success("[自动重连]：已成功找到新的游戏窗口!");
            delay(10000, true);
        }
        return hwnd;
    }


    /**
     * 查找hwnd窗口下的指定子窗口
     *
     * @param hwnd      父窗口
     * @param className 窗口类名
     * @return 如果找到了指定类名的窗口则返回该窗口，否则返回0
     */
    private long findHwnd(long hwnd, String className) {
        while (hwnd != 0) {
            hwnd = dm.getWindow(hwnd, 1);
            if (className.equals(dm.getWindowClass(hwnd))) {
                return hwnd;
            }
        }
        return 0;
    }

    /**
     * 处理在关闭了糖果窗口进程后，会留有的一个窗口小尾巴
     * 该类会绑定所有顶层的糖果窗口，去查找崩溃图标，如果找到则在图标上右键
     *
     * <b>注意：糖果浏览器必须设置右键关闭标签栏！</b>
     */
    static class TangoGcHandler {
        /**
         * 多线程环境下，串行执行任务，当队列满了的时候，放弃队列中的任务
         */
        private static final ExecutorService executorService = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.DiscardOldestPolicy());


        public static void gc() {
            // 提交通知，让线程去执行关闭窗口任务
            executorService.submit(() -> {
                ActiveXComponent compnent = LibraryFactory.getActiveXCompnent();
                Library dm = new DMLibrary(compnent);
                List<Long> hwnds = dm.enumWindow(0, "", "Tango3", 2);
                Mouse mouse = new DMMouse(compnent);
                for (Long hwnd : hwnds) {

                    // 尝试变更速度
                    long toolHwnd = dm.findWindowEx(hwnd, "AfxWnd80su", "BrowserBar");
                    if(dm.bindWindowEx(toolHwnd, "dx2", "dx2", "dx", "dx.public.active.message", 0)) {
                        delayPersisted(500, false);
                        for (int i = 0; i < 10; i++) {
                            mouse.moveAndClick(dm.getClientSize(toolHwnd)[0] -16, 16);
                        }
                        dm.unbindWindow();
                        delayPersisted(500, false);
                    }

                    // 将没用的图标删除
                    hwnd = dm.findWindowEx(hwnd, "AfxWnd80su", "PageLabelBar");
                    if (hwnd == 0) {
                        continue;
                    }
                    dm.bindWindowEx(hwnd, "dx2", "dx2", "dx", "dx.public.active.message", 0);
                    delayPersisted(1000, false);

                    // 在窗口可见范围内去找图
                    int[] size = dm.getClientSize(hwnd);
                    int width = size[0];
                    int height = size[1];
                    Point result = new Point();
                    // TODO 更新
                    while (dm.findPic(0, 0, width, height, DDTankFileConfigProperties.getBaseDir() + "糖果崩溃.bmp", "101010", 0.8, 0, result)) {
                        mouse.moveTo(result.setOffset(10, 0));
                        mouse.rightClick();
                        delay(1000, true);
                    }

                    dm.unbindWindow();
                    delayPersisted(1000, false);
                }
                // 释放资源
                ComThread.Release();
            });
        }
    }
}
