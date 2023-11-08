package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.base.impl.DMKeyboard;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.base.impl.LibraryFactory;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.impl.*;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.exception.StopTaskException;
import cn.windor.ddtank.handler.DDTankCoreAttackHandler;
import cn.windor.ddtank.handler.DDTankSelectMapHandler;
import cn.windor.ddtank.handler.impl.DDTankCoreAttackHandlerImpl;
import cn.windor.ddtank.handler.impl.DDTankSelectMapHandlerImpl;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import com.jacob.com.ComThread;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.digester.Rule;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static cn.windor.ddtank.util.ThreadUtils.delay;
import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

@Slf4j
public class DDTankCoreTask extends DDTank implements Runnable {

    // 游戏句柄
    long hwnd;

    // 游戏版本
    String version;



    @Getter
    // 已通关副本数
    private int passes;

    // 已运行循环次数
    @Getter
    private long times;

    // 脚本开始时间
    private long startTime = -1;

    // runTime是上一次暂停前运行的时间
    private long endTime = -1;
    private long runTime;
    private long suspendTime;

    boolean suspend = false;

    boolean needCorrect = false;
    private int offsetX;
    private int offsetY;

    volatile boolean needRestart = false;

    AtomicReference<CoreThreadStateEnum> coreState = new AtomicReference<>(CoreThreadStateEnum.NOT_STARTED);

    protected Library dm;

    @Getter
    protected DDTankPic ddtankPic;

    protected DDTankOperate ddtankOperate;
    protected Keyboard keyboard;
    protected Mouse mouse;

    @Getter
    protected DDTankConfigProperties properties;

    private DDTankCoreAttackHandler ddTankCoreAttackHandler;
    private DDTankSelectMapHandler ddtankSelectMapHandler;


    /**
     * 普通的新建任务方法
     * @param hwnd
     * @param version
     * @param properties
     * @param needCorrect
     */
    public DDTankCoreTask(long hwnd, String version, DDTankConfigProperties properties, boolean needCorrect) {
        super();
        this.hwnd = hwnd;
        this.version = version;
        this.coreState.set(CoreThreadStateEnum.WAITING_START);
        this.properties = properties;
        this.needCorrect = needCorrect;
    }

    /**
     * 根据现有的task再新建一个task
     * 用途：定期重启使用，防止内存溢出
     * @param task
     */
    public DDTankCoreTask(DDTankCoreTask task) {
        super(task.ddtLog);
        this.hwnd = task.hwnd;
        this.version = task.version;
        this.coreState.set(CoreThreadStateEnum.WAITING_START);
        this.properties = task.properties;
        this.passes = task.passes;
        this.times = 0;
        this.runTime = task.getRunTime();
        this.ddTankCoreAttackHandler = task.ddTankCoreAttackHandler;
        this.suspend = task.suspend;
        this.needRestart = false;
        this.needCorrect = task.needCorrect;
        this.offsetX = task.offsetX;
        this.offsetY = task.offsetY;
    }

    /**
     * 脚本的初始化操作，必须在运行线程内执行，这样大漠对象才和运行线程绑定
     */
    private void init() {
        // 大漠对象需要在外部执行，因为要调用绑定
        // 设置大漠字库
        if (!dm.setDict(0, "C:/tmp/ddtankLibrary.txt")) {
            log.error("大漠字库设置失败！");
        } else if (!dm.useDict(0)) {
            log.error("大漠字库使用失败！");
        } else {
            log.info("大漠字库设置并使用成功！");
        }

        // 设置脚本参数
        this.mouse = new DMMouse(dm.getSource());
        this.keyboard = new DMKeyboard(dm.getSource());
        if ("10".equals(version)) {
            this.ddtankPic = new DDTankPic10_4(dm, "C:/tmp/", properties, mouse);
        } else if ("2.4".equalsIgnoreCase(version)) {
            this.ddtankPic = new DDTankPic2_4(dm, "C:/tmp/", properties, mouse);
        } else {
            this.ddtankPic = new DDTankPic2_3(dm, "C:/tmp/", properties, mouse);
        }

        if ("10".equals(version)) {
            this.ddtankOperate = new DDtankOperate10_4(dm, mouse, keyboard, ddtankPic, properties);
        } else {
            this.ddtankOperate = new DDtankOperate2_3(dm, mouse, keyboard, ddtankPic, properties);
        }
        ddTankCoreAttackHandler = new DDTankCoreAttackHandlerImpl(properties, keyboard, ddtankPic, ddtankOperate, ddtLog);
        ddtankSelectMapHandler = new DDTankSelectMapHandlerImpl(properties, ddtankOperate, ddtLog);

        // 矫正坐标
        if (needCorrect && (offsetX != 0 || offsetY != 0)) {
            logInfo("检测到已矫正过坐标，自动使用上一次的矫正坐标，若需要重新矫正请将脚本删除后再启动");
            dm.setFindOffset(offsetX, offsetY);
            mouse.setOffset(offsetX, offsetY);
        } else if (needCorrect) {
            // 矫正坐标
            logInfo("开始矫正坐标...");
            int[] size = dm.getClientSize(hwnd);
            int width = size[0];
            int height = size[1];
            boolean offseted = false;
            Point result = new Point();
            long startTime = System.currentTimeMillis();
            while (!offseted) {
                if (System.currentTimeMillis() - startTime > 1000) {
                    break;
                }
                if (dm.findPic(0, 0, width, height, "C:/tmp/需要激活窗口.bmp", "202020", 0.8, 0, result)) {
                    // 10.4截图标准值：Point(x=465, y=343)
                    offsetX = result.getX() - 465;
                    offsetY = result.getY() - 343;
                    logInfo("检测到[激活窗口矫正]：" + offsetX + ", " + offsetY);
                    offseted = true;
                } else if (dm.findPic(0, 0, width, height, "C:/tmp/矫正标识.bmp", "101010", 0.8, 0, result)) {
                    // 10.4截图标准值：Point(x=381, y=572)
                    offsetX = result.getX() - 381;
                    offsetY = result.getY() - 572;
                    logInfo("检测到[聊天窗口矫正]：" + offsetX + ", " + offsetY);
                    offseted = true;
                }
            }
            if (offseted) {
                dm.setFindOffset(offsetX, offsetY);
                mouse.setOffset(offsetX, offsetY);
            } else {
                logError("当前脚本" + Thread.currentThread().getName() + "(句柄" + hwnd + ")启动方式为前台模式，且矫正失败，请在首页重启该脚本或在详细页手动矫正（当前版本矫正方式为：激活窗口矫正与聊天窗口矫正");
            }
        }
    }


