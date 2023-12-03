package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.account.impl.SimpleDDTankAccountSignHandlerImpl;
import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.base.impl.DMKeyboard;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.account.DDTankAccountSignHandler;
import cn.windor.ddtank.handler.DDTankHwndMarkHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.util.StringUtils;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankCoreTaskRefindByOldWindow {
    private DDTankHwndMarkHandler DDTankHwndMarkHandler;
    private long hwnd;

    private Library dm;

    private Mouse mouse;

    private Keyboard keyboard;

    private DDTankLog ddtLog;

    private final String path = DDTankFileConfigProperties.getBaseDir();

    private final DDTankAccountSignHandler accountSignHandler;

    @Setter
    @Getter
    private String username;

    @Setter
    @Getter
    private String password;

    public DDTankCoreTaskRefindByOldWindow(long hwnd, Library dm, DDTankLog ddtLog) {
        this.dm = dm;
        this.ddtLog = ddtLog;
        this.DDTankHwndMarkHandler = new DDTankHwndMarkHandlerImpl(dm);
        this.mouse = new DMMouse(dm.getSource());
        this.keyboard = new DMKeyboard(dm.getSource());
        // 从顶级窗口进行查找
        this.hwnd = dm.getWindow(hwnd, 7);
        this.accountSignHandler = new SimpleDDTankAccountSignHandlerImpl(dm, mouse, keyboard);
    }

    // TODO 更新复杂对象

    public boolean update(Object... complexObject) {
        return false;
    }

    /**
     * 关闭弹窗
     *
     * @return
     */
    private boolean shutdownAlertMessageBox(long topHwnd) {
        // 当前顶级窗口下客户端大小<400, 200的认为是弹窗
        long hwnd = dm.findWindow("#32770", "");
        if (hwnd == 0) {
            return false;
        }
        String title = dm.getWindowTitle(hwnd);
        if (title.startsWith("JavaScript")) {
            dm.setWindowState(hwnd, 0);
            return true;
        }
        return false;
    }

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
        String className = dm.getWindowClass(hwnd);
        if ("Afx:00400000:8:00010003:00000006:00000000".equals(className)) {
            // 糖果浏览器
            return refindHwndTg();
        } else if ("DUIWindow".equals(className)) {
            // 360游戏大厅
            return refindHwnd360();
        } else {
            return 0;
        }
    }

    private long findHwnd(String className) {
        return findHwnd(hwnd, className);
    }

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
     * 先从顶级窗口下找到类名为Internet Explorer_Server的句柄，然后后退，前进用来刷新游戏窗口，糖果的刷新规则是Internet Explorer_Server窗口不会变
     * 正常情况 |cookie存在| cookie消失
     * 2559914 | 2559914 | 2559914
     * 3804868 | 3804868 | 3804868
     * 4458804 | 4458804 | 4458804
     * 987572  | 987572  | 987572 (Internet Explorer_Server)
     * 1051362 | 2363092 | 0      (MacromediaFlashPlayerActiveX)
     **/
    private long refindHwndTg() {
        long hwnd = findHwnd("Internet Explorer_Server");
        long gameHwnd = 0;
        if (hwnd == 0) {
            logInfo("未找到Internet Explorer_Server窗口！");
            return 0;
        }
        int failTimes = 0;
        while (!bindWrapper(hwnd)) {
            delay(1000, true);
            failTimes++;
            if (failTimes % 5 == 0) {
                // 5次绑定失败，返回0，一般不会在该步骤出错
                logError("重绑定失败，尝试绑定窗口失败！");
                return 0;
            }
        }

        failTimes = 0;
        // 尝试寻找该窗口下的 MacromediaFlashPlayerActiveX 句柄
        while ((gameHwnd = findHwnd(hwnd, "MacromediaFlashPlayerActiveX")) == 0) {
            delay(100, true);
            failTimes++;

            if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
                // 每失败后等待100ms后尝试一次登录操作
                accountSignHandler.login();
                delay(1000, true);
            }
            if(failTimes % 6 == 3) {
                // 尝试后退操作
                mouse.moveAndClick(1, 1);
                keyboard.keyPressChar("back");
                // 回车防止弹出确定退出弹窗
                delay(300, true);
                keyboard.keyPressChar("enter");
                delay(1000, true);
            }
            if (failTimes % 6 == 0) {
                mouse.moveAndClick(1, 1);
                // 尝试前进操作
                keyboard.keyDownChar("shift");
                delay(300, true);
                keyboard.keyPressChar("back");
                delay(300, true);
                keyboard.keyUpChar("shift");
                delay(1000, true);

            }
            if (failTimes > 18) {
                logError("未能找到有效的游戏窗口");
                break;
            }
        }

        // 最终解除外部绑定
        dm.unbindWindow();
        return gameHwnd;
    }

    private boolean bindWrapper(long hwnd) {
        return dm.bindWindowEx(hwnd, "dx2", "dx2", "dx", "dx.public.active.message|dx.public.input.ime", 0);
    }

    private long refindHwnd360() {
        // 进入循环
        long startTime = System.currentTimeMillis();
        for (int i = 0; ; ) {
            if (dm.bindWindowEx(hwnd, "dx2", "dx2", "dx", "dx.public.active.message|dx.public.input.ime", 0)) {
                break;
            }
            if (++i > 3) {
                // 失败
                log.error("在自动重启时绑定失败！");
                return 0;
            }
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
            if (dm.findPic(0, 0, width, height, path + "html-登录.bmp", "101010", 0.8, 0, loginBtn)) {
                log.info("自动重连-点击登录按钮");
                mouse.moveAndClick(loginBtn);
                delay(1000, true);
            }

            // 4. 尝试获取有效的游戏窗口
            long window = this.hwnd;
            do {
                if (DDTankHwndMarkHandler.isLegalHwnd(window)) {
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

    public void log(String msg) {
        ddtLog.info(msg);
        log.debug(msg);
    }

    public void logInfo(String msg) {
        ddtLog.info(msg);
        log.info(msg);
    }

    public void logWarn(String msg) {
        ddtLog.info(msg);
        log.warn(msg);
    }

    public void logError(String msg) {
        ddtLog.info(msg);
        log.error(msg);
    }
}
