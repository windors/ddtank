package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/util")
public class UtilControllerRouting {

    @Autowired
    private DDTankThreadService threadService;

    @GetMapping("/test")
    public String test(Map<String, Object> map) {
        Map<String, String> tests = new HashMap<>();
        Method[] methods = DDTankPic.class.getMethods();
        for (Method method : methods) {
            Description annotation = AnnotationUtils.getAnnotation(method, Description.class);
            if(annotation != null) {
                tests.put(annotation.value(), method.getName());
            }
        }
        map.put("tests", tests);
        map.put("startedThreadMap", threadService.getAllStartedThreadMap());
        return "util/test";
    }
}
