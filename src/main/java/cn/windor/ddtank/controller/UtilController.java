package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.dto.HttpDataResponse;
import cn.windor.dto.HttpResponse;
import cn.windor.type.HttpResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/util")
public class UtilController {

    @Autowired
    private DDTankThreadService threadService;

    @PostMapping("/test")
    public HttpDataResponse<Object> test(@RequestParam String methodName,
                                 @RequestParam long hwnd) {
        DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if(thread == null) {
            return new HttpDataResponse(HttpResponseEnum.ILLEGAL_INPUT, null);
        }
        try{
            Method method = DDTankPic.class.getMethod(methodName);
            Object result = method.invoke(thread.getDdtankPic());
            return new HttpDataResponse(HttpResponseEnum.OK, result == null ? "null" : result.toString());
        }catch (Exception e) {
            e.printStackTrace();
            return new HttpDataResponse(HttpResponseEnum.ERROR, e);
        }
    }

    @PostMapping("/suspend")
    public HttpResponse suspendCoreThread(@RequestParam long hwnd) {
        DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if(thread == null) {
            return new HttpResponse(HttpResponseEnum.ILLEGAL_INPUT);
        }
        thread.sendSuspend();
        return HttpResponse.ok();
    }

    @PostMapping("/continue")
    public HttpResponse continueCoreThread(@RequestParam long hwnd) {
        DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if(thread == null) {
            return new HttpResponse(HttpResponseEnum.ILLEGAL_INPUT);
        }
        thread.sendContinue();
        return HttpResponse.ok();
    }

    /**
     * TODO 将返回值映射到前端
     * @param hwnd
     * @return
     */
    @PostMapping("/state")
    public HttpDataResponse<CoreThreadStateEnum> getCoreThreadState(@RequestParam long hwnd) {
        DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if(thread == null) {
            return HttpDataResponse.ok(CoreThreadStateEnum.NOT_STARTED);
        }
        return HttpDataResponse.ok(thread.getCoreState());
    }

    @PostMapping("/capture")
    public StreamingResponseBody capture(@RequestParam long hwnd) {
        return null;
    }

    @PostMapping("/start")
    public HttpDataResponse start(@RequestParam long hwnd,
                              @RequestParam int keyboardMode,
                              @RequestParam int mouseMode,
                              @RequestParam int picMode,
                              @RequestParam int operateMode,
                              @RequestParam int propertiesMode,
                              @RequestParam String name) {
        return HttpDataResponse.ok(threadService.start(hwnd, keyboardMode, mouseMode, picMode, operateMode, propertiesMode, name));
    }
}
