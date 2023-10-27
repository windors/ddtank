package cn.windor.ddtank.type;

import lombok.Getter;

public enum CoreThreadStateEnum {
    /**
     * 未启动
     */
    NOT_STARTED("未启动"),
    /**
     * 准备启动（加载了配置但并未调用start方法）
     */
    WAITING_START("等待启动"),
    /**
     * 正常运行
     */
    RUN("运行中"),
    /**
     * 准备暂停
     */
    WAITING_SUSPEND("等待暂停"),
    /**
     * 已暂停
     */
    SUSPEND("已暂停"),
    /**
     * 准备恢复
     */
    WAITING_CONTINUE("等待恢复"),

    /**
     * 等待停止
     */
    WAITING_STOP("等待停止运行"),
    /**
     * 已停止
     */
    STOP("已停止运行");

    @Getter
    final String msg;
    private CoreThreadStateEnum(String msg) {
        this.msg = msg;
    }
}
