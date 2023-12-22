package cn.windor.ddtank.dto;



public class HttpDataResponse<T> extends HttpResponse {
    private T data;

    public HttpDataResponse(int code, String msg, T data) {
        super(code, msg);
        this.data = data;
    }

    public HttpDataResponse(Response responseEnum, T data) {
        super(responseEnum);
        this.data = data;
    }

    public static <T> HttpDataResponse<T> ok(T data) {
        return new HttpDataResponse<T>(OK.code, OK.msg, data);
    }

    public static HttpDataResponse<?> err(Response responseEnum) {
        return new HttpDataResponse<>(responseEnum.getCode(), responseEnum.getMsg(), null);
    }

    public static <T> HttpDataResponse<T> err(Response responseEnum, T data) {
        return new HttpDataResponse<>(responseEnum.getCode(), responseEnum.getMsg(), data);
    }

    public static void main(String[] args) {
        HttpResponse err = HttpDataResponse.err(DDTankResponseEnum.PARAM_LOST);
    }
}