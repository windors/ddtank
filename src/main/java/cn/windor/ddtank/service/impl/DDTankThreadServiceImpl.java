package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.core.*;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.dto.DDTankThreadResponseEnum;
import cn.windor.ddtank.dto.Response;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.service.DDTankMarkHwndService;
import cn.windor.ddtank.service.DDTankThreadService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

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
     * 追踪链交给了通过hwnd找DDTankCoreScript处理
     * @return 每个脚本的启动情况
     */
    @Override
    public Map<DDTankCoreScript, Response> start(List<DDTankCoreScript> scripts) {
        scripts = new ArrayList<>(scripts);
        Map<DDTankCoreScript, Response> result = new HashMap<>();
        scripts.forEach(script -> result.put(script, DDTankThreadResponseEnum.FAIL));

        // 脚本存在检测，若脚本已被维护进threadMap则直接处理并返回即可
        for (Map.Entry<Long, DDTankCoreScriptThread> entry : threadMap.entrySet()) {
            DDTankCoreScriptThread scriptThread = entry.getValue();
            for (DDTankCoreScript script : scripts) {
                if (scriptThread.getScript() == script) {
                    // 当前脚本已经被加入到了内存中执行过
                    if (scriptThread.isAlive()) {
                        // 线程存活，忽略本次请求
                        result.put(script, DDTankThreadResponseEnum.THREAD_IS_ALIVE);
                    } else {
                        if (scriptThread.getState() == Thread.State.NEW) {
                            // 线程还未启动，直接启动
                            scriptThread.start();
                        } else {
                            // 线程已经被停止，需要再次创建新的线程对象并启动
                            scriptThread = new DDTankCoreScriptThread(script);
                            // 注意这里仅仅是替换，并没有根据script.hwnd替换key，唯一的替换在重绑定功能
                            threadMap.put(entry.getKey(), scriptThread);
                            scriptThread.start();
                        }
                        result.put(script, DDTankThreadResponseEnum.OK);
                    }
                    // 该script到这里已经处理完毕，无需再进入后面的其他流程
                    scripts.remove(script);
                    break;
                }
            }
        }

        List<DDTankCoreScript> legalScript = new ArrayList<>(scripts.size());
        // 窗口绑定检测，若目标窗口已绑定了其他脚本，则不再启动脚本
        scripts.forEach((script) -> {
            long hwnd = script.getHwnd();
            if (threadMap.get(hwnd) == null) {
                legalScript.add(script);
            } else {
                result.put(script, DDTankThreadResponseEnum.WINDOW_IS_BUNDED);
            }
        });

        // 创建线程并启动
        for (DDTankCoreScript script : legalScript) {
            long hwnd = script.getHwnd();
            DDTankCoreScriptThread thread = new DDTankCoreScriptThread(script);
            threadMap.put(hwnd, thread);
            thread.start();
            // 通知标记服务移除该对象
            markHwndService.removeByHwnd(hwnd);
            result.put(script, DDTankThreadResponseEnum.OK);
        }
        return result;
    }

    /**
     * 根据传入的脚本列表从运行中的脚本中获取包装其的线程集合
     * @return 找到的所有脚本线程，<b>注意：结果集合个数会小于等于传入的参数集合，注意外部要加以判断</b>
     */
    private List<DDTankCoreScriptThread> getExistsThreadByScript(List<DDTankCoreScript> scripts) {
        if (scripts == null || scripts.size() == 0) {
            return new ArrayList<>();
        }
        scripts = new ArrayList<>(scripts);
        // 找到scripts对应的线程
        List<DDTankCoreScriptThread> result = new ArrayList<>(scripts.size());
        for (DDTankCoreScriptThread thread : threadMap.values()) {
            for (DDTankCoreScript script : scripts) {
                // 这里使用的是等于号，所以script必须是从内存中获取的script，而非是equals的script
                if (thread.getScript() == script) {
                    result.add(thread);
                    scripts.remove(script);
                    break;
                }
            }
        }
        return result;
    }


    /**
     * 停止脚本
     *
     * @param scripts 要停止的脚本列表，必须从相关Service中获取，因为内部使用==来判断是否是同一个脚本
     */
    @SneakyThrows
    @Override
    public Map<DDTankCoreScript, Response> stop(List<DDTankCoreScript> scripts) {
        Map<DDTankCoreScript, Response> result = new HashMap<>(scripts.size(), 1);
        // 初始化结果集
        scripts.forEach(script -> result.put(script, DDTankThreadResponseEnum.FAIL));

        // 找到scripts对应的线程
        List<DDTankCoreScriptThread> existsScriptsThread = getExistsThreadByScript(scripts);
        // 封装停止任务
        List<Callable<Boolean>> stopThreadList = new ArrayList<>(existsScriptsThread.size());
        existsScriptsThread.forEach(scriptThread -> stopThreadList.add(new DDTankCoreScriptThreadStopCallable(scriptThread)));

        // 执行停止任务
        List<Future<Boolean>> stopThreadResultList = threadPool.invokeAll(stopThreadList);

        // 封装结果集
        int index = 0;
        for (Future<Boolean> stopThreadResult : stopThreadResultList) {
            DDTankCoreScriptThread thread = existsScriptsThread.get(index);
            DDTankCoreScript script = thread.getScript();
            try {
                if (stopThreadResult.get()) {
                    result.put(script, DDTankThreadResponseEnum.OK);
                }
            } catch (ExecutionException | InterruptedException e) {
                // TODO 停止过程中遇到了错误，理论上不会出现异常
                log.error("尝试停止脚本过程中遇到了异常：");
                e.printStackTrace();
            }
            index++;
        }
        return result;
    }


    /**
     * 停止线程所用的Callable接口实现
     */
    private static class DDTankCoreScriptThreadStopCallable implements Callable<Boolean> {

        /**
         * 已经尝试过停止的脚本线程
         * 由于Java中的一个线程对象调用过一次start方法后就无法再次调用start方法，所以在调用了停止方法后如果想要再次启动，则需要额外创建新的
         * DDTankCoreScriptThread对象，因此旧的DDTankCoreScriptThread对象就完全无用了，可以随意处置。
         */
        private static final Set<DDTankCoreScriptThread> triedThreadSet = new ConcurrentSkipListSet<>();

        private final DDTankCoreScriptThread scriptThread;

        public DDTankCoreScriptThreadStopCallable(DDTankCoreScriptThread scriptThread) {
            this.scriptThread = scriptThread;
        }

        @Override
        public Boolean call() throws Exception {
            if (scriptThread.isAlive()) {
                if (triedThreadSet.add(scriptThread)) {
                    // 未尝试过停止操作
                    scriptThread.tryStop();
                    scriptThread.join();
                    // 线程终止后及时的从尝试停止Set中移除，防止内存堆积
                    triedThreadSet.remove(scriptThread);
                } else {
                    // 已尝试过普通的停止方法，使用强制终止来对线程进行停止操作
                    scriptThread.stopForced();
                }
            }
            return true;
        }
    }

    @Override
    public Response updateProperties(long hwnd, DDTankCoreTaskProperties config) {
        DDTankCoreScriptThread thread = threadMap.get(hwnd);
        if (thread == null) {
            return DDTankThreadResponseEnum.WINDOW_SCRIPT_IS_NOT_EXISTS;
        }
        thread.getScript().updateProperties(config);
        return DDTankThreadResponseEnum.OK;
    }

    /**
     * 重启指定脚本
     *
     * @return
     */
    @Override
    public Map<DDTankCoreScript, Response> restart(List<DDTankCoreScript> scripts) {
        // 停止所有活动的脚本
        stop(scripts);

        // 启动所有脚本
        return start(scripts);
    }

    @Override
    public Response remove(long hwnd) {
        DDTankCoreScriptThread coreThread = threadMap.remove(hwnd);
        if (coreThread == null) {
            return DDTankThreadResponseEnum.WINDOW_SCRIPT_IS_NOT_EXISTS;
        }
        if (coreThread.isAlive()) {
            coreThread.tryStop();
        }
        return DDTankThreadResponseEnum.OK;
    }

    @Override
    public Response rebind(DDTankCoreScript script, long newHwnd) {
        long newLegalHwnd = markHwndService.getLegalHwnd(newHwnd);
        if (newLegalHwnd == 0) {
            return DDTankThreadResponseEnum.WINDOW_IS_ILLEGAL;
        }

        if (threadMap.get(newHwnd) != null) {
            return DDTankThreadResponseEnum.WINDOW_IS_BUNDED;
        }
        long hwnd = script.getHwnd();

        // 2. 获取hwnd所挂脚本的状态
        DDTankCoreScriptThread coreThread = getThread(hwnd);
        if(coreThread == null) {
            coreThread = new DDTankCoreScriptThread(script);
        }

        if (coreThread.isAlive()) {
            // 线程还在运行，调用内部的hwnd自行重绑定即可
            coreThread.getScript().rebind(newLegalHwnd, true);
        } else {
            // 线程终止，直接改变hwnd即可
            script.setHwnd(newLegalHwnd);
            coreThread = new DDTankCoreScriptThread(script);
            coreThread.start();
            // 移除等待队列中的newHwnd
            markHwndService.removeByHwnd(newHwnd);
            threadMap.remove(hwnd);
            threadMap.put(newLegalHwnd, coreThread);
        }
        return DDTankThreadResponseEnum.OK;
    }

    @Override
    public DDTankCoreScript get(long hwnd) {
        DDTankCoreScriptThread scriptThread = threadMap.get(hwnd);
        if (scriptThread == null) {
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
            if (thread.getScript() == script) {
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
     * 提供给正在运行中的脚本的静态重绑定方法，用于不停止/重启的内部重绑定
     *
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
        if (scriptThread == null) {
            return null;
        }
        return scriptThread.getScript();
    }
}