package cn.windor.ddtank.core;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DDtankConfigProperties;
import cn.windor.ddtank.type.TowardEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDtankCoreThread extends Thread {
    private static int i = 1;

    // 游戏句柄
    protected long hwnd;

    // 回合数
    protected int round;

    // 朝向
    protected TowardEnum toward;

    // 已通关副本数
    protected int times;

    // 1距的距离
    protected Double distance;

    protected Point myPosition, enemyPosition, myLastPosition, enemyLastPosition;

    protected int angle;

    protected double strength;

    private boolean isCalcedDistance;

    // 当前状态消息，给外部提供查看接口
    // TODO 将msg设置为一个定长集合，便于查看历史消息
    protected String msg;


    protected Library dm;
    protected DDTankPic dmPic;
    protected DDTankOperate ddtank;
    protected Keyboard keyboard;
    @Getter
    @Setter
    protected volatile DDtankConfigProperties properties;
    protected DDTankAngleAdjust angleAdjust;


    public DDtankCoreThread(long hwnd, Library dm, DDTankPic dmPic, DDTankOperate ddtank, DDtankConfigProperties properties, DDTankAngleAdjust angleAdjust) {
        this.hwnd = hwnd;
        this.dm = dm;
        this.dmPic = dmPic;
        this.ddtank = ddtank;
        this.properties = properties;
        this.angleAdjust = angleAdjust;
        this.setName("脚本" + i++);
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
        if (dm.bindWindowEx(hwnd, properties.getBindDisplay(), properties.getBindMouse(), properties.getBindKeypad(), properties.getBindPublic(), properties.getBindMode())) {
            init();
            try {
                if (false) {
                    // 测试内容
                } else {
                    while (true) {
                        try {
                            if (Thread.currentThread().isInterrupted()) {
                                break;
                            }

                            if (dmPic.needActiveWindow()) {
                                updateMsg("窗口未被激活，已重新激活窗口");
                            }

                            if (dmPic.needGoingToWharf()) {
                                updateMsg("检测到处于大厅，自动进入远征码头");
                            }

                            if (dmPic.needCreateRoom()) {
                                updateMsg("检测到处于远征码头，自动创建房间");
                            }

                            if (dmPic.needChooseMap()) {
                                updateMsg("检测到未选择副本，自动选择副本：核心参数：" + properties.getLevelLine() + "行" + properties.getLevelRow() + "列");
                                ddtank.chooseMap();
                                // TODO 未选择上脚本
                            }

                            if (dmPic.needClickStart()) {
                                updateMsg("检测到开始，点击开始按钮");
                                initEveryTimes();
                            }

                            if (dmPic.needCloseTip()) {
                                updateMsg("检测到提示，已自动关闭");
                            }

                            if (dmPic.isEnterLevel()) {
                                // 进图就测量屏距
                                if (properties.getIsCalcDistanceQuickly() && !properties.getIsHandleCalcDistance() &&
                                        !isCalcedDistance) {
                                    distance = ddtank.calcUnitDistance();
                                    isCalcedDistance = true;
                                    updateMsg("勾选了快速测量屏距：" + distance);
                                }
                                if (dmPic.isMyRound()) {
                                    round = round + 1;
                                    updateMsg("第" + (times + 1) + "次副本，第" + round + "回合");
                                    attack();
                                }
                            }

                            if(dmPic.needDraw(properties.getIsThirdDraw())) {

                            }

                            sleep(1000);
                            log.info("脚本运行中。。。");
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            } finally {
                dm.unbindWindow();
            }

            log.info("脚本已停止");
        } else {
            log.error("窗口绑定失败，请重新尝试启动脚本，大漠错误码：{}", dm.getLastError());
        }
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

    private void attack() {
        // 屏距测量
        String skill = properties.getAttackSkill().toLowerCase();
        if (skill.contains("p")) {
            keyboard.keysPress(skill);
        } else {
            if (!properties.getIsHandleCalcDistance() && !isCalcedDistance) {
                // 需要自动计算屏距
                distance = ddtank.calcUnitDistance();
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
            ddtank.angleAdjust(angle);
            ddtank.attack(strength);
        } else {
            attackAuto();
        }
    }

    private void attackAuto() {
        if ((myPosition = ddtank.getMyPosition()) != null) {
            myLastPosition.setX(myPosition.getX());
            myLastPosition.setY(myPosition.getY());
            if ((enemyPosition = ddtank.getEnemyPosition()) != null) {
                enemyLastPosition.setX(enemyPosition.getX());
                enemyLastPosition.setY(enemyPosition.getY());
                angle = ddtank.getBestAngle(myPosition, enemyPosition);

                // 走位并调整角度
                TowardEnum targetToward;
                if (myPosition.getX() < enemyPosition.getX()) {
                    targetToward = TowardEnum.RIGHT;
                } else {
                    targetToward = TowardEnum.LEFT;
                }

                if (ddtank.angleAdjust(angle, angleAdjust, TowardEnum.RIGHT)) {
                    double horizontal = (enemyPosition.getX() - myPosition.getX()) / distance;
                    double vertical = (myPosition.getY() - enemyPosition.getY()) / distance;
                    updateMsg("我的坐标：" + myPosition + ", 敌人的坐标：" + enemyPosition +  ", 水平屏距：" + horizontal + ", 垂直屏距：" + vertical);
                    strength = ddtank.getStrength(angle, horizontal, vertical);
                    updateMsg("自动攻击：" + angle + "度, " + strength + "力");
                    ddtank.attack(strength);
                } else {
                    // TODO 调整角度失败的情况，（走到了地图边缘、地形被卡住、需要飞）
                }
            }
        }
    }
}
