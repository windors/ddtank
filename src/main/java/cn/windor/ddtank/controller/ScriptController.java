package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScriptThread;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.dto.HttpDataResponse;
import cn.windor.dto.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/script")
public class ScriptController {

    @Autowired
    private DDTankScriptService scriptService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankThreadService ddTankThreadService;

    @Autowired
    private DDTankCoreTaskProperties defaultProperties;

    @PostMapping("/add")
    public HttpResponse addScript(@RequestParam String name,
                                  Boolean needCorrect,
                                  @RequestParam Integer propertiesMode) {
        DDTankCoreTaskProperties startProperties;
        if (propertiesMode == 0) {
            startProperties = defaultProperties.clone();
        } else {
            startProperties = configService.getByIndex(propertiesMode - 1).clone();
        }
        if(needCorrect == null) {
            needCorrect = false;
        }
        DDTankCoreScript coreThread = scriptService.add(name, needCorrect, startProperties);
        return HttpResponse.auto(coreThread != null);
    }

    @PostMapping("/add/{hwnd}")
    public HttpResponse addScriptByHwnd(@PathVariable long hwnd) {
        DDTankCoreScript script = ddTankThreadService.get(hwnd);
        if(script == null) {
            return HttpResponse.err();
        }
        scriptService.addOrUpdate(script);
        return HttpResponse.ok();
    }

    @PostMapping("/start")
    public HttpResponse startScripts(@RequestParam(name = "index") List<Integer> indexList) {
        List<DDTankCoreScript> scripts = new ArrayList<>(indexList.size());
        for (Integer index : indexList) {
            scripts.add(scriptService.getByIndex(index));
        }
        int success = 0;
        for (DDTankCoreScript script : scripts) {
            if(ddTankThreadService.start(script)) {
                success++;
            }
        }
        return HttpDataResponse.ok(success);
    }
}
