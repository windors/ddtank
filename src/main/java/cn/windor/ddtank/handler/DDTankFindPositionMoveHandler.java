package cn.windor.ddtank.handler;

/**
 * 接口解决的问题：当找不到敌人时如何走位
 */
public interface DDTankFindPositionMoveHandler {
    boolean move(int triedTimes);
}
