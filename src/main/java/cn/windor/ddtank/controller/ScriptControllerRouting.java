package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.dto.ScriptDDTankCoreThreadDTO;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@RequestMapping("/script")
@Controller
public class ScriptControllerRouting {

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankScriptService scriptService;

    /**
     * 手动启动页面
     */
    @GetMapping("/hand")
    public String handScript(Map<String, Object> map) {
        map.put("configList", configService.list());
        return "script/hand";
    }

    @GetMapping("/add")
    public String addScript(Map<String, Object> map) {
        map.put("configList", configService.list());
        return "script/add";
    }

    @GetMapping("/list")
    public String listScript(Map<String, Object> map) {
        List<DDTankCoreScript> coreThreads = scriptService.list();
        Map<String, List<ScriptDDTankCoreThreadDTO>> classicCoreThreadsMap = new HashMap<>();
        int index = 0;
        for (DDTankCoreScript coreThread : coreThreads) {
            String configName = coreThread.getProperties().getName();
            classicCoreThreadsMap.computeIfAbsent(configName, k -> new ArrayList<>()).add(new ScriptDDTankCoreThreadDTO(index, coreThread));
            index++;
        }
        classicCoreThreadsMap.values().forEach(list -> list.sort(Comparator.comparing(dto -> dto.getCoreThread().getName())));
        map.put("coreThreadsMap", classicCoreThreadsMap);
        return "script/list";
    }
}
