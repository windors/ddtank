package cn.windor.ddtank.controller;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.dto.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private DDTankConfigService ddTankConfigService;

    @PostMapping("add")
    public HttpResponse addConfig(DDTankConfigProperties config) {
        return HttpResponse.notDev();
    }

    @PostMapping("default")
    public HttpResponse updateDefault(DDTankConfigProperties config) {
        return HttpResponse.auto(ddTankConfigService.saveDefaultConfig(config));
    }
}
