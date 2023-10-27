package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.handler.DDTankAngleAdjustMoveHandler;
import cn.windor.ddtank.type.TowardEnum;

public interface DDTankOperate {
    void chooseMap();

    boolean angleAdjust(int targetAngle);

    boolean angleAdjust(int targetAngle, DDTankAngleAdjustMoveHandler angleAdjust, TowardEnum toward);

    void attack(double strength);

    /**
     * 获取最佳角度
     * @return
     */
    int getBestAngle(Point myPosition, Point enemyPosition);

    double getStrength(int angle, double horizontal, double vertical);
}