    @Override
    public void run() {
        this.dm = new DMLibrary(LibraryFactory.getActiveXCompnent());
        if (bind(this.dm)) {
            init();
            try {
                startTime = System.currentTimeMillis();
                log("脚本已启动！");
                while (!Thread.interrupted()) {
                    if (needRestart) {
                        break;
                    }
                    if (suspend) {
                        long suspendStartTime;
                        // 使用compareAndSet而不是直接替换是因为在等待暂停的这段时间里调用了一些方法使状态发生了改变，例如调用了停止方法但仅仅设置了状态值还未来得及中断，使用CompareAndSet可以防止看到错误的状态。下同
                        coreState.compareAndSet(CoreThreadStateEnum.WAITING_SUSPEND, CoreThreadStateEnum.SUSPEND);
                        // 当处于暂停状态并且不需要重启时会一直等待，需要重启则会最终运行到上面的break;
                        while (suspend && !needRestart) {
                            suspendStartTime = System.currentTimeMillis();
                            delay(1000, true);
                            suspendTime += System.currentTimeMillis() - suspendStartTime;
                        }
                        coreState.compareAndSet(CoreThreadStateEnum.WAITING_CONTINUE, CoreThreadStateEnum.RUN);
                    } else {
                        if (coreState.get() != CoreThreadStateEnum.RUN) {
                            coreState.compareAndSet(CoreThreadStateEnum.WAITING_START, CoreThreadStateEnum.RUN);
                            coreState.compareAndSet(CoreThreadStateEnum.WAITING_CONTINUE, CoreThreadStateEnum.RUN);
                        }
                        try {
                            if (ddtankPic.needActiveWindow()) {
                                log("重新激活窗口");
                            }

                            if (ddtankPic.needGoingToWharf()) {
                                log("进入远征码头");
                            }

                            if (ddtankPic.needCreateRoom()) {
                                log("创建房间");
                            }

                            if (ddtankPic.needChooseMap()) {
                                ddtankSelectMapHandler.select(passes);
                            }

                            if (ddtankPic.needCloseTip()) {
                                log("关闭提示");
                            }

                            if (ddtankPic.needClickPrepare()) {
                                log("点击准备按钮");
                            }

                            if (ddtankPic.needClickStart()) {
                                log("点击开始按钮");
                                initEveryTimes();
                                delay(1000, true);
                            }

                            if (ddtankPic.isEnterLevel()) {
                                ddTankCoreAttackHandler.main();
                            }

                            if (ddtankPic.needDraw()) {
                                delay(300, true);
                                DMLibrary.capture(dm, hwnd, DDTankFileConfigProperties.getDrawDir(Thread.currentThread().getName()) + "/" + passes + ".png");
                                passes++;
                            }
                            delay(properties.getDelay(), true);
                            times++;
                        } catch (StopTaskException ignored) {
                        } catch (Exception e) {
                            e.printStackTrace();
                            logError("脚本运行过程中出现异常：" + e.toString());
                        }
                    }
                }
            } catch (StopTaskException ignore) {

            } finally {
                // 副绑定不用管
                unBind(this.dm);
            }
            endTime = System.currentTimeMillis();
            logInfo("脚本已停止");
        } else {
            log("窗口绑定失败，请重新尝试启动脚本");
            log.error("窗口绑定失败，请重新尝试启动脚本，大漠错误码：{}", dm.getLastError());
        }
        coreState.set(CoreThreadStateEnum.STOP);
        System.gc();
    }

    public boolean addLevelSelectRule(LevelRule rule) {
        return ddtankSelectMapHandler.addRule(rule);
    }

    public boolean removeLevelSelectRule(int index) {
        return ddtankSelectMapHandler.removeRule(index);
    }

    public List<LevelRule> getLevelSelectRules() {
        return ddtankSelectMapHandler.getRules();
    }

    /**
     * 每回合的初始化操作
     */
    private void initEveryTimes() {
        ddTankCoreAttackHandler.reset();
    }

    public CoreThreadStateEnum getCoreState() {
        return coreState.get();
    }

    public boolean bind(Library dm) {
        ComThread.InitSTA();
        if (dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
            log("绑定句柄：" + hwnd);
            delayPersisted(1000, false);
            return true;
        }
        return false;
    }

    public void unBind(Library dm) {
        log(hwnd + "解除绑定" + System.currentTimeMillis());
        delayPersisted(1000, false);
        log.debug("解除绑定：{}", System.currentTimeMillis());
        dm.unbindWindow();
        ComThread.Release();
    }

    public long getRunTime() {
        if (startTime == -1) {
            // 脚本未正常运行
            return runTime;
        }
        if (endTime == -1) {
            // 脚本未停止
            return runTime + System.currentTimeMillis() - startTime - suspendTime;
        } else {
            return runTime + endTime - startTime - suspendTime;
        }
    }
}
