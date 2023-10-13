package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankAngleAdjust;
import cn.windor.ddtank.core.DDTankOperate;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.type.TowardEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDtankOperate10_4 implements DDTankOperate {
    private static final double[] strengthTable20 = {10, 19, 25, 30, 36, 40, 44, 48, 51, 54, 57, 60, 63, 66, 69, 72, 74, 76, 78, 80};
    private static final double[] strengthTable30 = {14, 20, 24, 28, 32, 35, 38, 41, 44, 47, 50, 52, 55, 57, 60, 62, 65, 67, 69, 72};
    private static final double[] strengthTable35 = {10, 16, 22, 26, 30, 33, 37, 40, 43, 45, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67};
    private static final double[] strengthTable40 = {12, 16, 20, 25, 30, 33, 35, 40, 43, 45, 48, 50, 53, 55, 58, 60, 62, 64, 67, 70};
    private static final double[] strengthTable45 = {13, 16, 20, 25, 30, 33, 35, 38, 41, 45, 48, 51, 53, 55, 57, 59, 60, 63, 66, 68};
    private static final double[] strengthTable50 = {14, 20, 24, 28, 32, 35, 38, 42, 44, 48, 50, 53, 55, 58, 60, 63, 65, 68, 70, 72};
    private static final double[] strengthTable65 = {13, 20, 26, 31, 37, 41, 44, 48, 53, 56, 58, 61, 64, 67, 70, 73, 76, 79, 82, 85};
    private static final double[] strengthTable70 = {18.5, 26.4, 32.6, 37.9, 42.7, 47.2, 51.3, 55.3, 59.1, 62.8, 66.3, 69.8, 73.1, 76.5, 79.7, 82.9, 86.1, 89.2, 92.3, 95.3};
    private static final double[] upOffset = {3, 2.425, 2, 1.55, 1.9, 1.15, 0.58, 0.24};
    private Mouse mouse;

    private Keyboard keyboard;

    private DDTankPic ddTankPic;

    private DDTankConfigProperties properties;

    private Library dm;

    public DDtankOperate10_4(Library dm, Mouse mouse, Keyboard keyboard, DDTankPic ddTankPic, DDTankConfigProperties properties) {
        this.dm = dm;
        this.mouse = mouse;
        this.keyboard = keyboard;
        this.ddTankPic = ddTankPic;
        this.properties = properties;
    }

    /**
     * 选副本策略
     */
    @Override
    public void chooseMap() {
        int x = -1, y = -1;
        int line = properties.getLevelLine();
        int row = properties.getLevelRow();
        // 副本模式：普通副本、门票副本、元素副本、活动副本、平衡副本等
        mouse.moveAndClick(221 + 525 * (properties.getLevelMode() / 100), 190);

        delay(800);
        switch (line % 2) {
            case 1:
                y = 230;
                break;
            case 0:
                y = 290;
                break;
        }

        // 对于需要翻页的副本将那些的操作，该端的翻页逻辑是点两下滑块，再点一下按钮
        if (line > 2) {
            for (int i = 0; i < (line / 2); i++) {
                for (int j = 0; j < 2; j++) {
                    mouse.moveTo(775, 300);
                    mouse.leftDown();
                    mouse.leftUp();
                    delay(100);
                    mouse.leftDown();
                    mouse.leftUp();
                    delay(100);
                    mouse.moveTo(775, 310);
                    mouse.leftDown();
                    mouse.leftUp();
                    delay(100);
                }
            }
        }

        switch (row % 4) {
            case 1:
                x = 280;
                break;
            case 2:
                x = 420;
                break;
            case 3:
                x = 560;
                break;
            case 0:
                x = 700;
                break;
        }

        log.debug("选择地图最终坐标: {}, {}", x, y);
        mouse.moveAndClick(x, y);
        delay(100);

        // 选择难度
        mouse.moveAndClick(224 + 554 * (properties.getLevelDifficulty() / 100), 500);

        // 点击确定
        mouse.moveAndClick(480, 560);
        delay(300);
    }


    @Override
    public double calcUnitDistance() {
        int count = 0;
        Point result = new Point();
        int[] arr = new int[5];
        // 计算多次，将结果存入arr中，最终取平均值
        for (int i = 0; i < arr.length; i++) {
            arr[i] = -1;
            // 多次检测防止未检测到屏字
            for (int j = 0; j < 5; j++) {
                if (dm.findStr(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                        "屏", "999999-000000", 1, result)) {
                    int x = result.getX(), y = result.getY();
                    count = 1;
                    while (x < properties.getStaticX2() && dm.findColor(x, y, properties.getStaticX2(), y + 1, "999999-000000", 1, 0, null)) {
                        count++;
                        x++;
                    }
                    arr[i] = count;
                    break;
                }
            }
        }
        Arrays.sort(arr);

        // 选择出出现元素最多的计算结果
        int times = 0, nowDistance = -1, maxTimes = -1, maxDistance = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == -1) {
                continue;
            }
            if (arr[i] == nowDistance) {
                times++;
            } else {
                times = 1;
                nowDistance = arr[i];
            }
            if (times > maxTimes) {
                maxTimes = times;
                maxDistance = nowDistance;
            }
        }
        if (maxDistance == -1) {
            log.error("屏距自动检测失败！本轮副本使用使用手动屏距！");
            return properties.getHandleDistance();
        }

        return (double) maxDistance / 10;
    }

    // TODO 未识别到数字的情况
    @Override
    public boolean angleAdjust(int targetAngle) {
        int failCount = 0, lastAngle, nowAngle;
        int angleMis = properties.getAngleMis();
        lastAngle = nowAngle = ddTankPic.getAngle();
        while (nowAngle < targetAngle - angleMis || nowAngle > targetAngle + angleMis) {
            if (nowAngle == lastAngle) {
                failCount++;
            } else {
                failCount = 0;
            }
            if (failCount > 3) {
                // TODO 超过3次尝试变更角度不动，说明需要移动
                return false;
            }
            if (nowAngle < (targetAngle + angleMis)) {
                for (int i = 0; i < targetAngle - nowAngle; i++) {
                    keyboard.keyDown('w');
                    keyboard.keyUp('w');
                }
                delay(100);
            }
            if (nowAngle > targetAngle + angleMis) {
                for (int i = 0; i < nowAngle - targetAngle; i++) {
                    keyboard.keyDown('s');
                    keyboard.keyUp('s');
                }
                delay(100);
            }
            lastAngle = nowAngle;
            nowAngle = ddTankPic.getAngle();
        }
        return true;
    }

    @Override
    public boolean angleAdjust(int targetAngle, DDTankAngleAdjust angleAdjust, TowardEnum toward) {
        int tried = 1;
        while (!angleAdjust(targetAngle) && angleAdjust.move(toward, targetAngle, tried++)) {
            if (tried++ % 3 == 0 && !ddTankPic.isMyRound()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void attack(double strength) {
        int tired = 0;
        double strengthUnit = (double) (properties.getStrengthEndX() - properties.getStrengthStartX()) / 100;
        if (strength < 3) {
            strength = 3;
        } else if (strength > 96) {
            strength = 96;
        }
        String[] checkColors = properties.getStrengthCheckColor().trim().split("\\|");
        for (int i = 0; i < checkColors.length; i++) {
            checkColors[i] = checkColors[i].trim().toUpperCase();
        }
        // 出手前检测是否能够出手，防止卡死
        if (ddTankPic.isMyRound()) {
            String color;
            keyboard.keyDown(' ');
            while (true) {
                color = dm.getColor((int) (properties.getStrengthStartX() + strengthUnit * strength - 1), properties.getStrengthCheckY()).toLowerCase();
                for (String checkColor : checkColors) {
                    if (checkColor.equals(color)) {
                        keyboard.keyUp(' ');
                        // 当颜色不变时，说明当前回合还未结束
                        while (checkColor.equals(color)) {
                            color = dm.getColor((int) (properties.getStrengthStartX() + strengthUnit * strength - 1), properties.getStrengthCheckY()).toLowerCase();
                            delay(1000);
                        }
                        return;
                    }
                }

                tired = tired + 1;
                if (tired % 1000 == 0) {
                    if (!ddTankPic.isMyRound()) {
                        keyboard.keyUp(' ');
                        return;
                    }
                }
                delay(properties.getStrengthCheckDelay());
            }
        }
    }
    @Override
    public int getBestAngle(Point myPosition, Point enemyPosition) {
        if (properties.getIsHandleAttack()) {
            // 手动及固定角度优先
            return properties.getHandleAngle();
        } else if (properties.getIsFixedAngle()) {
            return properties.getFixedAngle();
        }

        double vertical = myPosition.getY() - enemyPosition.getY();
        double horizontal = Math.abs(enemyPosition.getX()) - myPosition.getX();

        double theta;
        int angle;
        theta = Math.atan(vertical / horizontal) * 180 / (4 * Math.atan(1));
        if (theta < 5) {
            angle = 20;
        } else if (theta <= 20) {
            angle = 30;
        } else if (theta <= 26) {
            angle = 40;
        } else if (theta <= 32) {
            angle = 45;
        } else if (theta <= 50) {
            angle = 50;
        } else if (theta <= 65) {
            angle = 65;
        } else {
            angle = 70;
        }
        log.debug("敌我夹角：{}, 最佳角度: {}", theta, angle);
        return angle + properties.getOffsetAngle();
    }

    @Override
    public Point getMyPosition() {
        Point result = new Point();
        if (dm.findStr(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                "我", properties.getColorRole(), 0.9, result)) {
            // 由于大漠找字找的是左上角，所以需要修正
            result.setY(result.getY() + 10);
        }
        return result;
    }

    @Override
    public Point getEnemyPosition() {
        Point result = new Point();
        if (dm.findColor(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                properties.getColorEnemy(), 1, properties.getEnemyFindMode(), result)) {
            // TODO 不同模式下的修正
        }
        return result;
    }

    @Override
    public double getStrength(int angle, double horizontal, double vertical) {
        if (horizontal >= 20) {
            log.warn("当前屏距超过20，请更新力度公式。【当前屏距：{}, 垂直屏距：{}】", horizontal, vertical);
            return 100;
        }
        horizontal = Math.abs(horizontal);
        double strength;
        if (angle <= 20) {
            strength = strengthTable20[(int) horizontal] + (strengthTable20[(int) horizontal] - strengthTable20[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        } else if (angle <= 30) {
            strength = strengthTable30[(int) horizontal] + (strengthTable30[(int) horizontal] - strengthTable30[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        } else if (angle <= 35) {
            strength = strengthTable35[(int) horizontal] + (strengthTable35[(int) horizontal] - strengthTable35[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        } else if (angle <= 40) {
            strength = strengthTable40[(int) horizontal] + (strengthTable40[(int) horizontal] - strengthTable40[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        } else if (angle <= 45) {
            strength = strengthTable45[(int) horizontal] + (strengthTable45[(int) horizontal] - strengthTable45[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        } else if (angle <= 50) {
            strength = strengthTable50[(int) horizontal] + (strengthTable50[(int) horizontal] - strengthTable50[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        } else if (angle <= 65) {
            strength = strengthTable65[(int) horizontal] + (strengthTable65[(int) horizontal] - strengthTable65[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        } else {
            strength = strengthTable70[(int) horizontal] + (strengthTable70[(int) horizontal] - strengthTable70[(int) horizontal + 1]) * (horizontal - (int) horizontal);
        }

        if (vertical > 0.8) {
            double close = 1 / (horizontal / 100) / 10;
            close = close * close + 1;
            if (angle <= 20) {
                strength += upOffset[0] * vertical * close;
            } else if (angle <= 30) {
                strength += upOffset[1] * vertical * close;
            } else if (angle <= 35) {
                strength += upOffset[2] * vertical * close;
            } else if (angle <= 40) {
                strength += upOffset[3] * vertical * close;
            } else if (angle <= 45) {
                strength += upOffset[4] * vertical * close;
            } else if (angle <= 50) {
                strength += upOffset[5] * vertical * close;
            } else if (angle <= 65) {
                strength += upOffset[6] * vertical * close;
            } else {
                strength += upOffset[7] * vertical * close;
            }
        }
        return strength;
    }
}
