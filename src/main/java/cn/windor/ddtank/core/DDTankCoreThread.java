package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.LibraryFactory;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.handler.impl.DDTankCoreTaskRefindHandlerImpl;
import cn.windor.ddtank.service.impl.DDTankThreadServiceImpl;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.*;

import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

/**
 * 守护线程的任务是通过日志勘察脚本线程是否出现问题，同时调用一些其他操作
 */
@Slf4j
public class DDTankCoreThread extends Thread {

    @Getter
    private long gameHwnd;
    private String gameVersion;
    private DDTankConfigProperties properties;

    private boolean needCorrect;

    protected Library dm;

    @Getter
    private DDTankCoreTask task;

    private Thread coreThread;

    private final LinkedBlockingQueue<FutureTask<?>> daemonTaskQueue = new LinkedBlockingQueue<>();

    @Getter
    private DDTankCoreTaskRefindHandlerImpl taskRefindHandler;


    /**
     * 初始构造函数
     *
     * @param hwnd       游戏窗口句柄（能检测到游戏画面的句柄，同时需要键盘和鼠标操作在该拆窗口有效）
     * @param version    游戏版本
     * @param properties 游戏配置
     * @param startParam 起始参数，包含了线程名称、是否需要进行窗口矫正
     */
    public DDTankCoreThread(long hwnd, String version, DDTankConfigProperties properties, DDTankStartParam startParam) {
        this.gameHwnd = hwnd;
        this.gameVersion = version;
        this.properties = properties;
        this.setName(startParam.getName());
        this.needCorrect = startParam.isNeedCorrect();
        this.task = new DDTankCoreTask(gameHwnd, gameVersion, properties, needCorrect);
        this.coreThread = new Thread(task, getName() + "-exec");
    }

    /**
     * 通过现有的线程创建一个新的脚本，该方法用来重启脚本
     *
     * @param srcThread 现有脚本
     */
    public DDTankCoreThread(DDTankCoreThread srcThread) {
        this.taskRefindHandler = srcThread.taskRefindHandler;
        this.gameHwnd = srcThread.gameHwnd;
        this.gameVersion = srcThread.gameVersion;
        this.properties = srcThread.properties;
        this.setName(srcThread.getName());
        this.needCorrect = srcThread.needCorrect;
        this.task = new DDTankCoreTask(srcThread.task);
        this.coreThread = new Thread(task, getName() + "-exec");
    }

    /**
     * 通过现有的线程创建一个新的脚本，该方法用来重绑定脚本
     *
     * @param srcThread 原来的脚本线程
     * @param newHwnd   新的游戏窗口
     */
    public DDTankCoreThread(DDTankCoreThread srcThread, long newHwnd, boolean needCorrect) {
        this(srcThread);
        this.needCorrect = needCorrect;
        this.gameHwnd = newHwnd;
        this.task.hwnd = newHwnd;
    }

