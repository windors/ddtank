package cn.windor.ddtank.dto;

public enum DDTankResponseEnum implements Response {

    OK(200, "OK"),
    PARAM_LOST(405, "未根据参数找到符合要求的值"),
    WINDOW_SCRIPT_IS_NOT_EXISTS(405, "指定窗口未绑定脚本");;

    private final Integer code;
    private final String msg;

    private DDTankResponseEnum(int code, String msg) {
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
