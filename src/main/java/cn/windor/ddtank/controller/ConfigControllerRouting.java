package cn.windor.ddtank.controller;

import cn.windor.ddtank.config.DDTankConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("config")
public class ConfigControllerRouting {

    @Autowired
    private DDTankConfigProperties config;

    @GetMapping("add")
    public String addConfig(Map<String, Object> map) {
        map.put("config", config);
        return "config/add";
    }

    @GetMapping("list")
    public String listConfig() {
        return "config/list";
    }

    @GetMapping("default")
    public String defaultConfig(Map<String, Object> map) {
        map.put("config", config);
        return "config/default";
    }
}
