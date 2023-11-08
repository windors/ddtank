package cn.windor.ddtank.controller;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@Controller
public class IndexControllerRouting {

    @Autowired
    private DDTankThreadService ddtankThreadService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private Library dm;

    @GetMapping({"", "/", "/index"})
    public String index(Map<String, Object> map) {
        map.put("waitStartMap", ddtankThreadService.getWaitStartMap());
        if (ddtankThreadService.getWaitStartMap().size() > 0) {
            map.put("configList", configService.list());
        }
        map.put("startedMap", ddtankThreadService.getAllStartedThreadMap());
        if (System.currentTimeMillis() % 98 == 89) {
            map.put("danger", "");
        }
        return "index";
    }

    @GetMapping("/detail/run/{hwnd}")
    public String runDetail(@PathVariable long hwnd,
                            Map<String, Object> map) {
        DDTankCoreThread coreThread = ddtankThreadService.getAllStartedThreadMap().get(hwnd);
        map.put("name", coreThread.getName());
        map.put("ddtankLog", coreThread.getDDTankLog());
        map.put("config", coreThread.getProperties());
        map.put("rules", coreThread.getRules());
        return "detail";
    }

    @GetMapping("about")
    public String about() {
        return "about";
    }
}