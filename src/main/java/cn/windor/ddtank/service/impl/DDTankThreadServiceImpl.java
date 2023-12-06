package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.*;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.handler.DDTankHwndMarkHandler;
import cn.windor.ddtank.handler.impl.DDTankHwndMarkHandlerImpl;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class DDTankThreadServiceImpl implements DDTankThreadService {

    @Autowired
    private Library dm;

    public static final int SHORTCUT_START = 1; // 模拟任务开始快捷键
    public static final int SHORTCUT_STOP = 2; // 模拟手动结束任务快捷键

    // 用来批量执行停止任务
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private static final Map<Long, DDTankCoreThread> threadMap = new ConcurrentHashMap<>();

    private static final Map<Long, DDTankStartParam> waitStartMap = new ConcurrentHashMap<>();

    private final DDTankHwndMarkHandler DDTankHwndMarkHandler;

    public DDTankThreadServiceImpl(Library dm) {
        this.dm = dm;
        DDTankHwndMarkHandler = new DDTankHwndMarkHandlerImpl(dm);
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

    /**
     * 启动脚本
     */
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
        long hwnd = dm.getMousePointWindow();
        if (DDTankHwndMarkHandler.isLegalHwnd(hwnd)) {
            long legalHwnd = DDTankHwndMarkHandler.getLegalHwnd(hwnd);
            synchronized (this) {
                DDTankCoreThread coreThread = threadMap.get(hwnd);
                if (coreThread != null) {
                    log.info("窗口[{}]已记录在首页中，对应脚本名称为[{}]；若需启动当前脚本请在首页点击重启按钮；若需要更换版本配置请在首页手动将脚本移除后再次尝试", hwnd, coreThread.getName());
                } else if (waitStartMap.get(hwnd) == null) {
                    if (legalHwnd == hwnd) {
                        log.info("已成功记录当前窗口[{}]，请前往配置页面设置启动参数", hwnd);
                        waitStartMap.put(hwnd, new DDTankStartParam());
                    } else {
                        log.warn("当前窗口[{}]为前台模式（脚本启动后鼠标和键盘操作都将变为前台），请勿随意调整窗口大小和最小化。若需要使用后台模式请将浏览器设置为兼容模式", hwnd);
                        if (waitStartMap.get(legalHwnd) == null) {
                            waitStartMap.put(legalHwnd, new DDTankStartParam(true));
                            log.info("已记录当前顶层窗口{}", legalHwnd);
                        } else {
                            log.info("当前标签栏{}已被记录，前台模式多开请将标签栏拖出为一个新的窗口", legalHwnd);
                        }
                    }
                } else {
                    log.info("已记录过当前窗口[{}]，请前往配置页面设置启动参数", hwnd);
                }
            }
        } else {
            log.warn("当前窗口未通过脚本预设值！");
        }
        return hwnd;
    }

    /**
     * 停止脚本
     *
     * @param hwnds 要停止的脚本列表
     * @throws InterruptedException 等待脚本停止的过程中发生了中断
     */
    @Override
    public int stop(List<Long> hwnds) throws InterruptedException {
        int result = 0;
        List<Callable<Boolean>> stopThreadList = new ArrayList<>(hwnds.size());
        for (Long hwnd : hwnds) {
            DDTankCoreThread thread = threadMap.get(hwnd);
            if (thread == null) {
                // 未找到指定脚本，尝试从父窗口中查找（前台模式绑定的是顶层父窗口）
                hwnd = dm.getWindow(hwnd, 7);
                thread = threadMap.get(hwnd);
                if (thread == null) {
                    log.warn("[脚本停止]：未找到指定脚本{}", hwnd);
                    continue;
                }
            }
            final DDTankCoreThread finalThread = thread;
            result++;
            stopThreadList.add(() -> {
                finalThread.tryStop();
                try {
                    // 等待线程终止
                    finalThread.join();
                    return true;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        threadPool.invokeAll(stopThreadList);
        return result;
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

    /**
     * 重启脚本
     *
     * @param hwnds
     * @return
     */
    @Override
    public synchronized int restart(List<Long> hwnds) throws InterruptedException {
        int result = 0;
        List<Long> aliveHwnds = new ArrayList<>(hwnds.size());
        List<Long> legalHwnds = new ArrayList<>(hwnds.size());
        for (Long hwnd : hwnds) {
            DDTankCoreThread thread = threadMap.get(hwnd);
            if (thread == null) {
                // 未找到指定脚本，尝试从父窗口中查找（前台模式绑定的是顶层父窗口）
                hwnd = dm.getWindow(hwnd, 7);
                thread = threadMap.get(hwnd);
                if (thread == null) {
                    log.warn("[脚本重启]：未找到指定脚本{}", hwnd);
                    continue;
                }
            }
            legalHwnds.add(hwnd);
            // 如果有线程存活，那么将存活的线程记录下来，准备之后停止
            if (thread.isAlive()) {
                aliveHwnds.add(hwnd);
            } else {
                result++;
            }
        }

        // 停止所有活动的线程
        result += stop(aliveHwnds);

        for (Long hwnd : legalHwnds) {
            DDTankCoreThread thread = threadMap.get(hwnd);
            if (thread == null) {
                // 未找到指定脚本，尝试从父窗口中查找（前台模式绑定的是顶层父窗口）
                hwnd = dm.getWindow(hwnd, 7);
                thread = threadMap.get(hwnd);
            }
            thread = new DDTankCoreThread(thread);
            threadMap.put(hwnd, thread);
            thread.start();
        }
        return result;
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

    @Override
    public synchronized boolean rebind(long hwnd, long newHwnd) {
        // 1. 尝试标记 newHwnd
        if (!DDTankHwndMarkHandler.isLegalHwnd(newHwnd)) {
            log.error("重绑定失败，检测到窗口{}非法！", newHwnd);
            return false;
        }

        long newLegalHwnd = DDTankHwndMarkHandler.getLegalHwnd(newHwnd);

        if (threadMap.get(newLegalHwnd) != null) {
            log.error("重绑定失败：新窗口已绑定到{}！", threadMap.get(newLegalHwnd).getName());
            return false;
        }

        // 2. 获取hwnd所挂脚本的状态, 如果线程未停止则先停止线程
        DDTankCoreThread coreThread = threadMap.get(hwnd);
        if (coreThread == null) {
            log.error("重绑定失败，检测到脚本{}已被移除，请重新创建脚本！", hwnd);
            return false;
        }
        if (coreThread.isAlive()) {
            return coreThread.rebind(newHwnd, true);
        } else {
            // 3. 调用线程的重绑定方法
            coreThread = new DDTankCoreThread(coreThread, newLegalHwnd, newLegalHwnd != newHwnd);
            coreThread.start();
            // 4. 重绑定成功，移除标记中的该窗口句柄
            waitStartMap.remove(newHwnd);
            threadMap.remove(hwnd);
            threadMap.put(newHwnd, coreThread);
        }
        return true;
    }

    @Override
    public boolean addRule(long hwnd, LevelRule rule) {
        DDTankCoreThread coreThread = threadMap.get(hwnd);
        if (coreThread == null) {
            return false;
        }

        return coreThread.addRule(rule);
    }

    @Override
    public boolean removeRule(long hwnd, int index) {
        DDTankCoreThread coreThread = threadMap.get(hwnd);
        if (coreThread == null) {
            return false;
        }
        return coreThread.removeRule(index);
    }

    @Override
    public boolean setAutoReconnect(DDTankCoreThread coreThread, String username, String password) {
        if (coreThread == null) {
            return false;
        }
        return coreThread.setAutoReconnect(username, password);
    }

    public synchronized static boolean changeBindHwnd(long hwnd, long newHwnd) {
        threadMap.put(newHwnd, threadMap.remove(hwnd));
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
                    try {
                        stop(Collections.singletonList(hwnd));
                    } catch (InterruptedException e) {
                        log.warn("脚本已在停止");
                        throw new RuntimeException(e);
                    }
                    break;
                }
                default:
                    log.info("监听了此快捷键但是未定义其行为");
            }
        }
    }
}