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
import cn.windor.ddtank.core.impl.*;
import cn.windor.ddtank.exception.StopTaskException;
import cn.windor.ddtank.handler.DDTankFindPositionMoveHandler;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.ddtank.type.TowardEnum;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.ComThread;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static cn.windor.ddtank.util.ThreadUtils.delay;
import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

@Slf4j
public class DDTankCoreTask implements Runnable {

    // 游戏句柄
    long hwnd;

    // 回合数
    int round;

    String version;

    // 朝向
    TowardEnum toward;

    @Getter
    // 已通关副本数
    private int passes;

    // 已运行循环次数
    @Getter
    private long times;

    private long startTime = -1;
    // runTime是上一次暂停前运行的时间
    private long endTime = -1;
    private long runTime;

    // 1距的距离
    protected Double distance;

    protected Point myPosition, enemyPosition, myLastPosition, enemyLastPosition;

    protected Integer angle;

    protected Double strength;

    private boolean isCalcedDistance;

    boolean suspend = false;

    volatile boolean needRestart = false;

    AtomicReference<CoreThreadStateEnum> coreState = new AtomicReference<>(CoreThreadStateEnum.NOT_STARTED);

    // 当前状态消息，给外部提供查看接口
    protected DDTankLog ddtLog = new DDTankLog();


    protected Library dm;

    @Getter
    protected DDTankPic ddtankPic;

    protected DDTankOperate ddtankOperate;
    protected Keyboard keyboard;
    protected Mouse mouse;

    @Getter
    protected DDTankConfigProperties properties;

    DDTankCoreHandlerSelector handlerSelector;


    public DDTankCoreTask(long hwnd, String version, DDTankConfigProperties properties) {
        this.hwnd = hwnd;
        this.version = version;
        this.coreState.set(CoreThreadStateEnum.WAITING_START);
        this.properties = properties;
    }

    public DDTankCoreTask(DDTankCoreTask task) {
        this(task.hwnd, task.version, task.properties);
        this.round = task.round;
        this.toward = task.toward;
        this.passes = task.passes;
        this.times = 0;
        this.runTime = task.getRunTime();
        this.distance = task.distance;
        this.myPosition = task.myPosition;
        this.enemyPosition = task.enemyPosition;
        this.myLastPosition = task.myLastPosition;
        this.enemyLastPosition = task.enemyLastPosition;
        this.angle = task.angle;
        this.strength = task.strength;
        this.isCalcedDistance = task.isCalcedDistance;
        this.suspend = task.suspend;
        this.needRestart = false;
        this.ddtLog = task.ddtLog;

    }

    private void init() {
        // 将大漠组件推迟到运行
        ActiveXComponent compnent = LibraryFactory.getActiveXCompnent();
        this.dm = new DMLibrary(compnent);
        this.mouse = new DMMouse(compnent);
        this.keyboard = new DMKeyboard(compnent);
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
        this.handlerSelector = new DDTankCoreHandlerSelector(keyboard, properties);
    }


