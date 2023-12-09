package cn.windor.ddtank.core;

import cn.windor.ddtank.account.DDTankAccountSignHandler;
import cn.windor.ddtank.account.impl.SimpleDDTankAccountSignHandlerImpl;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.impl.DMKeyboard;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.base.impl.LibraryFactory;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.handler.DDTankCoreRefindHandler;
import cn.windor.ddtank.handler.DDTankStuckCheckDetectionHandler;
import cn.windor.ddtank.handler.impl.DDTankRefindByNewWindow;
import cn.windor.ddtank.handler.impl.DDTankStuckCheckDetectionByLog;
import cn.windor.ddtank.service.impl.DDTankThreadServiceImpl;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.ddtank.util.DDTankComplexObjectUpdateUtils;
import cn.windor.ddtank.util.VariantUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.thymeleaf.util.StringUtils;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.*;

import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

/**
 * 核心脚本的任务是通过日志勘察脚本线程是否出现问题，同时调用一些其他操作
 */
@Slf4j
public class DDTankCoreScript implements Serializable, Runnable {

    private static final long serialVersionUID = 1L;

    @Getter
    private long hwnd;

    @Getter
    private String name;
    private DDTankCoreTaskProperties properties;

    private boolean needCorrect;

    protected Library dm;

    @Getter
    DDTankCoreTask task;

    transient Thread coreThread;

    /**
     * 任务列表，用来测试功能
     */
    transient private LinkedBlockingQueue<FutureTask<?>> daemonTaskQueue;

    private DDTankCoreRefindHandler taskRefindHandler;

    @Getter
    private DDTankAccountSignHandler accountSignHandler;
    private DDTankStuckCheckDetectionHandler stuckCheckDetectionHandler;


    /**
     * 初始构造函数
     *
     * @param hwnd       游戏窗口句柄（能检测到游戏画面的句柄，同时需要键盘和鼠标操作在该拆窗口有效）
     * @param properties 游戏配置
     */
    public DDTankCoreScript(long hwnd, String name, DDTankCoreTaskProperties properties, boolean needCorrect) {
        this.hwnd = hwnd;
        this.properties = properties;
        this.needCorrect = needCorrect;
        this.name = name;
        this.task = new DDTankCoreTask(this.hwnd, properties, needCorrect);
        this.dm = new DMLibrary();
        this.accountSignHandler = new SimpleDDTankAccountSignHandlerImpl(dm, new DMMouse(dm.getSource()), new DMKeyboard(dm.getSource()));
        this.taskRefindHandler = new DDTankRefindByNewWindow(dm, task.ddtLog, accountSignHandler, task.properties);
        this.stuckCheckDetectionHandler = new DDTankStuckCheckDetectionByLog(task.ddtLog);
    }

    public void setProperties(DDTankCoreTaskProperties properties) {
        this.properties = properties;
        task.properties = properties;
        DDTankComplexObjectUpdateUtils.update(this, properties);
    }

    private boolean init() {
        this.coreThread = new Thread(task, name + "-task");
        if (daemonTaskQueue == null) {
            this.daemonTaskQueue = new LinkedBlockingQueue<>();
        }

        this.dm = new DMLibrary(LibraryFactory.getActiveXCompnent());
        // 一键更新所有的ActiveXComponent
        DDTankComplexObjectUpdateUtils.update(this, dm.getSource());

        // TODO 窗口检查失败
        initWindowCheck();

        // 绑定窗口
        if (!task.bind(this.dm)) {
            log.error("窗口[{}]绑定失败，请重新尝试启动脚本，大漠错误码：{}", hwnd, dm.getLastError());
            task.ddtLog.error("主绑定失败：" + dm.getLastError());
            return false;
        }

        // 启动脚本线程
        log.info("[窗口绑定]：守护线程已成功绑定游戏窗口，即将启动脚本线程");
        log.info("脚本已启动");
        coreThread.start();
        return true;
    }

    private void initWindowCheck() {
        if (!dm.getWindowState(hwnd, 0)) {
            // 窗口不存在
            getDDTankLog().warn("检测到窗口已失效，尝试自动登录");
            // 自动重连，尝试三次
            long hwnd = taskRefindHandler.refindHwnd(this.hwnd);
            int failTimes = 1;
            while (hwnd == 0 && failTimes++ < 3) {
                hwnd = taskRefindHandler.refindHwnd(this.hwnd);
            }
            if (hwnd == 0) {
                getDDTankLog().error("自动重绑定窗口失败");
                return;
            } else {
                task.ddtLog.success("已重新找到窗口句柄");
                rebind(hwnd, false);
            }
        }
    }

