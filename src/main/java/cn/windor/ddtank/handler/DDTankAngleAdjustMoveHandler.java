package cn.windor.ddtank.handler;

import cn.windor.ddtank.type.TowardEnum;

/**
 * 接口解决的问题：在调整角度时如果不能直接调整到指定角度时该如何操作
 */
public interface DDTankAngleAdjustMoveHandler {

    /**
     * 如何走位
     * @return 是否还需要移动
     */
    boolean move(TowardEnum targetToward, int targetAngle, int triedTimes);
}