    @Override
    public void run() {
        this.dm = new DMLibrary(LibraryFactory.getActiveXCompnent());
        this.taskRefindHandler = new DDTankCoreTaskRefindHandlerImpl(gameHwnd, dm, getDDTankLog());
        if (task.bind(this.dm)) {
            log.info("[窗口绑定]：守护线程已成功绑定游戏窗口，即将启动脚本线程");
            task.ddtLog.success("主绑定成功，即将启动脚本");
            try {
                // 启动脚本线程
                coreThread.start();

                while (!interrupted()) {
                    if (!dm.getWindowState(gameHwnd, 0)) {
                        // 调用dm在出错时往往会弹出窗口，所以需要手动关闭线程
                        log.info("检测到游戏窗口关闭");
                        task.ddtLog.error("检测到游戏窗口关闭，即将停止脚本运行");
                        coreThread.interrupt();
                        coreThread.join();
                        System.gc();
                        // 后处理
                        dm.unbindWindow();
                        // 蛋3版本才自动重连
                        if ("10".equals(gameVersion)) {
                            long hwnd = taskRefindHandler.refindHwnd(gameHwnd);
                            if (hwnd == 0) {
                                log.info("自动重连失败");
                                task.ddtLog.error("自动重连失败，即将停止运行");
                                break;
                            } else {
                                log.info("自动重连成功");
                                task.ddtLog.success("自动重连：已重新找到窗口句柄");
                                rebind(hwnd);
                            }
                        } else {
                            break;
                        }
                    }
                    if (task.getCallTimes() > 1000000) {
                        log.info("检测到[{}]已运行达到阈值，执行重启任务以释放内存", coreThread.getName());
                        task.needRestart = true;
                        // TODO 新开辟线程等待线程结束运行
                        coreThread.join();
                        this.task = new DDTankCoreTask(this.task, true);
                        restartTask();
                    }
                    // 执行任务
                    try {
                        FutureTask<?> task = daemonTaskQueue.poll(10, TimeUnit.SECONDS);
                        if (task != null) {
                            task.run();
                        }
                    } catch (InterruptedException e) {
                        interrupt();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException ignored) {
                // 在调用某些方法的时候发生了中断，直接执行finally即可
            } finally {
                task.unBind(this.dm);
                log.info("[窗口绑定]：守护线程已成功解除绑定游戏窗口");
                task.ddtLog.success("脚本已成功停止，主绑定成功解除");
            }
        } else {
            log.error("窗口[{}]绑定失败，请重新尝试启动脚本，大漠错误码：{}", gameHwnd, dm.getLastError());
            task.ddtLog.error("主绑定失败：" + dm.getLastError());
        }
    }


    /**
     * 重绑定方法
     *
     * @param hwnd
     */
    public boolean rebind(long hwnd) {
        if (hwnd != gameHwnd) {
            if (coreThread.isAlive()) {
                stop(3000);
            }
            DDTankThreadServiceImpl.changeBindHwnd(gameHwnd, hwnd);
            task.hwnd = hwnd;
            this.gameHwnd = hwnd;
            // 当前线程重绑定
            if (dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
                task.ddtLog.success("自动重连：重绑定成功！");
                delayPersisted(1000, false);
            } else {
                task.ddtLog.error("自动重连：重绑定失败！");
                return false;
            }
            // 启动task
            restartTask();
        }
        return true;
    }

    public boolean screenshot(String filepath) {
        int[] clientSize = dm.getClientSize(gameHwnd);
        return screenshot(0, 0, clientSize[0], clientSize[1], filepath);
    }

    public boolean screenshot(int x1, int y1, int x2, int y2, String filepath) {
        try {
            FutureTask<Boolean> resultFutureTask = new FutureTask<>(() -> dm.capture(x1, y1, x2, y2, filepath));
            daemonTaskQueue.put(resultFutureTask);
            return resultFutureTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean refreshPic() {
        FutureTask<Boolean> refreshPicTask = new FutureTask<>(() -> dm.freePic("*.bmp") & task.dm.freePic("*.bmp"));
        try {
            daemonTaskQueue.put(refreshPicTask);
            return refreshPicTask.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateProperties(DDTankConfigProperties properties) {
        log.info("更新了配置信息");
        task.ddtLog.info("更新了配置信息");
        task.properties.update(properties);
    }

    public boolean isSuspend() {
        return task.suspend;
    }

    /**
     * 停止操作是线程安全的，因为isAlive()方法并不受线程上下文干扰
     */
    public void stop(long waitMillis) {
        tryStop();
        try {
            coreThread.join(waitMillis);
            this.join(waitMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (coreThread.isAlive()) {
            log.warn("线程{}未在{}ms内关闭", coreThread.getName(), waitMillis);
        }
        if (this.isAlive()) {
            log.warn("线程{}未在{}ms内关闭", this.getName(), waitMillis);
        }
    }

    public void tryStop() {
        log.info("{}尝试停止操作", getName());
        if (coreThread.isAlive() || this.isAlive()) {
            task.coreState.set(CoreThreadStateEnum.WAITING_STOP);
        }

        if (coreThread.isAlive()) {
            coreThread.interrupt();
        }
        if (this.isAlive()) {
            this.interrupt();
        }
    }

    public void sendSuspend() {
        synchronized (task) {
            if (!task.suspend) {
                switch (task.getCoreState()) {
                    case NOT_STARTED:
                    case WAITING_STOP:
                    case STOP:
                        log.info("已设定脚本启动后暂停");
                        task.ddtLog.info("已设定脚本启动后暂停");
                        break;
                    case RUN:
                        // 尝试替换状态，若被其他未加task对象锁的代码替换说明这段时间被调用了停止方法
                        if (!task.coreState.compareAndSet(CoreThreadStateEnum.RUN, CoreThreadStateEnum.WAITING_SUSPEND)) {
                            log.info("已设定脚本启动后暂停");
                            task.ddtLog.info("已设定脚本启动后暂停");
                            break;
                        }
                    case WAITING_START:
                    case WAITING_CONTINUE:
                        log.info("即将暂停");
                        task.ddtLog.info("即将暂停");
                        break;
                }
                // 由于暂停需要从多个场景中暂停
                task.suspend();
            }
        }
    }

    public void sendContinue() {
        synchronized (task) {
            if (task.suspend) {
                switch (task.getCoreState()) {
                    case NOT_STARTED:
                    case WAITING_STOP:
                    case STOP:
                        log.info("已取消下次启动后的暂停操作");
                        task.ddtLog.info("已取消下次启动后的暂停操作");
                        break;
                    case SUSPEND:
                        // 尝试替换状态，若被其他未加task对象锁的代码替换说明这段时间被调用了停止方法
                        if (!task.coreState.compareAndSet(CoreThreadStateEnum.SUSPEND, CoreThreadStateEnum.WAITING_CONTINUE)) {
                            log.info("已取消下次启动后的暂停操作");
                            task.ddtLog.info("已取消下次启动后的暂停操作");
                        } else {
                            log.info("即将恢复运行");
                            task.ddtLog.info("即将恢复运行");
                        }
                        break;
                    case WAITING_SUSPEND:
                        task.coreState.compareAndSet(CoreThreadStateEnum.WAITING_SUSPEND, CoreThreadStateEnum.RUN);
                    case WAITING_START:
                        log.info("取消暂停操作");
                        task.ddtLog.info("取消暂停操作");
                        break;
                }
                task.suspend = false;
            }
        }
    }

    public DDTankPic getDdtankPic() {
        return task.ddtankPic;
    }

    public DDTankOperate getDdtankOperate() {
        return task.ddtankOperate;
    }

    public DDTankConfigProperties getProperties() {
        return task.properties;
    }

    public DDTankLog.Log getCurrentLog() {
        return task.ddtLog.newestLog();
    }

    public int getPasses() {
        return task.getPasses();
    }

    public long getTimes() {
        return task.getCallTimes();
    }

    /**
     * 如果线程未在运行中，那么将新创建一个线程运行脚本任务
     *
     * @return
     */
    public boolean restartTask() {
        if (coreThread.isAlive()) {
            return false;
        }
        coreThread = new Thread(task, getName() + "-exec");
        coreThread.start();
        return true;
    }

    public DDTankLog getDDTankLog() {
        return task.ddtLog;
    }


    public CoreThreadStateEnum getCoreState() {
        return task.getCoreState();
    }

    public long getRunTime() {
        return task.getRunTime();
    }

    public boolean addRule(LevelRule rule) {
        return task.addLevelSelectRule(rule);
    }

    public boolean removeRule(int index) {
        return task.removeLevelSelectRule(index);
    }

    public List<LevelRule> getRules() {
        return task.getLevelSelectRules();
    }

    public boolean setAutoReconnect(String username, String password) {
        taskRefindHandler.setUsername(username);
        taskRefindHandler.setPassword(password);
        return true;
    }
}