    @Override
    public void run() {
        init();
        if (bind(this.dm)) {
            // 设置大漠字库
            if (!dm.setDict(0, "C:/tmp/ddtankLibrary.txt")) {
                log.error("大漠字库设置失败！");
            } else if (!dm.useDict(0)) {
                log.error("大漠字库使用失败！");
            } else {
                myPosition = new Point();
                enemyPosition = new Point();
                myLastPosition = new Point();
                enemyLastPosition = new Point();
                log.info("大漠字库设置并使用成功！");
            }

            try {
                startTime = System.currentTimeMillis();
                updateLog("脚本已启动！");
                while (!Thread.interrupted()) {
                    if (suspend) {
                        // 使用compareAndSet而不是直接替换是因为在等待暂停的这段时间里调用了一些方法使状态发生了改变，例如调用了停止方法但仅仅设置了状态值还未来得及中断，使用CompareAndSet可以防止看到错误的状态。下同
                        coreState.compareAndSet(CoreThreadStateEnum.WAITING_SUSPEND, CoreThreadStateEnum.SUSPEND);
                        while (suspend) {
                            delay(1000, true);
                        }
                        coreState.compareAndSet(CoreThreadStateEnum.WAITING_CONTINUE, CoreThreadStateEnum.RUN);
                    } else {
                        if (needRestart) {
                            // 只有在运行情况下才能够让程序自主重启
                            break;
                        }
                        if (coreState.get() != CoreThreadStateEnum.RUN) {
                            coreState.compareAndSet(CoreThreadStateEnum.WAITING_START, CoreThreadStateEnum.RUN);
                            coreState.compareAndSet(CoreThreadStateEnum.WAITING_CONTINUE, CoreThreadStateEnum.RUN);
                        }
                        try {
                            if (ddtankPic.needActiveWindow()) {
                                updateLog("窗口未被激活，已重新激活窗口");
                            }

                            if (ddtankPic.needGoingToWharf()) {
                                updateLog("检测到处于大厅，自动进入远征码头");
                            }

                            if (ddtankPic.needCreateRoom()) {
                                updateLog("检测到处于远征码头，自动创建房间");
                            }

                            if (ddtankPic.needChooseMap()) {
                                updateLog("检测到未选择副本，自动选择副本：" + properties.getLevelLine() + "行" + properties.getLevelRow() + "列");
                                ddtankOperate.chooseMap();
                                // TODO 未选择上脚本
                            }

                            if (ddtankPic.needCloseTip()) {
                                updateLog("检测到提示，已自动关闭");
                            }

                            if (ddtankPic.needClickPrepare()) {
                                updateLog("检测到准备，点击准备按钮");
                            }

                            if (ddtankPic.needClickStart()) {
                                updateLog("检测到开始，点击开始按钮");
                                initEveryTimes();
                            }

                            if (ddtankPic.isEnterLevel()) {
                                // 进图就测量屏距
                                if (properties.getIsCalcDistanceQuickly() && !properties.getIsHandleCalcDistance() && !properties.getIsHandleAttack() && !isCalcedDistance) {
                                    distance = ddtankPic.calcUnitDistance();
                                    isCalcedDistance = true;
                                    updateLog("勾选了快速测量屏距：" + distance);
                                }
                                if (ddtankPic.isMyRound()) {
                                    round = round + 1;
                                    updateLog("第" + (passes + 1) + "次副本，第" + round + "回合");
                                    attack();
                                }
                            }

                            if (ddtankPic.needDraw()) {
                                passes++;
                            }
                            delay(properties.getDelay(), true);
                            times++;
                            log.trace("脚本运行中。。。");

                        } catch (StopTaskException ignored) {
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("脚本出现异常：{}", e.toString());
                        }
                    }
                }
            } finally {
                // 副绑定不用管
                unBind(this.dm);
            }
            endTime = System.currentTimeMillis();
            updateLog("脚本已停止");
        } else {
            updateLog("窗口绑定失败，请重新尝试启动脚本");
            log.error("窗口绑定失败，请重新尝试启动脚本，大漠错误码：{}", dm.getLastError());
        }
        if (!suspend) {
            // 当前脚本未处于暂停状态，可以使用startTime

        }
        coreState.set(CoreThreadStateEnum.STOP);
        System.gc();
    }

    private void initEveryTimes() {
        round = 0;
        toward = TowardEnum.UNKNOWN;
        isCalcedDistance = false;
    }

    public void updateLog(String msg) {
        ddtLog.log(msg);
        log.debug(msg);
    }

    public String getCurrentMsg() {
        return ddtLog.newestLog();
    }

    private void attack() {
        // 屏距测量
        String skill = properties.getAttackSkill().toLowerCase();
        if (skill.toLowerCase().contains("p")) {
            keyboard.keysPress(skill);
        } else {
            if (properties.getIsHandleAttack()) {
                isCalcedDistance = true;
            } else if (properties.getIsHandleCalcDistance()) {
                distance = properties.getHandleDistance();
            } else if (!properties.getIsHandleCalcDistance() && !isCalcedDistance) {
                // 需要自动计算屏距
                distance = ddtankPic.calcUnitDistance();
                isCalcedDistance = true;
                updateLog("自动测量屏距：1距=" + distance + "px");
            }

            if (properties.getIsHandleAttack()) {
                // 手动攻击
                angle = properties.getHandleAngle();
                strength = properties.getHandleStrength();
                ddtankOperate.angleAdjust(angle);
                ddtankOperate.attack(strength);
            } else {
                attackAuto();
            }
        }
    }

