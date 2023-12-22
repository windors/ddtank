package cn.windor.ddtank.dto;

public class HttpResponseBuilder {
    private final Response response;

    private String msg;

    public HttpResponseBuilder(Response response) {
        this.response = response;
    }

    public HttpResponseBuilder setMsg(String msg) {
        this.msg = msg;
        return this;
    }



    public Response build() {
        return new Response() {
            @Override
            public Integer getCode() {
                return response.getCode();
            }

            @Override
            public String getMsg() {
                return msg == null ? response.getMsg() : msg;
            }
        };
    }
}
