package cn.windor.ddtank.controller;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.dto.HttpDataResponse;
import cn.windor.dto.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private DDTankConfigProperties defaultProperties;

    @PostMapping("/add")
    public HttpResponse addScript(@RequestParam String name,
                                  @RequestParam String version,
                                  Boolean needCorrect,
                                  @RequestParam Integer propertiesMode) {
        DDTankConfigProperties startProperties;
        if (propertiesMode == 0) {
            startProperties = defaultProperties.clone();
        } else {
            startProperties = configService.getByIndex(propertiesMode - 1).clone();
        }
        if(needCorrect == null) {
            needCorrect = false;
        }
        DDTankCoreThread coreThread = scriptService.add(name, version, needCorrect, startProperties);
        return HttpResponse.auto(coreThread != null);
    }

    @PostMapping("/start")
    public HttpResponse startScripts(@RequestParam(name = "index") List<Integer> indexList) {
        int success = scriptService.start(indexList);
        return HttpDataResponse.ok(success);
    }
}
