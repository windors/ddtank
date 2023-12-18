package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.exception.DDTankAngleResolveException;
import cn.windor.ddtank.exception.DDTankStrengthResolveException;
import cn.windor.ddtank.core.DDTankOperate;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.util.ColorUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.*;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDtankOperate10_4 implements DDTankOperate, Serializable {

    private static final long serialVersionUID = 1L;

    protected Mouse mouse;

    protected Keyboard keyboard;

    protected DDTankPic ddTankPic;

    protected DDTankCoreTaskProperties properties;

    protected Library dm;

    public DDtankOperate10_4(Library dm, Mouse mouse, Keyboard keyboard, DDTankPic ddTankPic, DDTankCoreTaskProperties properties) {
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

        delay(800, true);
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
            for (int i = 0; i < ((line - 1) / 2); i++) {
                for (int j = 0; j < 2; j++) {
                    mouse.moveTo(775, 300);
                    mouse.leftDown();
                    mouse.leftUp();
                    delay(100, true);
                    mouse.leftDown();
                    mouse.leftUp();
                    delay(100, true);
                    mouse.moveTo(775, 310);
                    mouse.leftDown();
                    mouse.leftUp();
                    delay(100, true);
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
        delay(100, true);

        // 选择难度
        mouse.moveAndClick(224 + 554 * (properties.getLevelDifficulty() / 100), 500);

        // 点击确定
        mouse.moveAndClick(480, 560);
        delay(300, true);
    }

    // TODO 未识别到数字的情况
    @Override
    public boolean angleAdjust(int targetAngle) {
        int failCount = 0, lastAngle, nowAngle;
        int angleMis = properties.getAngleMis();
        try {
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
                angleAdjust(nowAngle, targetAngle, angleMis);

                lastAngle = nowAngle;
                nowAngle = ddTankPic.getAngle();
            }
        } catch (Exception e) {
            throw new DDTankAngleResolveException();
        }
        return true;
    }

    protected void angleAdjust(int nowAngle, int targetAngle, int angleMis) {
        if (nowAngle < (targetAngle + angleMis)) {
            for (int i = 0; i < targetAngle - nowAngle; i++) {
                keyboard.keyDown('w');
                keyboard.keyUp('w');
            }
            delay(100, true);
        }
        if (nowAngle > targetAngle + angleMis) {
            for (int i = 0; i < nowAngle - targetAngle; i++) {
                keyboard.keyDown('s');
                keyboard.keyUp('s');
            }
            delay(100, true);
        }
    }


    public void attack2(double strength) {
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
                    if (checkColor.equalsIgnoreCase(color)) {
                        keyboard.keyUp(' ');
                        // 当颜色不变时，说明当前回合还未结束
                        while (checkColor.equalsIgnoreCase(color)) {
                            color = dm.getColor((int) (properties.getStrengthStartX() + strengthUnit * strength - 1), properties.getStrengthCheckY()).toLowerCase();
                            if (properties.getAftertreatment()) {
                                // 后处理
                                for (int i = 0; i < properties.getAftertreatmentSec(); i++) {
                                    keyboard.keyPress(properties.getAftertreatmentStr().charAt(0));
                                    delay(1000 / properties.getAftertreatmentSec(), true);
                                }
                            } else {
                                delay(10, true);
                            }
                        }
                        return;
                    }
                }

                tired = tired + 1;
                if (tired % 1000 == 0) {
                    if (!ddTankPic.isMyRound()) {
                        keyboard.keyUp(' ');
                        return;
                    } else {
                        keyboard.keyDown(' ');
                    }
                }
                delay(properties.getStrengthCheckDelay(), true);
            }
        }
    }

    @Override
    public void attack(double strength) {
        int tired = 0;
        double strengthUnit = (double) (properties.getStrengthEndX() - properties.getStrengthStartX()) / 100;
        int x = (int) (properties.getStrengthStartX() + strengthUnit * strength - 1);
        String nowColor = dm.getAveRGB(x - 1, 574, x + 1, 590).toLowerCase();
        while (true) {
            String color = dm.getAveRGB(x - 1, 574, x + 1, 590).toLowerCase();
            if (!ColorUtils.isSimColor(nowColor, color + "-303030")) {
                keyboard.keyUp(' ');
                // 当颜色不变时，说明当前回合还未结束
                nowColor = dm.getAveRGB(x - 1, 574, x + 1, 590).toLowerCase();
                long startTime = System.currentTimeMillis();
                do {
                    if(System.currentTimeMillis() - startTime > 1000) {
                        // 定时检查是否还在回合内，不在回合内直接返回即可
                        if(!ddTankPic.isMyRound()) {
                            return;
                        }
                        startTime = System.currentTimeMillis();
                    }
                    color = dm.getAveRGB(x - 1, 574, x + 1, 590).toLowerCase();
                    if (properties.getAftertreatment()) {
                        // 后处理
                        for (int i = 0; i < properties.getAftertreatmentSec(); i++) {
                            keyboard.keyPress(properties.getAftertreatmentStr().charAt(0));
                            delay(1000 / properties.getAftertreatmentSec(), true);
                        }
                    } else {
                        delay(10, true);
                    }
                } while (ColorUtils.isSimColor(nowColor, color + "-101010"));
                return;
            }
            tired = tired + 1;
            if (tired % 500 == 0) {
                if (!ddTankPic.isMyRound()) {
                    keyboard.keyUp(' ');
                    return;
                } else {
                    keyboard.keyDown(' ');
                }
            }
            delay(properties.getStrengthCheckDelay(), true);
        }
    }

    public static void main(String[] args) {
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
        if (angle == 70) {
            angle = (int) theta - 5;
        }
        if (properties.getIsClosestAngle()) {
            // 临近角度
            Integer nowAngle = ddTankPic.getAngle();
            if (nowAngle > angle) {
                // 只有在当前角度比自动角度高时才能临近，否则根本都打不到boss
                if (nowAngle < 20) {
                    angle = 20;
                } else if (nowAngle < 30) {
                    angle = 30;
                } else if (nowAngle < 40) {
                    angle = 40;
                } else if (nowAngle < 45) {
                    angle = 45;
                } else if (nowAngle < 50) {
                    angle = 50;
                } else if (nowAngle < 65) {
                    angle = 65;
                } else if (nowAngle < 70) {
                    angle = 70;
                }
            }
        }
        log.debug("敌我夹角：{}, 最佳角度: {}", theta, angle);
        return angle + properties.getOffsetAngle();
    }

    @Override
    public double getStrength(int angle, double wind, double horizontal, double vertical) {
        horizontal = Math.abs(horizontal);
        ProcessBuilder processBuilder = new ProcessBuilder("C:\\tmp\\getStrength.exe", angle + "", "" + wind, "" + horizontal, "" + vertical);
        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return Double.parseDouble(reader.readLine());
        } catch (Exception e) {
            throw new DDTankStrengthResolveException(e.getMessage());
        }
    }

}
