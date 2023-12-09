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
import java.util.Map;
import java.util.TreeMap;

@Controller
@RequestMapping("/util")
public class UtilControllerRouting {

    @Autowired
    private DDTankThreadService threadService;

    @GetMapping("/test")
    public String test(Map<String, Object> map) {
        Map<String, String> tests = new TreeMap<>((s1, s2) -> {
            int result;
            result = s1.indexOf(0) - s2.indexOf(0);
            if(result == 0) {
                result = s1.length() - s2.length();
            }
            if(result == 0) {
                return s1.compareTo(s2);
            }
            return result;
        });
        Method[] methods = DDTankPic.class.getMethods();
        for (Method method : methods) {
            Description annotation = AnnotationUtils.getAnnotation(method, Description.class);
            if(annotation != null) {
                tests.put(annotation.value(), method.getName());
            }
        }
        map.put("tests", tests);
        map.put("startedScriptMap", threadService.getAllStartedScriptMap());
        return "util/test";
    }

    @GetMapping("/strength")
    public String strength(Map<String, Object> map) {

        return "util/strength";
    }
}
