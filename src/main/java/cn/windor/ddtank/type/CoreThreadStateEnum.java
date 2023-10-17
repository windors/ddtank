package cn.windor.ddtank.type;

public enum CoreThreadStateEnum {
    /**
     * 未启动
     */
    NOT_STARTED,
    /**
     * 准备启动（加载了配置但并未调用start方法）
     */
    WAITING_START,
    /**
     * 正常运行
     */
    RUN,
    /**
     * 准备暂停
     */
    WAITING_SUSPEND,
    /**
     * 已暂停
     */
    SUSPEND,
    /**
     * 准备恢复
     */
    WAITING_CONTINUE,

    /**
     * 等待停止
     */
    WAITING_STOP,
    /**
     * 已停止
     */
    STOP
}
