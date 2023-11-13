package cn.windor.ddtank.handler;

/**
 * 接口解决的问题：当找不到敌人时如何走位
 */
public interface DDTankFindPositionMoveHandler {
    /**
     * 移动方法
     * @param triedTimes 已尝试的次数
     * @return 如果返回true表示还需要移动，返回false则不需要移动
     */
    boolean move(int triedTimes);
}
