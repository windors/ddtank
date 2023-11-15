package cn.windor.ddtank.core;

/**
 * 弹弹堂核心任务复杂对象
 * 在任务重启时需要对复杂对象进行重赋值，此时这些复杂对象就不应该再使用原先的大漠对象，原先的大漠对象已经被销毁，此时就需要使用新的大漠对象
 */
public interface DDTankCoreTaskComplexObject {
    /**
     *
     * @param complexObject 需要更新的参数
     * @return 是否全部更新成功
     */
    boolean update(Object... complexObject);
}
