package cn.windor.ddtank.core;

/**
 * 脚本核心攻击处理器接口
 */
public interface DDTankCoreAttackHandler extends DDTankCoreSceneTask, DDTankCoreTaskComplexObject {

    /**
     * 在脚本点击选择地图/开始后会调用该方法
     * 目的是为了告知对象本次攻击已经结束了。
     */
    void reset();
}
