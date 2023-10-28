package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.impl.DMKeyboard;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.base.impl.LibraryFactory;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import com.jacob.activeX.ActiveXComponent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;

/**
 * 守护线程的任务是通过日志勘察脚本线程是否出现问题，同时调用一些其他操作
 */
@Slf4j
public class DDTankCoreThread extends Thread {

    private long gameHwnd;
    private String gameVersion;
    private DDTankConfigProperties properties;

    protected Library dm;

    @Getter
    private DDTankCoreTask task;

    private Thread coreThread;

    private final LinkedBlockingQueue<FutureTask<?>> daemonTaskQueue = new LinkedBlockingQueue<>();


    public DDTankCoreThread(long hwnd, String version, DDTankConfigProperties properties, String name) {
        this.gameHwnd = hwnd;
        this.gameVersion = version;
        this.properties = properties;
        this.setName(name);
    }

    public DDTankCoreThread(DDTankCoreThread srcThread) {
        this(srcThread.gameHwnd, srcThread.gameVersion, srcThread.properties, srcThread.getName());
    }

    @Override
    public void run() {
        ActiveXComponent compnent = LibraryFactory.getActiveXCompnent();
        Library dm = new DMLibrary(compnent);
        Mouse mouse = new DMMouse(compnent);
        Keyboard keyboard = new DMKeyboard(compnent);
        this.dm = dm;
        this.task = new DDTankCoreTask(gameHwnd, dm, mouse, keyboard, gameVersion, properties);
        this.coreThread = new Thread(task, getName() + "-exec");
        if (task.bind()) {
            log.info("窗口[{}]绑定成功，即将启动脚本", gameHwnd);
            try {
                // 启动脚本线程
                coreThread.start();

                while (!interrupted()) {
                    if (!dm.getWindowState(gameHwnd, 0)) {
                        // 调用dm在出错时往往会弹出窗口，所以需要手动关闭线程
                        log.error("检测到游戏窗口关闭，停止脚本运行");
                        coreThread.interrupt();
                        // TODO 后处理
                        break;
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
            } finally {
                task.unBind();
            }
        } else {
            log.error("窗口[{}]绑定失败，请重新尝试启动脚本，大漠错误码：{}", gameHwnd, dm.getLastError());
        }
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

    public void updateProperties(DDTankConfigProperties properties) {
        task.updateMsg("更新了[" + Thread.currentThread().getName() + "]的配置文件");
        task.properties.update(properties);
    }

    public void stop(long waitMillis) {
        task.coreState = CoreThreadStateEnum.WAITING_STOP;
        if(coreThread.isAlive()) {
            coreThread.interrupt();
        }
        if(this.isAlive()) {
            this.interrupt();
        }
        try {
            coreThread.join(waitMillis);
            this.join(waitMillis);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(coreThread.isAlive()) {
            log.warn("线程{}未在{}ms内关闭", coreThread.getName(), waitMillis);
        }
        if(this.isAlive()) {
            log.warn("线程{}未在{}ms内关闭", this.getName(), waitMillis);
        }
    }

    public void sendSuspend() {
        synchronized (task) {
            if (!task.suspend) {
                task.coreState = CoreThreadStateEnum.WAITING_SUSPEND;
                task.updateMsg("暂停运行");
                task.suspend = true;
            }
        }
    }

    public void sendContinue() {
        synchronized (task) {
            if (task.suspend) {
                task.coreState = CoreThreadStateEnum.WAITING_CONTINUE;
                task.updateMsg("恢复运行");
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

    public String getCurrentMsg() {
        return task.getCurrentMsg();
    }

    public int getTimes() {
        return task.getTimes();
    }

    public boolean restartTask() {
        if(coreThread.isAlive()) {
            return false;
        }
        coreThread = new Thread(task);
        coreThread.start();
        return true;
    }


    public CoreThreadStateEnum getCoreState() {
        return task.coreState;
    }
}
