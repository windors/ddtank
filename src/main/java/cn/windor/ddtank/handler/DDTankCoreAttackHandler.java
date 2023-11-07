package cn.windor.ddtank.handler;

/**
 * 脚本核心攻击处理器接口
 */
public interface DDTankCoreAttackHandler {
    /**
     * 在脚本检测到进入副本后会调用该方法
     */
    void main();

    /**
     * 在脚本点击选择地图/开始后会调用该方法
     */
    void reset();
}
