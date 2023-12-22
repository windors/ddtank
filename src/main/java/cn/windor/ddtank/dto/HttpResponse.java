package cn.windor.ddtank.dto;


import java.io.Serializable;

/**
 * Windor项目统一返回对象
 */
public class HttpResponse implements Serializable {
    protected final int code;
    protected final String msg;

    protected static final HttpResponse OK = new HttpResponse(200, "OK");

    protected static final HttpResponse ERROR = new HttpResponse(500, "ERROR");

    public HttpResponse(Response responseEnum) {
        this.code = responseEnum.getCode();
        this.msg = responseEnum.getMsg();
    }

    public HttpResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static HttpResponse ok() {
        return OK;
    }

    public static HttpResponse auto(boolean success) {
        return success ? OK : ERROR;
    }

    public static HttpResponse auto(Response response) {
        return new HttpResponse(response);
    }


    public static HttpResponse err(Response ddTankHttpResponseEnum) {
        return new HttpResponse(ddTankHttpResponseEnum);
    }

    public static HttpResponse err(Response ddTankHttpResponseEnum, String reason) {
        return new HttpResponse(ddTankHttpResponseEnum.getCode(), reason);
    }

    public static HttpResponse notDev() {
        return new HttpResponse(500, "功能正在开发中");
    }
}