    @Override
    public void run() {
        if (!init()) {
            return;
        }
        try {
            while (!Thread.interrupted()) {
                if (!needRefindCheck()) {
                    break;
                }

                if (task.getCallTimes() > 1000000) {
                    log.info("检测到[{}]已运行达到阈值，执行重启任务以释放内存", coreThread.getName());
                    task.needRestart = true;
                    // TODO 新开辟线程等待线程结束运行
                    coreThread.join();
                    restartTask();
                }
                // 执行任务
                try {
                    FutureTask<?> task = daemonTaskQueue.poll(10, TimeUnit.SECONDS);
                    if (task != null) {
                        task.run();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
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
            VariantUtils.remove();
        }
    }

    /**
     * 检查是否需要进行重连
     *
     * @return
     * @throws InterruptedException
     */
    private boolean needRefindCheck() throws InterruptedException {
        boolean needRefindWindow = false;
        if (!dm.getWindowState(hwnd, 0)) {
            // 调用dm在出错时往往会弹出窗口，所以需要手动关闭线程
            log.info("检测到游戏窗口关闭");
            task.ddtLog.error("检测到游戏窗口关闭，即将重新启动");
            needRefindWindow = true;
        } else if (task.coreState.get() != CoreThreadStateEnum.SUSPEND && stuckCheckDetectionHandler.isStuck()) {
            log.info("检测到游戏卡住");
            task.ddtLog.error("检测到游戏卡住，即将重新启动");
            needRefindWindow = true;
        }
        if (needRefindWindow) {
            if(StringUtils.isEmpty(accountSignHandler.getPassword()) ||
                    StringUtils.isEmpty(accountSignHandler.getUsername()) ||
                    StringUtils.isEmpty(properties.getWebsite())) {
                log.info("检测到网站或账户或密码为空，取消重新启动");
                task.ddtLog.warn("检测到网站或账户或密码为空，取消重新启动");
                return false;
            }
            // 先打断当前的脚本线程
            coreThread.interrupt();
            coreThread.join();
            System.gc();
            // 后处理
            dm.unbindWindow();
            // 自动重连
            long hwnd = taskRefindHandler.refindHwnd(this.hwnd);
            while (hwnd == 0) {
                hwnd = taskRefindHandler.refindHwnd(this.hwnd);
            }
            if (hwnd == 0) {
                log.info("自动重连失败");
                task.ddtLog.error("自动重连失败，即将停止运行");
                return false;
            } else {
                log.info("自动重连成功");
                task.ddtLog.success("自动重连：已重新找到窗口句柄");
                rebind(hwnd, true);
            }
        }
        return true;
    }


    /**
     * 重绑定方法
     *
     * @param hwnd
     */
    public boolean rebind(long hwnd, boolean needRestart) {
        if (hwnd != this.hwnd) {
            if (coreThread.isAlive()) {
                // TODO 关闭task线程
                coreThread.interrupt();
            }
            DDTankThreadServiceImpl.changeBindHwnd(this.hwnd, hwnd);
            task.hwnd = hwnd;
            this.hwnd = hwnd;
            // 当前线程先对窗口进行重绑定，确保游戏窗口首个绑定的是守护线程
            if (dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
                task.ddtLog.success("重绑定成功！");
                delayPersisted(1000, false);
            } else {
                task.ddtLog.error("重绑定失败！");
                return false;
            }
            // 启动task
            if (needRestart) {
                restartTask();
            }
        }
        return true;
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
        this.task = new DDTankCoreTask(task);
        coreThread = new Thread(task, name + "-task");
        coreThread.start();
        return true;
    }

    public boolean screenshot(String filepath) {
        int[] clientSize = dm.getClientSize(hwnd);
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

    public void updateProperties(DDTankCoreTaskProperties properties) {
        log.info("更新了配置信息");
        task.ddtLog.info("更新了配置信息");
        task.properties.update(properties);
    }

    public boolean isSuspend() {
        return task.suspend;
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

    public DDTankCoreTaskProperties getProperties() {
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
        accountSignHandler.setUsername(username);
        accountSignHandler.setPassword(password);
        return true;
    }

    public void setHwnd(long hwnd) {
        this.hwnd = hwnd;
        this.task.hwnd = hwnd;
    }

    public void setName(String name) {
        this.name = name;
        if (coreThread != null) {
            coreThread.setName(name + "-task");
        }
        if (daemonTaskQueue != null) {
            daemonTaskQueue.add(new FutureTask<>(() -> {
                Thread.currentThread().setName(name);
                return null;
            }));
        }
    }
}