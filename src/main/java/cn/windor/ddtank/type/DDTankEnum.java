package cn.windor.ddtank.type;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DDTankEnum implements IEnum<Integer> {

    ;

    @EnumValue
    @JsonValue
    private final int code;
    private final String msg;

    private DDTankEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getValue() {
        return this.code;
    }

    public int getCode() {
        return this.code;
    }

    public String getMsg() {
        return this.msg;
    }
}
