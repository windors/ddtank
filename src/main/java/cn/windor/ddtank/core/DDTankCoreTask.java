package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.impl.*;
import cn.windor.ddtank.exception.StopTaskException;
import cn.windor.ddtank.handler.DDTankAngleAdjustMoveHandler;
import cn.windor.ddtank.handler.DDTankFindPositionMoveHandler;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.ddtank.type.TowardEnum;
import com.jacob.com.ComThread;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static cn.windor.ddtank.util.ThreadUtils.delay;

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
    private int times;

    // 1距的距离
    protected Double distance;

    protected Point myPosition, enemyPosition, myLastPosition, enemyLastPosition;

    protected Integer angle;

    protected Double strength;

    private boolean isCalcedDistance;

    boolean suspend = false;

    @Getter
    CoreThreadStateEnum coreState;

    // 当前状态消息，给外部提供查看接口
    // TODO 将msg设置为一个定长集合，便于查看历史消息
    protected String msg;


    protected Library dm;

    @Getter
    protected DDTankPic ddtankPic;

    protected DDTankOperate ddtankOperate;
    protected Keyboard keyboard;

    @Getter
    protected DDTankConfigProperties properties;

    DDTankCoreHandlerSelector handlerSelector;


    public DDTankCoreTask(long hwnd, Library dm, Mouse mouse, Keyboard keyboard, String version, DDTankConfigProperties properties) {
        this.hwnd = hwnd;
        this.dm = dm;
        this.keyboard = keyboard;
        this.version = version;


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
        this.properties = properties;
        this.coreState = CoreThreadStateEnum.WAITING_START;
        this.handlerSelector = new DDTankCoreHandlerSelector(keyboard, properties);
    }

    private void init() {
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
    }


    @Override
    public void run() {
        if (bind()) {
            init();
            try {
                updateMsg("脚本已启动！");
                while (!Thread.interrupted()) {
                    if (suspend) {
                        coreState = CoreThreadStateEnum.SUSPEND;
                        delay(1000, true);
                    } else {
                        try {
                            coreState = CoreThreadStateEnum.RUN;
                            if (ddtankPic.needActiveWindow()) {
                                updateMsg("窗口未被激活，已重新激活窗口");
                            }

                            if (ddtankPic.needGoingToWharf()) {
                                updateMsg("检测到处于大厅，自动进入远征码头");
                            }

                            if (ddtankPic.needCreateRoom()) {
                                updateMsg("检测到处于远征码头，自动创建房间");
                            }

                            if (ddtankPic.needChooseMap()) {
                                updateMsg("检测到未选择副本，自动选择副本：" + properties.getLevelLine() + "行" + properties.getLevelRow() + "列");
                                ddtankOperate.chooseMap();
                                // TODO 未选择上脚本
                            }

                            if (ddtankPic.needCloseTip()) {
                                updateMsg("检测到提示，已自动关闭");
                            }

                            if (ddtankPic.needClickPrepare()) {
                                updateMsg("检测到准备，点击准备按钮");
                            }

                            if (ddtankPic.needClickStart()) {
                                updateMsg("检测到开始，点击开始按钮");
                                initEveryTimes();
                            }

                            if (ddtankPic.isEnterLevel()) {
                                // 进图就测量屏距
                                if (properties.getIsCalcDistanceQuickly() && !properties.getIsHandleCalcDistance() && !properties.getIsHandleAttack() && !isCalcedDistance) {
                                    distance = ddtankPic.calcUnitDistance();
                                    isCalcedDistance = true;
                                    updateMsg("勾选了快速测量屏距：" + distance);
                                }
                                if (ddtankPic.isMyRound()) {
                                    round = round + 1;
                                    updateMsg("第" + (times + 1) + "次副本，第" + round + "回合");
                                    attack();
                                }
                            }

                            if (ddtankPic.needDraw()) {
                                times++;
                            }
                            delay(properties.getDelay(), true);
                            log.trace("脚本运行中。。。");
                        } catch (StopTaskException ignored) {
                        } catch (Exception e) {
                            e.printStackTrace();
                            log.error("脚本出现异常：{}", e.toString());
                        }
                    }
                }
            } finally {
                unBind();
            }
            updateMsg("脚本已停止");
        } else {
            updateMsg("窗口绑定失败，请重新尝试启动脚本");
            log.error("窗口绑定失败，请重新尝试启动脚本，大漠错误码：{}", dm.getLastError());
        }
        coreState = CoreThreadStateEnum.STOP;
    }

    private void initEveryTimes() {
        round = 0;
        toward = TowardEnum.UNKNOWN;
        isCalcedDistance = false;
    }

    public void updateMsg(String msg) {
        this.msg = msg;
        log.info(msg);
    }

    public String getCurrentMsg() {
        return msg;
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
                updateMsg("自动测量屏距：1距=" + distance + "px");
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
        while (needFind) {
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
                        updateMsg("检测到当前方向向左，敌人在右，已自动转向");
                    } else if (toward == TowardEnum.RIGHT && enemyPosition.getX() < myPosition.getX()) {
                        keyboard.keyPress('a');
                        updateMsg("检测到当前方向向右，敌人在左，已自动转向");
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
                    updateMsg("我的坐标：" + myPosition + ", 敌人的坐标：" + enemyPosition + ", 水平屏距：" + horizontal + ", 垂直屏距：" + vertical);
                    strength = ddtankOperate.getStrength(angle, horizontal, vertical);
                    strength += properties.getOffsetStrength();
                    angle += properties.getOffsetAngle();
                    updateMsg("自动攻击：" + angle + "度, " + strength + "力");
                    keyboard.keysPress(properties.getAttackSkill(), 10);
                    ddtankOperate.attack(strength);
                } else {
                    // TODO 调整角度失败的情况，即调整角度策略失效
                    updateMsg("未能调整到指定角度，当前角度：" + ddtankPic.getAngle() + ", 目标角度：" + angle);
                }
            }
        }
    }


    public boolean bind() {
        ComThread.InitSTA();
        if (dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
            updateMsg("绑定句柄：" + hwnd);
            delay(1000, true);
            return true;
        }
        return false;
    }

    public void unBind() {
        updateMsg(hwnd + "解除绑定");
        delay(1000, true);
        dm.unbindWindow();
        ComThread.Release();
    }
}
