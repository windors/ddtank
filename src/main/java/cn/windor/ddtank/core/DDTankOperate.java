package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.type.TowardEnum;

public interface DDTankOperate {
    void chooseMap();

    double calcUnitDistance();

    boolean angleAdjust(int targetAngle);

    boolean angleAdjust(int targetAngle, DDTankAngleAdjust angleAdjust, TowardEnum toward);

    void attack(double strength);

    /**
     * 获取最佳角度
     * @return
     */
    int getBestAngle(Point myPosition, Point enemyPosition);

    /**
     * 获取我的位置
     * @return null表示未找到
     */
    Point getMyPosition();

    /**
     * 获取敌人的位置
     * @return null表示未找到
     */
    Point getEnemyPosition();

    double getStrength(int angle, double horizontal, double vertical);
}
