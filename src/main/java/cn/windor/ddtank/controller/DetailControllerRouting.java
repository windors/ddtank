package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("detail")
public class DetailControllerRouting {

    @Autowired
    private DDTankThreadService ddTankThreadService;

    @Autowired
    private DDTankScriptService scriptService;

    @GetMapping("run/{hwnd}")
    public String runDetail(@PathVariable long hwnd,
                            Map<String, Object> map) {
        DDTankCoreThread coreThread = ddTankThreadService.getAllStartedThreadMap().get(hwnd);
        map.put("name", coreThread.getName());
        map.put("ddtankLog", coreThread.getDDTankLog());
        map.put("config", coreThread.getProperties());
        map.put("rules", coreThread.getRules());
        map.put("username", coreThread.getAccountSignHandler().getUsername());
        map.put("password", coreThread.getAccountSignHandler().getPassword());
        map.put("taskAutoComplete", coreThread.getTask().getTaskAutoComplete());
        map.put("autoUseProp", coreThread.getTask().getAutoUseProp());
        map.put("passes", coreThread.getPasses());
        map.put("state", coreThread.getCoreState());
        map.put("runTime", coreThread.getRunTime());
        map.put("suspend", coreThread.isSuspend());
        return "detail";
    }

    @GetMapping("/script/{index}")
    public String detail(@PathVariable Integer index,
                         Map<String, Object> map) {
        DDTankCoreThread coreThread = scriptService.getByIndex(index);
        map.put("index", index);
        map.put("name", coreThread.getName());
        map.put("ddtankLog", coreThread.getDDTankLog());
        map.put("config", coreThread.getProperties());
        map.put("rules", coreThread.getRules());
        map.put("username", coreThread.getAccountSignHandler().getUsername());
        map.put("password", coreThread.getAccountSignHandler().getPassword());
        map.put("taskAutoComplete", coreThread.getTask().getTaskAutoComplete());
        map.put("autoUseProp", coreThread.getTask().getAutoUseProp());
        map.put("passes", coreThread.getPasses());
        map.put("state", coreThread.getCoreState());
        map.put("runTime", coreThread.getRunTime());
        map.put("suspend", coreThread.isSuspend());
        return "script/detail";
    }
}
