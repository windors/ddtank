package cn.windor.ddtank.core;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.ddtank.type.TowardEnum;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j

public class DDTankCoreThread extends Thread {

    // 游戏句柄
    protected long hwnd;

    // 回合数
    protected int round;

    // 朝向
    protected TowardEnum toward;

    @Getter
    // 已通关副本数
    protected int times;

    // 1距的距离
    protected Double distance;

    protected Point myPosition, enemyPosition, myLastPosition, enemyLastPosition;

    protected int angle;

    protected double strength;

    private boolean isCalcedDistance;

    @Getter
    private boolean suspend;

    @Getter
    private CoreThreadStateEnum coreState;

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
    protected DDTankAngleAdjustMove angleAdjust;

    @Getter
    private final DDTankDaemonThread daemonThread;

    public DDTankCoreThread(long hwnd, Library dm, DDTankPic ddtankPic, DDTankOperate ddtankOperate, DDTankConfigProperties properties, DDTankAngleAdjustMove angleAdjust) {
        this.hwnd = hwnd;
        this.dm = dm;
        this.ddtankPic = ddtankPic;
        this.ddtankOperate = ddtankOperate;
        this.properties = properties;
        this.angleAdjust = angleAdjust;
        this.coreState = CoreThreadStateEnum.WAITING_START;
        daemonThread = new DDTankDaemonThread(this, hwnd, dm, properties);
    }

    private void init() {
        // 设置大漠字库
        if (!dm.setDict(0, "C:/tmp/ddtankLibrary10.4.txt")) {
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
                updateMsg("脚本初始化成功！");
                while (!Thread.currentThread().isInterrupted()) {
                    if (suspend) {
                        coreState = CoreThreadStateEnum.SUSPEND;
                        delay(1000);
                    } else {
                        coreState = CoreThreadStateEnum.RUN;
                        try {
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

                            if (ddtankPic.needClickStart()) {
                                updateMsg("检测到开始，点击开始按钮");
                                initEveryTimes();
                            }

                            if (ddtankPic.needCloseTip()) {
                                updateMsg("检测到提示，已自动关闭");
                            }

                            if (ddtankPic.isEnterLevel()) {
                                // 进图就测量屏距
                                if (properties.getIsCalcDistanceQuickly() && !properties.getIsHandleCalcDistance() && !isCalcedDistance) {
                                    distance = ddtankOperate.calcUnitDistance();
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

                            sleep(properties.getDelay());
                            log.debug("脚本运行中。。。");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } finally {
                unBind();
            }
            updateMsg("脚本已停止");
        } else {
            log.error("窗口绑定失败，请重新尝试启动脚本，大漠错误码：{}", dm.getLastError());
        }
        coreState = CoreThreadStateEnum.STOP;
    }

    private void initEveryTimes() {
        round = 0;
        toward = TowardEnum.UNKNOWN;
        isCalcedDistance = false;
    }

    protected void updateMsg(String msg) {
        this.msg = msg;
        log.info(msg);
    }

    public String getCurrentMsg() {
        return msg;
    }

    private void attack() {
        // 屏距测量
        String skill = properties.getAttackSkill().toLowerCase();
        if (skill.contains("p")) {
            keyboard.keysPress(skill);
        } else {
            if (!properties.getIsHandleCalcDistance() && !isCalcedDistance) {
                // 需要自动计算屏距
                distance = ddtankOperate.calcUnitDistance();
                isCalcedDistance = true;
                updateMsg("自动测量屏距：1距=" + distance + "px");
            } else {
                distance = properties.getHandleDistance();
            }
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

    private void attackAuto() {
        if ((myPosition = ddtankPic.getMyPosition()) != null) {
            myLastPosition.setX(myPosition.getX());
            myLastPosition.setY(myPosition.getY());
            if ((enemyPosition = ddtankPic.getEnemyPosition()) != null) {
                enemyLastPosition.setX(enemyPosition.getX());
                enemyLastPosition.setY(enemyPosition.getY());
                angle = ddtankOperate.getBestAngle(myPosition, enemyPosition);
                if(properties.getAttackTurn()) {
                    // 开启了攻击转向
                    if(enemyPosition.getX() > myPosition.getX()) {
                        // 敌人在右
                        keyboard.keyPress('a');
                        keyboard.keyPress('d');
                        toward = TowardEnum.RIGHT;
                    }else{
                        keyboard.keyPress('d');
                        keyboard.keyPress('a');
                        toward = TowardEnum.LEFT;
                    }
                    delay(300);
                }else{
                    toward = ddtankPic.getToward();
                    if(toward == TowardEnum.LEFT && enemyPosition.getX() > myPosition.getX()) {
                        keyboard.keyPress('d');
                        updateMsg("检测到当前方向向左，敌人在右，已自动转向");
                    }else if(toward == TowardEnum.RIGHT && enemyPosition.getX() < myPosition.getX()) {
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
                if (ddtankOperate.angleAdjust(angle, angleAdjust, targetToward)) {
                    double horizontal = (enemyPosition.getX() - myPosition.getX()) / distance;
                    double vertical = (myPosition.getY() - enemyPosition.getY()) / distance;
                    updateMsg("我的坐标：" + myPosition + ", 敌人的坐标：" + enemyPosition + ", 水平屏距：" + horizontal + ", 垂直屏距：" + vertical);
                    strength = ddtankOperate.getStrength(angle, horizontal, vertical);
                    updateMsg("自动攻击：" + angle + "度, " + strength + "力");
                    ddtankOperate.attack(strength);
                } else {
                    // TODO 调整角度失败的情况，即调整角度策略失效

                }
            }
        }
    }

    public void updateProperties(DDTankConfigProperties properties) {
        updateMsg("更新了[" + this.getName() + "]的配置文件");
        this.properties.update(properties);
    }

    public void sendStop() {
        coreState = CoreThreadStateEnum.WAITING_STOP;
        this.interrupt();
    }

    public void sendSuspend() {
        if (!this.suspend) {
            coreState = CoreThreadStateEnum.WAITING_SUSPEND;
            updateMsg("暂停运行");
            this.suspend = true;
        }
    }

    public void sendContinue() {
        if (suspend) {
            coreState = CoreThreadStateEnum.WAITING_CONTINUE;
            updateMsg("恢复运行");
            this.suspend = false;
        }
    }


    private boolean bind = false;
    private long lastBindTime = 0;

    public boolean bind() {
        if (!bind && dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
            updateMsg("绑定句柄：" + hwnd);
            bind = true;
            lastBindTime = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public void unBind() {
        if (bind) {
            updateMsg(hwnd + "解除绑定");
            long time = System.currentTimeMillis() - lastBindTime;
            if (time < 1000) {
                delay(time);
            }else {
                /*防止正在调用大漠的插件，立刻停止会导致出现窗口刷新问题*/
                delay(300);
            }
            dm.unbindWindow();
        }
    }
}