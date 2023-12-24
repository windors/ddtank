package cn.windor.ddtank.dto;


public enum DDTankThreadResponseEnum implements Response {
    OK(200, "OK"),
    FAIL(405, "失败"),
    THREAD_IS_ALIVE(405, "脚本线程正在运行中"),
    WINDOW_IS_ILLEGAL(405, "当前窗口未通过校验"),
    WINDOW_IS_BUNDED(405, "当前窗口已被其他脚本绑定"),
    WINDOW_SCRIPT_IS_NOT_EXISTS(405, "指定窗口未绑定脚本");

    private final Integer code;

    private final String msg;

    private DDTankThreadResponseEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getValue() {
        return this.code;
    }

    public Integer getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
