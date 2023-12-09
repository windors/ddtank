package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.*;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.handler.DDTankHwndMarkHandler;
import cn.windor.ddtank.handler.impl.DDTankHwndMarkHandlerImpl;
import cn.windor.ddtank.service.DDTankMarkHwndService;
import cn.windor.ddtank.service.DDTankThreadService;
import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class DDTankThreadServiceImpl implements DDTankThreadService {

    @Autowired
    private Library dm;

    @Autowired
    private DDTankMarkHwndService markHwndService;

    // 用来批量执行停止任务
    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    /**
     * 窗口-脚本线程映射，脚本的启动/重启/停止都由该变量维护
     */
    private static final Map<Long, DDTankCoreScriptThread> threadMap = new ConcurrentHashMap<>();

    /**
     * 启动脚本
     */
    @Override
    public boolean start(DDTankCoreScript coreScript) {
        long hwnd = coreScript.getHwnd();
        DDTankCoreScriptThread thread;
        synchronized (threadMap) {
            if ((thread = threadMap.get(hwnd)) != null) {
                // 指定脚本已保存在线程映射中
                DDTankCoreScript script = thread.getScript();
                if (script == coreScript) {
                    if (!thread.isAlive()) {
                        // 如果脚本线程已停止运行则再次启动脚本
                        thread = new DDTankCoreScriptThread(script);
                        threadMap.put(hwnd, thread);
                    }
                    return true;
                }else {
                    log.warn("启动失败，当前窗口[{}]已绑定脚本[{}]", hwnd, thread.getScript().getName());
                    return false;
                }
            } else {
                // 当前窗口未运行过脚本，则直接启动脚本即可。
                thread = new DDTankCoreScriptThread(coreScript);
                thread.start();
                threadMap.put(hwnd, thread);
                markHwndService.removeByHwnd(hwnd);
                return true;
            }
        }
    }

    @Override
    public boolean start(DDTankCoreScriptThread coreScriptThread) {
        if(coreScriptThread.getState() == Thread.State.TERMINATED) {
            // 如果线程终止则调用start(script)
            return start(new DDTankCoreScriptThread(coreScriptThread.getScript()));
        }

        // 否则现有线程可以
        return false;
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
            DDTankCoreScriptThread thread = threadMap.get(hwnd);
            if (thread == null) {
                // 未找到指定脚本，有可能是在换绑后前端未刷新仍然传送的旧句柄，先进行窗口存在检测
                if(!dm.getWindowState(hwnd, 0)) {
                    continue;
                }
                // 窗口未失效，尝试从父窗口中查找（前台模式绑定的是顶层父窗口）
                hwnd = dm.getWindow(hwnd, 7);
                thread = threadMap.get(hwnd);
                if (thread == null) {
                    log.warn("[脚本停止]：未找到指定脚本{}", hwnd);
                    continue;
                }
            }
            final DDTankCoreScriptThread finalThread = thread;
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
    public boolean updateProperties(long hwnd, DDTankCoreTaskProperties config) {
        DDTankCoreScriptThread thread = threadMap.get(hwnd);
        if (thread == null) {
            return false;
        }
        thread.getScript().updateProperties(config);
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
            DDTankCoreScriptThread thread = threadMap.get(hwnd);
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
            DDTankCoreScriptThread thread = threadMap.get(hwnd);
            if (thread == null) {
                // 未找到指定脚本，尝试从父窗口中查找（前台模式绑定的是顶层父窗口）
                hwnd = dm.getWindow(hwnd, 7);
                thread = threadMap.get(hwnd);
            }
            thread = new DDTankCoreScriptThread(thread.getScript());
            threadMap.put(hwnd, thread);
            thread.start();
        }
        return result;
    }

    @Override
    public boolean remove(long hwnd) {
        DDTankCoreScriptThread coreThread = threadMap.remove(hwnd);
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
        long newLegalHwnd = markHwndService.getLegalHwnd(newHwnd);
        if(newLegalHwnd == 0) {
            log.error("重绑定失败：新窗口无效！");
            return false;
        }

        if (threadMap.get(newHwnd) != null) {
            log.error("重绑定失败：新窗口已绑定到{}！", threadMap.get(newLegalHwnd).getName());
            return false;
        }

        // 2. 获取hwnd所挂脚本的状态
        DDTankCoreScriptThread coreThread = threadMap.get(hwnd);
        if (coreThread == null) {
            log.error("重绑定失败：检测到窗口{}绑定脚本已被移除，请重新创建脚本！", hwnd);
            return false;
        }
        if (coreThread.isAlive()) {
            // 线程还在运行，调用内部的hwnd自行重绑定即可
            return coreThread.getScript().rebind(newLegalHwnd, true);
        } else {
            // 线程终止，直接改变hwnd即可
            DDTankCoreScript script = coreThread.getScript();
            script.setHwnd(newLegalHwnd);
            coreThread = new DDTankCoreScriptThread(script);
            coreThread.start();
            // 移除等待队列中的newHwnd
            markHwndService.removeByHwnd(newHwnd);
            threadMap.remove(hwnd);
            threadMap.put(newLegalHwnd, coreThread);
        }
        return true;
    }

    @Override
    public boolean addRule(long hwnd, LevelRule rule) {
        DDTankCoreScriptThread coreThread = threadMap.get(hwnd);
        if (coreThread == null) {
            return false;
        }

        return coreThread.getScript().addRule(rule);
    }

    @Override
    public boolean removeRule(long hwnd, int index) {
        DDTankCoreScriptThread coreThread = threadMap.get(hwnd);
        if (coreThread == null) {
            return false;
        }
        return coreThread.getScript().removeRule(index);
    }

    @Override
    public boolean setAutoReconnect(DDTankCoreScript coreThread, String username, String password) {
        if (coreThread == null) {
            return false;
        }
        return coreThread.setAutoReconnect(username, password);
    }

    @Override
    public DDTankCoreScript get(long hwnd) {
        DDTankCoreScriptThread scriptThread = threadMap.get(hwnd);
        if(scriptThread == null) {
            return null;
        }
        return scriptThread.getScript();
    }

    @Override
    public DDTankCoreScriptThread getThread(long hwnd) {
        return threadMap.get(hwnd);
    }

    @Override
    public boolean isRunning(DDTankCoreScript script) {
        for (DDTankCoreScriptThread thread : threadMap.values()) {
            if(thread.getScript() == script) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<Long, DDTankCoreScript> getAllStartedScriptMap() {
        Map<Long, DDTankCoreScript> scriptMap = new HashMap<>();
        for (Long hwnd : threadMap.keySet()) {
            scriptMap.put(hwnd, threadMap.get(hwnd).getScript());
        }
        return scriptMap;
    }

    /**
     * 提供给正在运行中的脚本的重绑定方法，用于不停止/重启的内部重绑定
     * @param hwnd
     * @param newHwnd
     * @return
     */
    public static boolean changeBindHwnd(long hwnd, long newHwnd) {
        threadMap.put(newHwnd, threadMap.remove(hwnd));
        return true;
    }

    public static DDTankCoreScript getRunningScript(long hwnd) {
        DDTankCoreScriptThread scriptThread = threadMap.get(hwnd);
        if(scriptThread == null) {
            return null;
        }
        return scriptThread.getScript();
    }
}