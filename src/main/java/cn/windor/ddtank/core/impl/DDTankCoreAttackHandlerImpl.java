package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.*;
import cn.windor.ddtank.exception.DDTankAngleResolveException;
import cn.windor.ddtank.core.DDTankCoreAttackHandler;
import cn.windor.ddtank.handler.DDTankFindPositionMoveHandler;
import cn.windor.ddtank.type.TowardEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.*;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankCoreAttackHandlerImpl implements DDTankCoreAttackHandler {

    private final DDTankConfigProperties properties;
    private Keyboard keyboard;
    private DDTankOperate ddtankOperate;

    private DDTankLog ddtLog;
    // 1距的距离
    protected Double distance;
    protected Point myPosition, enemyPosition, myLastPosition, enemyLastPosition;
    protected Integer angle;

    protected Double strength;

    // 攻击缓存
    @Getter
    @Setter
    private static Map<String, Double> calcedMap = new ConcurrentHashMap<>();

    boolean exit;

    // 回合数
    int round;
    // 朝向
    TowardEnum toward;
    protected DDTankPic ddtankPic;
    DDTankCoreHandlerSelector handlerSelector;

    private final static ExecutorService calcStrengthExecutors = Executors.newCachedThreadPool();

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
    public boolean update(Object... complexObject) {
        boolean success = true;
        for (Object param : complexObject) {
            if (param instanceof Keyboard) {
                this.keyboard = (Keyboard) param;
                continue;
            }
            if (param instanceof DDTankPic) {
                this.ddtankPic = (DDTankPic) param;
                continue;
            }
            if (param instanceof DDTankOperate) {
                this.ddtankOperate = (DDTankOperate) param;
                continue;
            }
            if (param instanceof DDTankLog) {
                this.ddtLog = (DDTankLog) param;
                continue;
            }
            success = false;
        }
        if (success) {
            handlerSelector = new DDTankCoreHandlerSelector(keyboard, properties);
        }
        return success;
    }

    @Override
    public long main() {
        long failTimes = 0;
        // 屏距计算器
        DistanceCalculate distanceCalculate = new DistanceCalculate();
        while (!exit && ddtankPic.isEnterLevel()) {
            // 计算当前屏距
            while (!distanceCalculate.calc()) {
                delay(10, true);
                if (++failTimes > 3) {
                    break;
                }
            }
            updateLastEnemyPosition();

            if (ddtankPic.isMyRound()) {
                round = round + 1;
                // 获取屏距
                distance = distanceCalculate.get();
                log.debug("第{}回合", round);
                ddtLog.primary("第" + round + "回合");
                // TODO 自定义回合技能
                String skill = properties.getAttackSkill().toLowerCase();
                if (skill.contains("p")) {
                    // 如果技能带p，那么直接按技能就ok
                    keyboard.keysPress(skill);
                }
                // 手动攻击，暂停停止
/*                else if (properties.getIsHandleAttack()) {
                    angle = properties.getHandleAngle();
                    strength = properties.getHandleStrength();
                    if (ddtankOperate.angleAdjust(angle, handlerSelector.getAngleMoveHandler(), toward)) {
                        log.debug("手动攻击：" + angle + "度, " + strength + "力");
                        ddtLog.info("手动攻击：" + angle + "度, " + strength + "力");
                        keyboard.keysPress(properties.getAttackSkill(), 10);
                        ddtankOperate.attack(strength);
                    } else {
                        log.debug("未能调整到指定角度，当前角度：{}, 目标角度：{}", ddtankPic.getAngle(), angle);
                        ddtLog.warn("未能调整到指定角度，当前角度：" + ddtankPic.getAngle() + ", 目标角度：" + angle);
                    }
                }*/
                else {
                    // 自动攻击
                    attackAuto();
                }
            } else {
                // 未到自己回合，预调整角度
                preAngleAdjust();
            }
            // 进入副本后延迟为外面的 1/10
            delay(properties.getDelay() / 10, true);
            ++failTimes;
        }
        // 下次再调用时仍然能继续调用
        exit = false;
        return failTimes;
    }

    /**
     * 在未到自己回合时会调用该函数以计算敌人位置，用于记录敌人的最后位置
     */
    private void updateLastEnemyPosition() {
        Point enemyPosition = ddtankPic.getEnemyPosition();
        if (enemyPosition != null) {
            enemyLastPosition = enemyPosition;
        }
    }

    /**
     * 在未到自己回合时会调用该函数以提前调整角度，便于快速出手
     */
    private void preAngleAdjust() {
        try {
            if (properties.getIsHandleAttack()) {
                // 判断是否手动攻击
                ddtankOperate.angleAdjust(properties.getHandleAngle());
            } else if (properties.getIsFixedAngle()) {
                // 判断是否是固定角度攻击
                ddtankOperate.angleAdjust(properties.getFixedAngle());
            } else if (properties.getIsClosestAngle()) {
                // TODO 判断是否是临近角度攻击
            } else {
                if (angle != null) {
                    ddtankOperate.angleAdjust(angle);
                } else {
                    ddtankOperate.angleAdjust(20);
                }
            }
        } catch (DDTankAngleResolveException ignore) {
        }
    }

    @Override
    public void suspend() {
        exit = true;
    }

    @Override
    public void reset() {
        angle = null;
        round = 0;
        toward = TowardEnum.UNKNOWN;
        enemyPosition = new Point();
        enemyLastPosition = new Point();
        myPosition = new Point();
        myLastPosition = new Point();
    }

    /**
     * 判断点是否为空点
     */
    private boolean isEmpty(Point point) {
        return point == null || point.getX() == 0 || point.getY() == 0;
    }

    private void attackAuto() {
        if (!findMyPosition(handlerSelector.getPositionMoveHandler()) || !findEnemyPosition(handlerSelector.getPositionMoveHandler())) {
            // 一开始就未找到位置则退出回合
            return;
        }
        towardCheck();

        // 调整角度
        angle = ddtankOperate.getBestAngle(myPosition, enemyPosition);
        if (!angleAdjust(angle)) {
            // 角度调整失败，则退出回合
            // 角度获取失败，跳过回合
            ddtLog.error("角度获取失败，跳过此回合");
            keyboard.keyPress('p');
            return;
        }

        double horizontal = new BigDecimal((enemyPosition.getX() - myPosition.getX()) / distance).setScale(2, RoundingMode.UP).doubleValue();
        double vertical = new BigDecimal((myPosition.getY() - enemyPosition.getY()) / distance).setScale(2, RoundingMode.UP).doubleValue();
        double wind = ddtankPic.getWind();
        if (toward == TowardEnum.LEFT) {
            // 当前头朝左，此时风向需要取反
            wind = -wind;
        }
        log.debug("水平屏距：{}, 垂直屏距：{}, 风力：{}", horizontal, vertical, wind);
        ddtLog.info("水平屏距：" + horizontal + ", 垂直屏距：" + vertical);
        ddtLog.info("风力：" + wind);
        // 开辟线程去异步计算力度（大约要花1.5s）
        Future<Double> calcStrengthTask = calcStrengthExecutors.submit(new CalcStrengthTask(angle, wind, horizontal, vertical));

        // 预先按下空格
        boolean get = false;
        try {
            // 等待指定时间，若指定时间内执行完毕，则会直接向下运行，跳过等待
            strength = calcStrengthTask.get((long) (properties.getAttackDelay() * 1000), TimeUnit.MILLISECONDS);
            get = true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException ignore) {
        }
        keyboard.keyDown(' ');
        long start = System.currentTimeMillis();

        // 如果指定时间内未获取到力度，则继续等待力度计算，如果已经获取到了力度则会跳过
        if (!get) {
            try {
                strength = calcStrengthTask.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        strength += properties.getOffsetStrength();
        strength = new BigDecimal(strength).setScale(2, RoundingMode.UP).doubleValue();


        log.debug("自动攻击：{}度, {}力", angle, strength);
        ddtLog.info("自动攻击：" + angle + "度, " + strength + "力");
        keyboard.keysPress(properties.getAttackSkill(), 0);
        if(System.currentTimeMillis() - start < 200) {
            // 按下空格后延迟一小段时间
            delay(200 - (System.currentTimeMillis() - start), true);
        }
        ddtankOperate.attack(strength);
    }

    /**
     * @param angle
     * @return 角度是否调整成功
     */
    private boolean angleAdjust(int angle) {
        TowardEnum targetToward;
        if (myPosition.getX() < enemyPosition.getX()) {
            targetToward = TowardEnum.RIGHT;
        } else {
            targetToward = TowardEnum.LEFT;
        }
        try {
            int tried = 1;
            while (!ddtankOperate.angleAdjust(angle) && handlerSelector.getAngleMoveHandler().move(targetToward, angle, tried++)) {
                if (tried++ % 3 == 0 && !ddtankPic.isMyRound()) {
                    // TODO 调整角度失败的情况，即调整角度策略失效
                    log.debug("未能调整到指定角度，当前角度：{}, 目标角度: {}, 执行原地飞操作", ddtankPic.getAngle(), angle);
                    ddtLog.info("未能调整到指定角度，当前角度：" + ddtankPic.getAngle() + ", 目标角度：" + angle + "执行原地飞操作");
                    keyboard.keyPress('f');
                    ddtankOperate.attack(5);
                    return false;
                }
            }
            if (tried > 1) {
                // 需要重新获取我的位置
                ddtLog.primary("重新获取我的位置");
                findMyPosition(null);
            }
        } catch (DDTankAngleResolveException e) {
            return false;
        }
        return true;
    }

    private void towardCheck() {
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
                log.debug("检测到当前方向向左，敌人在右，已自动转向");
                ddtLog.primary("检测到当前方向向左，敌人在右，已自动转向");
                delay(100, true);
            } else if (toward == TowardEnum.RIGHT && enemyPosition.getX() < myPosition.getX()) {
                keyboard.keyPress('a');
                log.debug("检测到当前方向向右，敌人在左，已自动转向");
                ddtLog.primary("检测到当前方向向右，敌人在左，已自动转向");
                delay(100, true);
            }
        }
    }

    /**
     * 寻找敌人位置，并将敌人位置保存在enemyPosition成员变量中
     *
     * @param positionMoveHandler 当找不到敌人时应该如何操作
     * @return
     */
    private boolean findEnemyPosition(DDTankFindPositionMoveHandler positionMoveHandler) {
        Point enemyPosition = ddtankPic.getEnemyPosition();
        if (isEmpty(enemyPosition)) {
            if (!isEmpty(enemyLastPosition)) {
                ddtLog.warn("未找到敌人，即将使用敌人的最后坐标");
                this.enemyPosition = enemyLastPosition;
                return true;
            } else {
                // 从开局到最终就找不到boss，尝试不断的走位来获取boss位置
                int tiredTimes = 0;
                while (ddtankPic.isMyRound()) {
                    if ((enemyPosition = ddtankPic.getEnemyPosition()) != null) {
                        break;
                    }
                    ++tiredTimes;
                    if (positionMoveHandler != null) {
                        if (!positionMoveHandler.move(tiredTimes)) {
                            break;
                        }
                    } else {
                        if (tiredTimes > 100) {
                            ddtLog.error("未找到敌人位置");
                            return false;
                        }
                    }
                }
            }
        }
        // 如果找到了敌人位置，则更新敌人最后坐标
        if (!isEmpty(enemyPosition)) {
            this.enemyPosition = enemyPosition;
            enemyLastPosition.setX(enemyPosition.getX());
            enemyLastPosition.setY(enemyPosition.getY());
            log.debug("敌人的坐标：{}, {}", enemyPosition.getX(), enemyPosition.getY());
            ddtLog.info("敌人的坐标：" + enemyPosition.getX() + ", " + enemyPosition.getY());
            return true;
        }
        ddtLog.warn("未找到敌人位置");
        return false;
    }

    private boolean findMyPosition(DDTankFindPositionMoveHandler positionMoveHandler) {
        int tiredTimes = 0;
        boolean needFind = true;
        Point myPosition = null;
        while (needFind && ddtankPic.isMyRound()) {
            for (int i = 0; i < 10; i++) {
                if ((myPosition = ddtankPic.getMyPosition()) != null) {
                    // 如果找到了位置则退出循环
                    needFind = false;
                    break;
                }
            }
            // 如果10次仍然没有找到位置
            if (myPosition == null) {
                tiredTimes++;
//                ddtLog.warn("未找到位置，尝试走位。");
                if (positionMoveHandler != null) {
                    if (!positionMoveHandler.move(tiredTimes)) {
                        break;
                    }
                } else {
                    if (tiredTimes > 100) {
                        ddtLog.error("未找到走位后的位置");
                        return false;
                    }
                }
            }
        }
        if (myPosition != null) {
            this.myPosition = myPosition;
            log.debug("我的坐标：{}, {}", myPosition.getX(), myPosition.getY());
            ddtLog.info("我的坐标：" + myPosition.getX() + ", " + myPosition.getY());
            return true;
        }
        log.warn("未找到我的位置");
        return false;
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
            try {
                double distance = ddtankPic.calcUnitDistance();
                if (distance > 0) {
                    distanceList.add(distance);
                    return true;
                }
            } catch (Exception e) {
                log.info("请更新屏距算法，当前调用出现{}异常。（下个版本解决，目前不影响使用）", e.getClass());
                return false;
            }
            return false;
        }

        public double get() {
            // 手动屏距直接返回即可
            if (properties.getIsHandleCalcDistance()) {
                log.debug("手动屏距：{}", properties.getHandleDistance());
                ddtLog.info("手动屏距：" + properties.getHandleDistance());
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
                ddtLog.warn("未能检测到有效屏距");
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
            log.debug("自动屏距：{}", finalDistance);
            ddtLog.info("自动屏距：" + finalDistance);
            // 统计出现次数最多的屏距
            return finalDistance;
        }
    }

    class CalcStrengthTask implements Callable<Double> {

        private final double horizontal;

        private final double vertical;

        private final int angle;

        private final double wind;


        public CalcStrengthTask(int angle, double wind, double horizontal, double vertical) {
            this.angle = angle;
            this.wind = wind;
            this.horizontal = horizontal;
            this.vertical = vertical;
        }

        @Override
        public Double call() throws Exception {
            String key = angle + "|" + wind + "|" + horizontal + "|" + vertical;
            if (calcedMap.get(key) == null) {
                double strength = ddtankOperate.getStrength(angle, wind, horizontal, vertical);
                calcedMap.put(key, strength);
                log.info("缓存{}, {}", key, strength);
                return strength;
            }
            return calcedMap.get(key);
        }
    }
}
