package cn.windor.ddtank.controller;

import cn.windor.ddtank.service.DDTankConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@RequestMapping("/script")
@Controller
public class ScriptControllerRouting {

    @Autowired
    private DDTankConfigService configService;

    @GetMapping("/add")
    public String addScript(Map<String, Object> map) {
        map.put("configList", configService.list());
        return "script/add";
    }
}
