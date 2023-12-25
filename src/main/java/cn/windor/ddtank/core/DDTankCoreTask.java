package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.base.impl.DMKeyboard;
import cn.windor.ddtank.base.impl.DMLibrary;
import cn.windor.ddtank.base.impl.DMMouse;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.config.DDTankSetting;
import cn.windor.ddtank.core.impl.*;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.exception.StopTaskException;
import cn.windor.ddtank.handler.DDTankAutoUsePropHandler;
import cn.windor.ddtank.handler.DDTankSelectMapHandler;
import cn.windor.ddtank.handler.DDTankAutoCompleteHandler;
import cn.windor.ddtank.handler.impl.DDTankAutoUsePropHandlerImpl;
import cn.windor.ddtank.core.impl.DDTankCoreAttackHandlerImpl;
import cn.windor.ddtank.handler.impl.DDTankSelectMapHandlerImpl;
import cn.windor.ddtank.handler.impl.DDTankAutoCompleteHandlerImpl;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.ddtank.util.DDTankComplexObjectUpdateUtils;
import cn.windor.ddtank.util.JacobUtils;
import com.jacob.com.ComThread;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static cn.windor.ddtank.util.ThreadUtils.delay;
import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

@Slf4j
public class DDTankCoreTask implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;

    // 游戏句柄
    long hwnd;

    protected DDTankLog ddtLog;

    @Getter
    // 已通关副本数
    private int passes;

    @Getter
    // 整体设置
    protected DDTankCoreTaskProperties properties;

    // 脚本开始时间
    private long startTime = -1;

    private long endTime = -1;
    // runTime是上一次暂停前运行的时间
    private long runTime = 0;
    private long suspendTime;

    boolean suspend = false;

    boolean needCorrect = false;
    private int offsetX;
    private int offsetY;

    @Setter
    @Getter
    // 自动领取任务。负数表示不自动领取，非负数通关数 % 该值 == 0 时会执行任务
    private int taskAutoComplete = -1;

    @Getter
    @Setter
    // 自动使用道具
    private int autoUseProp = -1;


    // 如果该变量为true，则需要结束本次脚本运行，通过重启的方式释放当前线程占用的资源
    volatile boolean needRestart = false;
    // 如果该值为true，表示当前脚本是通过现有脚本复制来的，此时例如绑定、矫正等一些操作就不要再次在日志中说明
    boolean isAutoRestart = false;

    transient AtomicReference<CoreThreadStateEnum> coreState = new AtomicReference<>(CoreThreadStateEnum.NOT_STARTED);

    protected Library dm;

    @Getter
    protected DDTankPic ddtankPic;

    protected DDTankOperate ddtankOperate;
    protected Keyboard keyboard;
    protected Mouse mouse;


    // 攻击处理器，用于处理进入副本内的操作
    private DDTankCoreAttackHandler ddTankCoreAttackHandler;

    // 选择地图处理器，用于选择地图
    private DDTankSelectMapHandler ddtankSelectMapHandler;

    // 自动领取任务策略
    private DDTankAutoCompleteHandler ddTankTaskAutoCompleteHandler;
    private DDTankAutoUsePropHandler ddTankAutoUsePropHandler;

    private DDTankLevelSummary levelSummary;


    /**
     * 普通的新建任务方法
     *
     * @param hwnd
     * @param properties
     * @param needCorrect
     */
    public DDTankCoreTask(long hwnd, DDTankCoreTaskProperties properties, boolean needCorrect) {
        this.ddtLog = new DDTankLog();
        this.hwnd = hwnd;
        this.coreState.set(CoreThreadStateEnum.WAITING_START);
        this.properties = properties;
        this.needCorrect = needCorrect;

        // 复杂对象的创建
        this.dm = new DMLibrary();
        this.mouse = new DMMouse(dm.getSource());
        this.keyboard = new DMKeyboard(dm.getSource());


        this.ddTankCoreAttackHandler = new DDTankCoreAttackHandlerImpl(dm, properties, keyboard, ddtankPic, ddtankOperate, ddtLog);
        this.ddtankSelectMapHandler = new DDTankSelectMapHandlerImpl(properties, ddtankOperate, ddtLog);
        this.ddTankTaskAutoCompleteHandler = new DDTankAutoCompleteHandlerImpl(keyboard, mouse);
        this.ddTankAutoUsePropHandler = new DDTankAutoUsePropHandlerImpl(mouse, keyboard, dm, ddtLog);
        this.levelSummary = new DDTankLevelSummaryImpl(properties);
    }

    /**
     * 根据现有的task再新建一个task
     * 用途：定期重启使用，防止内存溢出
     *
     * @param task
     */
    public DDTankCoreTask(DDTankCoreTask task) {
        this.ddtLog = task.ddtLog;
        this.hwnd = task.hwnd;
        this.coreState.set(CoreThreadStateEnum.WAITING_START);
        this.properties = task.properties;
        this.passes = task.passes;
        this.runTime = task.getRunTime();
        this.taskAutoComplete = task.taskAutoComplete;
        this.autoUseProp = task.autoUseProp;
        this.suspend = task.suspend;
        this.needRestart = false;
        this.needCorrect = task.needCorrect;
        this.offsetX = task.offsetX;
        this.offsetY = task.offsetY;
        this.keyboard = task.keyboard;
        this.mouse = task.mouse;
        this.ddTankCoreAttackHandler = task.ddTankCoreAttackHandler;
        this.ddtankSelectMapHandler = task.ddtankSelectMapHandler;
        this.ddTankTaskAutoCompleteHandler = task.ddTankTaskAutoCompleteHandler;
        this.ddTankAutoUsePropHandler = task.ddTankAutoUsePropHandler;
        this.levelSummary = task.levelSummary;
    }

    /**
     * 自动重启时调用的构造方法，主要是日志输出
     * @param task
     * @param isAutoRestart
     */
    public DDTankCoreTask(DDTankCoreTask task, boolean isAutoRestart) {
        this(task);
        this.isAutoRestart = isAutoRestart;
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
            if (!isAutoRestart) {
                log.info("大漠字库设置并使用成功！");
                ddtLog.success("大漠字库设置并使用成功");
            }
        }

        // 重新开始计时
        runTime = getRunTime();
        if(runTime < 0) {
            log.error("时间算法有误，请");
        }
        startTime = System.currentTimeMillis();
        endTime = -1;

        // 更新对象
        String picDir = new File(DDTankFileConfigProperties.getBaseDir(), properties.getPicDir()).getAbsolutePath() + "/";
        String version = properties.getVersion();
        if ("10".equals(version)) {
            this.ddtankPic = new DDTankPic10_4(dm, picDir, properties, mouse);
        } else if ("2.4".equalsIgnoreCase(version)) {
            this.ddtankPic = new DDTankPic2_4(dm, picDir, properties, mouse);
        } else {
            this.ddtankPic = new DDTankPic2_3(dm, picDir, properties, mouse);
        }
        if ("10".equals(version)) {
            this.ddtankOperate = new DDtankOperate10_4(dm, mouse, keyboard, ddtankPic, properties);
        } else {
            this.ddtankOperate = new DDtankOperate2_3(dm, mouse, keyboard, ddtankPic, properties);
        }
        DDTankComplexObjectUpdateUtils.update(this, dm.getSource(), ddtankPic, ddtankOperate);

        // 跳过过场动画
        mouse.moveAndClick(21, 519);

        // 首次启动时向控制台说明当前为前台模式启动，重启等操作就不会再重复
        if (!isAutoRestart && needCorrect) {
            log.warn("当前窗口无法开启后台模式，可能浏览器使用了极速模式的内核，此时脚本无法获取到游戏窗口截图。即将以前台模式启动。");
        }

        // 矫正坐标，需要矫正时检测是否已经矫正过（偏移不为0）
        if (needCorrect && (offsetX != 0 || offsetY != 0)) {
            // TODO 添加手动矫正
            if (!isAutoRestart) {
                log.info("[矫正坐标]：检测到已矫正过坐标，自动使用上一次的矫正坐标，若需要重新矫正请将脚本删除");
            }
            dm.setFindOffset(offsetX, offsetY);
            mouse.setOffset(offsetX, offsetY);
        } else if (needCorrect) {
            // 矫正坐标
            log.info("[矫正坐标]：开始矫正坐标，当前版本矫正坐标方式为：检测激活窗口图片（标准值 Point(x=465, y=343)）与矫正标识图片（标准值 Point(x=381, y=572)）");
            ddtLog.warn("开始矫正坐标");
            int[] size = dm.getClientSize(hwnd);
            int width = size[0];
            int height = size[1];
            boolean offseted = false;
            Point result = new Point();
            long startTime = System.currentTimeMillis();
            while (!offseted) {
                // 如果超过1秒未成功矫正则退出
                if (System.currentTimeMillis() - startTime > 1000) {
                    break;
                }
                if (dm.findPic(0, 0, width, height, "C:/tmp/需要激活窗口.bmp", "202020", 0.8, 0, result)) {
                    // 10.4截图标准值：
                    offsetX = result.getX() - 465;
                    offsetY = result.getY() - 343;
                    if (!isAutoRestart) {
                        log.info("[矫正坐标]：检测到需要激活窗口，已成功矫正坐标，当前校正值 x：{}, y：{}，请勿随意更改游戏窗口大小且确保游戏窗口不被阻挡", offsetX, offsetY);
                        ddtLog.success("矫正起始点：" + offsetX + ", " + offsetY);
                    }
                    offseted = true;
                } else if (dm.findPic(0, 0, width, height, "C:/tmp/矫正标识.bmp", "101010", 0.8, 0, result)) {
                    // 10.4截图标准值：Point(x=381, y=572)
                    offsetX = result.getX() - 381;
                    offsetY = result.getY() - 572;
                    if (!isAutoRestart) {
                        log.info("[矫正坐标]：检测到矫正标识，已成功矫正坐标，当前校正值 x：{}, y：{}，请勿随意更改游戏窗口大小且确保游戏窗口不被阻挡", offsetX, offsetY);
                        ddtLog.success("矫正起始点：" + offsetX + ", " + offsetY);
                    }
                    offseted = true;
                }
            }
            if (offseted) {
                dm.setFindOffset(offsetX, offsetY);
                mouse.setOffset(offsetX, offsetY);
            } else {
                if (!isAutoRestart) {
                    log.warn("[矫正坐标]：自动矫正失败");
                    ddtLog.warn("自动矫正失败");
                }
            }
        }
    }


    @Override
    public void run() {
        this.dm = new DMLibrary(JacobUtils.getActiveXCompnent());
        if (bind(this.dm)) {
            if (!isAutoRestart) {
                log.info("[窗口绑定]：已成功绑定游戏窗口");
                ddtLog.success("副绑定成功");
            }
            init();
            try {
                startTime = System.currentTimeMillis();
                ddtLog.success("脚本已启动并运行");
                while (!Thread.interrupted()) {
                    if (needRestart) {
                        break;
                    }
                    if (suspend) {
                        long suspendStartTime;
                        coreState.set(CoreThreadStateEnum.SUSPEND);
                        // 当处于暂停状态并且不需要重启时会一直等待，需要重启则会最终运行到上面的break;
                        while (suspend && !needRestart) {
                            suspendStartTime = System.currentTimeMillis();
                            delay(1000, true);
                            suspendTime += System.currentTimeMillis() - suspendStartTime;
                        }
                        // 期间可能调用了停止方法，会导致状态变为等待停止，此时就不应该替换为RUN
                        coreState.compareAndSet(CoreThreadStateEnum.WAITING_CONTINUE, CoreThreadStateEnum.RUN);
                    } else {
                        coreState.set(CoreThreadStateEnum.RUN);
                        try {
                            if (ddtankPic.needActiveWindow()) {
                                ddtLog.info("重新激活窗口");
                            }

                            if (ddtankPic.needGoingToWharf()) {
                                ddtLog.info("进入远征码头");
                            }

                            if (ddtankPic.needCreateRoom()) {
                                ddtLog.info("创建房间");
                            }

                            if (ddtankPic.needChooseMap()) {
                                ddtankSelectMapHandler.select(passes);
                                if ("10".equals(properties.getVersion())) {
                                    delay(1000, true);
                                }
                            }

                            if (ddtankPic.needCloseTip()) {
                                ddtLog.info("关闭提示");
                            }

                            if (ddtankPic.needClickPrepare()) {
                                ddtLog.info("点击准备按钮");
                                initEveryTimes();
                            }

                            if (ddtankPic.needClickStart()) {
                                ddtLog.info("点击开始按钮");
                                initEveryTimes();
                            }

                            if (ddtankPic.isEnterLevel()) {
                                ddTankCoreAttackHandler.main();
                            }

                            if (ddtankPic.needDraw()) {
                                delay(300, true);

                                // 翻牌截图
                                if(DDTankSetting.isDrawCapture()) {
                                    DMLibrary.capture(dm, hwnd, DDTankFileConfigProperties.getDrawDir(Thread.currentThread().getName()) + "/" + passes + ".png");
                                }
                                ddtLog.success("第" + ++passes + "次副本已通关");

                                // 关卡记录结束
                                levelSummary.summary();

                                // 执行自动领任务操作
                                if(taskAutoComplete > 0 && passes % taskAutoComplete == 0) {
                                    // 等待翻牌结束后的黑屏时间
                                    delay(3000, true);
                                    ddtLog.primary("执行自动领取任务");
                                    ddTankTaskAutoCompleteHandler.completeTask();
                                }

                                // 执行自动使用道具
                                if(autoUseProp > 0 && passes % autoUseProp == 0) {
                                    // 等待翻牌结束后的黑屏时间
                                    delay(3000, true);
                                    ddtLog.primary("执行自动使用道具");
                                    // TODO 简易版，等待更改为更灵活的插口
                                    ddTankAutoUsePropHandler.useProp();
                                }

                                // 执行固定延迟
                                delay((long) (properties.getLevelEndWaitTime() * 1000), true);
                            }

                            // 整体流程检测间隔延迟
                            delay(properties.getDelay(), true);
                        } catch (StopTaskException ignored) {
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("脚本运行过程中出现异常：" + e.toString());
                            ddtLog.error("脚本运行过程中出现异常：" + e.toString());
                            delay(1000, true);
                        }
                    }
                }
            } catch (StopTaskException ignore) {

            } finally {
                // 副绑定不用管
                dm.unbindWindow();
                JacobUtils.release();
            }
            endTime = System.currentTimeMillis();
            log.info("脚本停止运行");
        } else {
            log.error("窗口绑定失败，请重新尝试启动脚本，大漠错误码：{}", dm.getLastError());
            ddtLog.error("窗口绑定失败：" + dm.getLastError());
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
        if(ddtankSelectMapHandler == null) {
            return Collections.emptyList();
        }
        return ddtankSelectMapHandler.getRules();
    }

    /**
     * 每回合的初始化操作
     */
    private void initEveryTimes() {
        ddTankCoreAttackHandler.reset();
    }

    public CoreThreadStateEnum getCoreState() {
        if(coreState == null) {
            coreState = new AtomicReference<>(CoreThreadStateEnum.NOT_STARTED);
        }
        return coreState.get();
    }

    public boolean bind(Library dm) {
        ComThread.InitSTA();
        if (dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
            delayPersisted(1000, false);
            return true;
        }
        return false;
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
            // 脚本已停止运行
            return runTime + endTime - startTime - suspendTime;
        }
    }

    public long getCallTimes() {
        if (dm == null) {
            return 0;
        }
        return ((DMLibrary) dm).getCallTimes();
    }

    public void suspend() {
        this.suspend = true;
        this.ddTankCoreAttackHandler.suspend();
    }

    public Map<DDTankLevel, Integer> getSummary() {
        return levelSummary.getSummary();
    }

    public void updateFieldIfNull() {
        properties.updateFieldIfNull();
        if(levelSummary == null) {
            levelSummary = new DDTankLevelSummaryImpl(properties);
        }
    }
}