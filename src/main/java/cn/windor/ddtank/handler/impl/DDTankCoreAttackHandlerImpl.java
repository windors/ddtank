package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.*;
import cn.windor.ddtank.handler.DDTankCoreAttackHandler;
import cn.windor.ddtank.handler.DDTankFindPositionMoveHandler;
import cn.windor.ddtank.type.TowardEnum;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankCoreAttackHandlerImpl implements DDTankCoreAttackHandler {

    private final DDTankConfigProperties properties;
    private final Keyboard keyboard;
    private final DDTankOperate ddtankOperate;

    private DDTankLog ddtLog;
    // 1距的距离
    protected Double distance;
    protected Point myPosition, enemyPosition, myLastPosition, enemyLastPosition;
    protected Integer angle;

    protected Double strength;

    // 回合数
    int round;
    // 朝向
    TowardEnum toward;
    protected DDTankPic ddtankPic;
    DDTankCoreHandlerSelector handlerSelector;

    public DDTankCoreAttackHandlerImpl(DDTankConfigProperties properties, Keyboard keyboard, DDTankPic ddtankPic, DDTankOperate ddtankOperate, DDTankLog ddtLog) {
        this.properties = properties;
        this.keyboard = keyboard;
        this.ddtankPic = ddtankPic;
        this.ddtankOperate = ddtankOperate;
        this.ddtLog = ddtLog;
        myPosition = new Point();
        enemyPosition = new Point();
        myLastPosition = new Point();
        enemyLastPosition = new Point();
        handlerSelector = new DDTankCoreHandlerSelector(keyboard, properties);
    }

    @Override
    public void main() {
        DistanceCalculate distanceCalculate = new DistanceCalculate();
        while (ddtankPic.isEnterLevel()) {
            // 计算屏距
            int failTimes = 0;
            while (!distanceCalculate.calc()) {
                delay(10, true);
                if (++failTimes > 3) {
                    break;
                }
            }

            if (ddtankPic.isMyRound()) {
                round = round + 1;
                // 获取屏距
                distance = distanceCalculate.get();
                log("第" + round + "回合");
                // TODO 自定义回合技能
                String skill = properties.getAttackSkill().toLowerCase();
                if (skill.contains("p")) {
                    keyboard.keysPress(skill);
                } else if (properties.getIsHandleAttack()) {
                    angle = properties.getHandleAngle();
                    strength = properties.getHandleStrength();
                    if (ddtankOperate.angleAdjust(angle, handlerSelector.getAngleMoveHandler(), toward)) {
                        log("手动攻击：" + angle + "度, " + strength + "力");
                        keyboard.keysPress(properties.getAttackSkill(), 10);
                        ddtankOperate.attack(strength);
                    } else {
                        log("未能调整到指定角度，当前角度：" + ddtankPic.getAngle() + ", 目标角度：" + angle);
                    }
                } else {
                    // 自动攻击
                    attackAuto();
                }
            }
            // 进入副本后延迟为外面的 1/10
            delay(properties.getDelay() / 10, true);
        }
    }

    @Override
    public void reset() {
        round = 0;
        toward = TowardEnum.UNKNOWN;
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
                log("未找到位置，尝试走位。若长时间未找到分析是否是小地图截取太小的问题。");
                DDTankFindPositionMoveHandler positionMoveHandler = handlerSelector.getPositionMoveHandler();
                if (!positionMoveHandler.move(++tiredTimes)) {
                    break;
                }
            }
        }
        if (myPosition != null) {
            log("我的坐标：" + myPosition.getX() + ", " + myPosition.getY());
            myLastPosition.setX(myPosition.getX());
            myLastPosition.setY(myPosition.getY());
            enemyPosition = ddtankPic.getEnemyPosition();
            if (enemyPosition == null) {
                if (enemyLastPosition.getX() != 0 && enemyLastPosition.getY() != 0) {
                    // 当前记录过敌人的位置，但这回合找不到了
                    enemyPosition = new Point(enemyLastPosition.getX() + 30, enemyLastPosition.getY());
                    if (enemyPosition.getX() >= 1000) {
                        enemyPosition.setX(1000);
                    }
                } else {
                    // 第一回合就找不到boss，尝试不断的走位来获取boss位置
                    while (ddtankPic.isMyRound()) {
                        if ((enemyPosition = ddtankPic.getEnemyPosition()) != null) {
                            break;
                        }
                        DDTankFindPositionMoveHandler positionMoveHandler = handlerSelector.getPositionMoveHandler();
                        if (!positionMoveHandler.move(++tiredTimes)) {
                            break;
                        }
                    }
                }
            }
            log("敌人的坐标：" + enemyPosition.getX() + ", " + enemyPosition.getY());
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
                    log("检测到当前方向向左，敌人在右，已自动转向");
                    delay(100, true);
                } else if (toward == TowardEnum.RIGHT && enemyPosition.getX() < myPosition.getX()) {
                    keyboard.keyPress('a');
                    log("检测到当前方向向右，敌人在左，已自动转向");
                    delay(100, true);
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
                double horizontal = new BigDecimal((enemyPosition.getX() - myPosition.getX()) / distance).setScale(2, RoundingMode.UP).doubleValue();
                double vertical = new BigDecimal((myPosition.getY() - enemyPosition.getY()) / distance).setScale(2, RoundingMode.UP).doubleValue();
                log("水平屏距：" + horizontal + ", 垂直屏距：" + vertical);
                strength = ddtankOperate.getStrength(angle, horizontal, vertical);
                strength += properties.getOffsetStrength();
                strength = new BigDecimal(strength).setScale(2, RoundingMode.UP).doubleValue();
                log("自动攻击：" + angle + "度, " + strength + "力");
                keyboard.keysPress(properties.getAttackSkill(), 0);
                ddtankOperate.attack(strength);
            } else {
                // TODO 调整角度失败的情况，即调整角度策略失效
                log("未能调整到指定角度，当前角度：" + ddtankPic.getAngle() + ", 目标角度：" + angle);
            }
        }
    }

    private boolean getMyPosition() {
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

        return true;
    }

    class DistanceCalculate {
        private List<Double> distanceList = new ArrayList<>();

        // 在首次调用get时会停止检测屏距。
        private boolean stop;

        private double finalDistance;

        public boolean calc() {
            if (stop) {
                return true;
            }
            double distance = ddtankPic.calcUnitDistance();
            if (distance > 0) {
                distanceList.add(distance);
                return true;
            }
            return false;
        }

        public double get() {
            // 手动屏距直接返回即可
            if (properties.getIsHandleCalcDistance()) {
                log("手动屏距：" + properties.getHandleDistance());
                return properties.getHandleDistance();
            }

            // 如果已经成功获取过一次了，直接返回即可
            if (stop) {
                return finalDistance;
            }
            stop = true;
            if (distanceList.size() < 1) {
                // 未检测到有效屏距
                stop = false;
                log.error("未检测到有效屏距");
                return 10.0;
            }
            // 统计
            Collections.sort(distanceList);
            double times = 1;
            double maxTimes = 1;
            double nowDistance = distanceList.get(0);
            for (Double distance : distanceList) {
                if (distance == nowDistance) {
                    times++;
                    if (times > maxTimes) {
                        finalDistance = distance;
                        maxTimes = times;
                    }
                } else {
                    nowDistance = distance;
                    times = 1;
                }
            }
            // 释放内存，之后都不需要用到了
            distanceList = null;
            log("自动屏距：" + finalDistance);
            // 统计出现次数最多的屏距
            return finalDistance;
        }
    }




    public void log(String msg) {
        ddtLog.log(msg);
        log.debug(msg);
    }

    public void logInfo(String msg) {
        ddtLog.log(msg);
        log.info(msg);
    }

    public void logWarn(String msg) {
        ddtLog.log(msg);
        log.warn(msg);
    }

    public void logError(String msg) {
        ddtLog.log(msg);
        log.error(msg);
    }
}
