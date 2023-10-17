package cn.windor.ddtank.controller;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankConfigService;
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
    private DDTankConfigProperties defaultConfig;

    @Autowired
    private DDTankThreadService threadService;

    @Autowired
    private DDTankConfigService configService;

    /**
     * 查看指定运行中脚本的配置
     */
    @GetMapping("/run/{hwnd}")
    public String coreThreadConfig(@PathVariable long hwnd,
                            Map<String, Object> map) {
        DDTankCoreThread coreThread = threadService.getAllStartedThreadMap().get(hwnd);
        map.put("threadName", coreThread.getName());
        map.put("config", coreThread.getProperties());
        return "config/update";
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
