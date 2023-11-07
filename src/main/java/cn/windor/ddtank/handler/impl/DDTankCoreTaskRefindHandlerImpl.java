package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.handler.DDTankCoreTaskRefindHandler;
import cn.windor.ddtank.handler.HwndMarkHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.parameters.P;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankCoreTaskRefindHandlerImpl implements DDTankCoreTaskRefindHandler {
    private HwndMarkHandler hwndMarkHandler;
    private long hwnd;

    private Library dm;

    private Mouse mouse;

    private final String path = DDTankFileConfigProperties.getBaseDir();

    public DDTankCoreTaskRefindHandlerImpl(long hwnd, Library dm) {
        this.dm = dm;
        this.hwndMarkHandler = new HwndMarkHandlerImpl(dm);
        this.mouse = new DMMouse(dm.getSource());
        // 从顶级窗口进行查找
        this.hwnd = dm.getWindow(hwnd, 7);
    }

    /**
     * 关闭弹窗
     *
     * @return
     */
    private boolean shutdownAlertMessageBox(long topHwnd) {
        // 当前顶级窗口下客户端大小<400, 200的认为是弹窗
        long hwnd = dm.findWindow("#32770", "");
        if(hwnd == 0) {
            return false;
        }
        String title = dm.getWindowTitle(hwnd);
        if(title.startsWith("JavaScript")) {
            dm.setWindowState(hwnd, 0);
            return true;
        }
        return false;
    }

    @Override
    public long refindHwnd(long gameHwnd) {
        // 1. 获取gameHwnd的顶级窗口，查看和记录的顶级窗口是否一致，若不一致使用gameHwnd的顶级窗口（说明有过窗口分离操作）
        long topHwnd = dm.getWindow(hwnd, 7);
        if (topHwnd != 0 && topHwnd != this.hwnd) {
            this.hwnd = topHwnd;
        }
        // 在所有窗口中，0表示窗口无效
        if (hwnd == 0) {
            return 0;
        }
        // 进入循环
        long startTime = System.currentTimeMillis();
        for (int i = 0; ; ) {
            if (dm.bindWindowEx(hwnd, "dx2", "dx2", "dx", "dx.public.active.message", 0)) {
                break;
            }
            if (++i > 3) {
                // 失败
                log.error("在自动重启时绑定失败！");
                return 0;
            }
        }
        if(!"DUIWindow".equals(dm.getWindowClass(hwnd))) {
            // 目前仅支持360游戏大厅
            return 0;
        }
        Point prev = new Point(143, 54);
        if (dm.findPic(0, 0, 210, 80, path + "360-上一步.bmp", "505050", 1, 0, prev)) {
            prev.setOffset(6, 6);
        }
        // 进入循环前先点击两下后退
        for (int i = 0; i < 2; i++) {
            mouse.moveAndClick(prev);
            delay(1000, true);
        }
        delay(10000, true);
        int[] size = dm.getClientSize(hwnd);
        int width = size[0];
        int height = size[1];
        // 2分钟内自动登录失败
        while (System.currentTimeMillis() - startTime <= 120000) {
            // 1. 检测弹窗，如果关闭了弹窗（返回false），那么就需要将窗口退回到第一步
            if (shutdownAlertMessageBox(hwnd)) {
                for (int i = 0; i < 2; i++) {
                    mouse.moveAndClick(prev);
                    delay(1000, true);
                }
                delay(10000, true);
            }
            Point loginBtn = new Point();
            // 2. 检测登录按钮
            if(dm.findPic(0, 0, width, height, path+"html-登录.bmp", "101010", 0.8, 0, loginBtn)) {
                log.info("自动重连-点击登录按钮");
                mouse.moveAndClick(loginBtn);
                delay(1000, true);
            }

            // 4. 尝试获取有效的游戏窗口
            long window = this.hwnd;
            do {
                if(hwndMarkHandler.isLegalHwnd(window)) {
                    // 找到了合适的句柄
                    dm.unbindWindow();
                    return window;
                }
                window = dm.getWindow(window, 1);
            } while (window != 0);
            delay(1000, true);
        }
        dm.unbindWindow();
        return 0;
    }
}
