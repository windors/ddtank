package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.dto.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/config")
public class ConfigController {

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankThreadService threadService;

    /**
     * 保存默认配置
     */
    @PostMapping("default")
    public HttpResponse updateDefault(DDTankCoreTaskProperties config) {
        return HttpResponse.auto(configService.saveDefaultConfig(config));
    }

    /**
     * 更新运行中的配置
     */
    @PutMapping("/run/{hwnd}")
    public HttpResponse updateCoreThreadProperties(@PathVariable long hwnd,
                                                   DDTankCoreTaskProperties properties) {
        return HttpResponse.auto(threadService.updateProperties(hwnd, properties));
    }

    /**
     * 更新运行中的配置
     */
    @PutMapping("{index}")
    public HttpResponse updateProperties(@PathVariable int index,
                                                   DDTankCoreTaskProperties properties) {
        return HttpResponse.auto(configService.update(index, properties));
    }

    /**
     * 删除配置
     */
    @DeleteMapping("{index}")
    public HttpResponse deleteConfig(@PathVariable int index) {
        return HttpResponse.auto(configService.removeByIndex(index) != null);
    }

    /**
     * 添加新配置
     */
    @PostMapping("")
    public HttpResponse addConfig(@RequestParam String name,
                                  DDTankCoreTaskProperties config) {
        return HttpResponse.auto(configService.add(config));
    }
}
