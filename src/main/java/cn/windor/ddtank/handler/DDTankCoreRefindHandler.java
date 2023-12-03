package cn.windor.ddtank.handler;

public interface DDTankCoreRefindHandler {
    /**
     * 重新打开新的游戏窗口
     * @param gameHwnd 当前记录的游戏窗口，用于确定打开的是新窗口
     * @return 新的游戏窗口
     */
    long refindHwnd(long gameHwnd);
}