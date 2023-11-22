package cn.windor.ddtank.controller;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.dto.StartedDDTankCoreThreadDTO;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.*;

@Controller
public class IndexControllerRouting {

    @Autowired
    private DDTankThreadService ddtankThreadService;

    @Autowired
    private DDTankConfigService configService;

    @GetMapping({"", "/", "/index"})
    public String index(Map<String, Object> map) {
        map.put("waitStartMap", ddtankThreadService.getWaitStartMap());
        if (ddtankThreadService.getWaitStartMap().size() > 0) {
            map.put("configList", configService.list());
        }

        // key: 配置名称, value: 脚本集合
        Map<String, List<StartedDDTankCoreThreadDTO>> classifiedStartedMap = new HashMap<>();
        Map<Long, DDTankCoreThread> startedThreadMap = ddtankThreadService.getAllStartedThreadMap();
        for (Long hwnd : startedThreadMap.keySet()) {
            DDTankCoreThread coreThread = startedThreadMap.get(hwnd);
            String configName = coreThread.getProperties().getName();
            List<StartedDDTankCoreThreadDTO> threadList = classifiedStartedMap.computeIfAbsent(configName, k -> new ArrayList<>());
            threadList.add(new StartedDDTankCoreThreadDTO(hwnd, coreThread));
        }
        classifiedStartedMap.values().forEach(list -> list.sort(Comparator.comparing(dto -> dto.getCoreThread().getName())));

        map.put("classifiedStartedMap", classifiedStartedMap);


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
        map.put("username", coreThread.getTaskRefindHandler().getUsername());
        map.put("password", coreThread.getTaskRefindHandler().getPassword());
        map.put("taskAutoComplete", coreThread.getTask().getTaskAutoComplete());
        map.put("autoUseProp", coreThread.getTask().getAutoUseProp());
        map.put("passes", coreThread.getPasses());
        map.put("state", coreThread.getCoreState());
        map.put("runTime", coreThread.getRunTime());
        map.put("suspend", coreThread.isSuspend());
        return "detail";
    }

    @GetMapping("about")
    public String about() {
        return "about";
    }
}