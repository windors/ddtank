package cn.windor.ddtank.controller;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Arrays;
import java.util.Map;

@Controller
public class IndexControllerRouting {

    @Autowired
    private DDTankThreadService dDtankThreadService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private Library dm;

    @GetMapping({"", "/", "/index"})
    public String index(Map<String, Object> map) {
        map.put("waitStartMap", dDtankThreadService.getWaitStartMap());
        if (dDtankThreadService.getWaitStartMap().size() > 0) {
            map.put("configList", configService.list());
        }
        map.put("startedMap", dDtankThreadService.getAllStartedThreadMap());
        if (System.currentTimeMillis() % 98 == 89) {
            map.put("danger", "");
        }
        return "index";
    }

    @GetMapping("/detail/run/{hwnd}")
    public String runDetail(@PathVariable long hwnd,
                            Map<String, Object> map) {
        DDTankCoreThread coreThread = dDtankThreadService.getAllStartedThreadMap().get(hwnd);
        map.put("name", coreThread.getName());
        return "detail";
    }

    @GetMapping("about")
    public String about() {
        return "about";
    }
}