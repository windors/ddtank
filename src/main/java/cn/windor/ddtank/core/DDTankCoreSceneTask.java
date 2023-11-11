package cn.windor.ddtank.core;

/**
 * 场景任务
 *  实现类：副本内
 */
public interface DDTankCoreSceneTask {

    /**
     * 场景内的主循环
     * @return 返回循环的次数，方便守护线程定期重启任务释放内存
     */
    long main();

    /**
     * 暂停方法，需要主循环停止执行
     */
    void suspend();
}
