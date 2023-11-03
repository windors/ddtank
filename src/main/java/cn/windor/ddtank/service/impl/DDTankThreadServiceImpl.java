package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.base.impl.DMKeyboard;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.base.impl.LibraryFactory;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.*;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class DDTankThreadServiceImpl implements DDTankThreadService {

    @Autowired
    private Library dm;

    @Autowired
    private DDTankConfigProperties properties;

    @Autowired
    private DDTankConfigService configService;

    public static final int SHORTCUT_START = 1; // 模拟任务开始快捷键
    public static final int SHORTCUT_STOP = 2; // 模拟手动结束任务快捷键

    private final Map<Long, DDTankCoreThread> threadMap = new ConcurrentHashMap<>();

    private final Map<Long, DDTankStartParam> waitStartMap = new ConcurrentHashMap<>();

    public DDTankThreadServiceImpl(Library dm) {
        this.dm = dm;
        JIntellitype.getInstance().registerHotKey(SHORTCUT_START, JIntellitype.MOD_ALT, '1');
        JIntellitype.getInstance().registerHotKey(SHORTCUT_STOP,
                JIntellitype.MOD_CONTROL + JIntellitype.MOD_ALT, '1');
        JIntellitype.getInstance().addHotKeyListener(new DDtankHotkeyListener()); // 添加监听
        log.info("正在监听快捷键");
    }

    @Override
    public void changeStartShortcut(int modifier, char keycode) {
        JIntellitype.getInstance().registerHotKey(SHORTCUT_START, modifier, keycode);
    }

    @Override
    public void removeStartShortcut() {
        JIntellitype.getInstance().unregisterHotKey(SHORTCUT_START); // 移除快捷键 FIRST_SHORTCUT
    }

    @Override
    public Map<Long, DDTankCoreThread> getAllStartedThreadMap() {
        return this.threadMap;
    }

    @Override
    public Map<Long, DDTankStartParam> getWaitStartMap() {
        for (Long hwnd : waitStartMap.keySet()) {
            if (!dm.getWindowState(hwnd, 0)) {
                log.warn("检测到窗口关闭，已自动移除待启动脚本");
                waitStartMap.remove(hwnd);
            }
        }
        return this.waitStartMap;
    }

    @Override
    public synchronized boolean start(long hwnd, String version, String name, DDTankConfigProperties startProperties) {
        if (threadMap.get(hwnd) != null) {
            DDTankCoreThread coreThread = threadMap.get(hwnd);
            if (!coreThread.restartTask()) {
                log.warn("请勿重复启动，当前窗口已正在运行脚本");
            }
            return false;
        } else {
            DDTankStartParam startParam = waitStartMap.remove(hwnd);
            startParam.setName(name);
            // 1. 创建任务
            DDTankCoreThread thread = new DDTankCoreThread(hwnd, version, startProperties, startParam);
            threadMap.put(hwnd, thread);

            // 2. 启动线程
            thread.start();
            return true;
        }
    }

    @Override
    public Long mark() {
        Long hwnd = dm.getMousePointWindow();
        String className = dm.getWindowClass(hwnd);
        if ("MacromediaFlashPlayerActiveX".equals(className)
                // 360浏览器/qq浏览器极速模式
                || "Chrome_RenderWidgetHostHWND".equals(className)
                // 360游戏大厅前台模式
                || "NativeWindowClass".equals(className)) {
            synchronized (this) {
                DDTankCoreThread coreThread = threadMap.get(hwnd);
                if (coreThread != null) {
                    log.info("窗口[{}]已记录在首页中，对应脚本名称为[{}]；若需启动当前脚本请在首页点击重启按钮；若需要更换版本配置请在首页手动将脚本移除后再次尝试", hwnd, coreThread.getName());
                } else if (waitStartMap.get(hwnd) == null) {
                    if ("MacromediaFlashPlayerActiveX".equals(className) || "NativeWindowClass".equals(className)) {
                        log.info("已成功记录当前窗口[{}]，请前往配置页面设置启动参数", hwnd);
                        waitStartMap.put(hwnd, new DDTankStartParam());
                    } else {
                        log.warn("当前窗口[{}]为前台模式（脚本启动后鼠标和键盘操作都将变为前台），请勿随意调整窗口大小和最小化。若需要使用后台模式请将浏览器设置为兼容模式", hwnd);
                        hwnd = dm.getWindow(hwnd, 7);
                        if (waitStartMap.get(hwnd) == null) {
                            waitStartMap.put(hwnd, new DDTankStartParam(true));
                            log.info("已记录当前顶层窗口{}", hwnd);
                        } else {
                            log.info("当前标签栏{}已被记录，前台模式多开请将标签栏拖出为一个新的窗口", hwnd);
                        }
                    }
                } else {
                    log.info("已记录过当前窗口[{}]，请前往配置页面设置启动参数", hwnd);
                }
            }
        } else {
            log.warn("当前窗口类名不为[MacromediaFlashPlayerActiveX]且不为[Chrome_RenderWidgetHostHWND]！");
        }
        return hwnd;
    }

    @Override
    public void stop(long hwnd) {
        DDTankCoreThread thread = threadMap.get(hwnd);
        if(thread == null) {
            hwnd = dm.getWindow(hwnd, 7);
            thread = threadMap.get(hwnd);
        }
        if(thread != null) {
            thread.tryStop();
        }else {
            log.error("当前窗口不在脚本内记录");
        }
    }


    @Override
    public boolean updateProperties(long hwnd, DDTankConfigProperties config) {
        DDTankCoreThread thread = threadMap.get(hwnd);
        if (thread == null) {
            return false;
        }
        thread.updateProperties(config);
        return true;
    }

    @Override
    public boolean restart(long hwnd) {
        DDTankCoreThread thread = threadMap.get(hwnd);
        if (thread == null) {
            return false;
        }

        if (thread.isAlive()) {
            thread.tryStop();
        }

        thread = new DDTankCoreThread(thread);
        threadMap.put(hwnd, thread);
        thread.start();
        return true;
    }

    @Override
    public boolean remove(long hwnd) {
        DDTankCoreThread coreThread = threadMap.remove(hwnd);
        if (coreThread == null) {
            return false;
        }
        if (coreThread.isAlive()) {
            coreThread.tryStop();
        }
        return true;
    }

    class DDtankHotkeyListener implements HotkeyListener {
        @Override
        public void onHotKey(int identifier) {
            switch (identifier) {
                case SHORTCUT_START: {
                    mark();
                    break;
                }
                case SHORTCUT_STOP: {
                    long hwnd = dm.getMousePointWindow();
                    stop(hwnd);
                    break;
                }
                default:
                    log.info("监听了此快捷键但是未定义其行为");
            }
        }
    }
}