package cn.windor.ddtank.core;

import cn.windor.ddtank.type.TowardEnum;

public interface DDTankAngleAdjust {

    /**
     * 如何走位
     * @return 是否还需要移动
     */
    boolean move(TowardEnum targetToward, int targetAngle, int triedTimes);
}