    private void attackAuto() {
        int tiredTimes = 0;
        boolean needFind = true;
        while (needFind && ddtankPic.isMyRound()) {
            for (int i = 0; i < 10; i++) {
                if ((myPosition = ddtankPic.getMyPosition()) != null) {
                    needFind = false;
                    break;
                }
            }
            if (myPosition == null) {
                tiredTimes++;
                log.info("未找到位置，尝试走位。若长时间未找到分析是否是小地图截取太小的问题。");
                DDTankFindPositionMoveHandler positionMoveHandler = handlerSelector.getPositionMoveHandler();
                if (!positionMoveHandler.move(++tiredTimes)) {
                    break;
                }
            }
        }
        if (myPosition != null) {
            myLastPosition.setX(myPosition.getX());
            myLastPosition.setY(myPosition.getY());
            if ((enemyPosition = ddtankPic.getEnemyPosition()) != null) {
                enemyLastPosition.setX(enemyPosition.getX());
                enemyLastPosition.setY(enemyPosition.getY());
                angle = ddtankOperate.getBestAngle(myPosition, enemyPosition);
                if (properties.getAttackTurn()) {
                    // 开启了攻击转向
                    if (enemyPosition.getX() > myPosition.getX()) {
                        // 敌人在右
                        keyboard.keyPress('a');
                        keyboard.keyPress('d');
                        toward = TowardEnum.RIGHT;
                    } else {
                        keyboard.keyPress('d');
                        keyboard.keyPress('a');
                        toward = TowardEnum.LEFT;
                    }
                    delay(300, true);
                } else {
                    toward = ddtankPic.getToward();
                    if (toward == TowardEnum.LEFT && enemyPosition.getX() > myPosition.getX()) {
                        keyboard.keyPress('d');
                        updateLog("检测到当前方向向左，敌人在右，已自动转向");
                    } else if (toward == TowardEnum.RIGHT && enemyPosition.getX() < myPosition.getX()) {
                        keyboard.keyPress('a');
                        updateLog("检测到当前方向向右，敌人在左，已自动转向");
                    }
                }

                // 走位并调整角度
                TowardEnum targetToward;
                if (myPosition.getX() < enemyPosition.getX()) {
                    targetToward = TowardEnum.RIGHT;
                } else {
                    targetToward = TowardEnum.LEFT;
                }
                if (ddtankOperate.angleAdjust(angle, handlerSelector.getAngleMoveHandler(), targetToward)) {
                    double horizontal = (enemyPosition.getX() - myPosition.getX()) / distance;
                    double vertical = (myPosition.getY() - enemyPosition.getY()) / distance;
                    updateLog("我的坐标：" + myPosition + ", 敌人的坐标：" + enemyPosition + ", 水平屏距：" + horizontal + ", 垂直屏距：" + vertical);
                    strength = ddtankOperate.getStrength(angle, horizontal, vertical);
                    strength += properties.getOffsetStrength();
                    updateLog("自动攻击：" + angle + "度, " + strength + "力");
                    keyboard.keysPress(properties.getAttackSkill(), 10);
                    ddtankOperate.attack(strength);
                } else {
                    // TODO 调整角度失败的情况，即调整角度策略失效
                    updateLog("未能调整到指定角度，当前角度：" + ddtankPic.getAngle() + ", 目标角度：" + angle);
                }
            }
        }
    }


    public CoreThreadStateEnum getCoreState() {
        return coreState.get();
    }

    public boolean bind(Library dm) {
        ComThread.InitSTA();
        if (dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
            updateLog("绑定句柄：" + hwnd);
            delayPersisted(1000, false);
            return true;
        }
        return false;
    }

    public void unBind(Library dm) {
        updateLog(hwnd + "解除绑定");
        delayPersisted(1000, false);
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
            return runTime + System.currentTimeMillis() - startTime;
        } else {
            return runTime + endTime - startTime;
        }
    }
}
