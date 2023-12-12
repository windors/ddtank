package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("config")
public class ConfigControllerRouting {

    @Autowired
    private DDTankCoreTaskProperties defaultConfig;

    @Autowired
    private DDTankThreadService threadService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankScriptService scriptService;

    /**
     * 查看指定运行中脚本的配置
     */
    @GetMapping("/run/{hwnd}")
    public String coreThreadConfig(@PathVariable long hwnd,
                            Map<String, Object> map) {
        DDTankCoreScript script = threadService.get(hwnd);
        map.put("threadName", script.getName());
        map.put("config", script.getProperties());
        return "config/update_run";
    }
    @GetMapping("/script/{index}")
    public String scriptCoreThreadConfig(@PathVariable int index,
                                         Map<String, Object> map) {
        DDTankCoreScript script = scriptService.getByIndex(index);
        map.put("threadName", script.getName());
        map.put("config", script.getProperties());
        return "config/update_script";

    }

    @GetMapping("/add")
    public String addConfig(Map<String, Object> map) {
        map.put("config", defaultConfig);
        return "config/add";
    }


    /**
     * 配置列表
     */
    @GetMapping("list")
    public String listConfig(Map<String, Object> map) {
        map.put("configList", configService.list());
        return "config/list";
    }

    /**
     * 编辑已存在的配置
     */
    @GetMapping("edit/{index}")
    public String editConfig(Map<String, Object> map,
                             @PathVariable int index) {
        map.put("config", configService.getByIndex(index));
        map.put("index", index);
        return "config/edit";
    }

    /**
     * 默认配置
     */
    @GetMapping("default")
    public String defaultConfig(Map<String, Object> map) {
        map.put("config", defaultConfig);
        return "config/default";
    }
}
