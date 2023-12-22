package cn.windor.ddtank.exception;

import cn.windor.ddtank.dto.Response;
import lombok.Getter;

public class CallFailedException extends RuntimeException {
    // 失败原因
    @Getter
    private final Response response;

    public CallFailedException(Response reason) {
        this.response = reason;
    }
}
