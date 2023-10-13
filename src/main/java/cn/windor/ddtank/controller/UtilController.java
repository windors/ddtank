package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.core.DDtankCoreThread;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.dto.HttpDataResponse;
import cn.windor.dto.HttpResponse;
import cn.windor.type.HttpResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;

@RestController
@RequestMapping("/util")
public class UtilController {

    @Autowired
    private DDTankThreadService threadService;

    @PostMapping("/test")
    public HttpDataResponse<Object> test(@RequestParam String methodName,
                                 @RequestParam long hwnd) {
        DDtankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if(thread == null) {
            return new HttpDataResponse(HttpResponseEnum.ILLEGAL_INPUT, null);
        }
        try{
            Method method = DDTankPic.class.getMethod(methodName);
            return new HttpDataResponse(HttpResponseEnum.OK, method.invoke(thread.getDmPic()));
        }catch (Exception e) {
            e.printStackTrace();
            return new HttpDataResponse(HttpResponseEnum.ERROR, e);
        }
    }

    @PostMapping("/debug")
    public HttpResponse suspendHwnd(@RequestParam long hwnd,
                                    @RequestParam boolean debug) {
        DDtankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if(thread == null) {
            return new HttpResponse(HttpResponseEnum.ILLEGAL_INPUT);
        }
        thread.setDebug(debug);
        return HttpResponse.ok();
    }
}
