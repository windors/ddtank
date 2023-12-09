package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreScriptThread;
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
        DDTankCoreScriptThread thread = ddTankThreadService.getThread(hwnd);
        DDTankCoreScript script = thread.getScript();
        map.put("name", script.getName());
        map.put("running", thread.isAlive());
        map.put("ddtankLog", script.getDDTankLog());
        map.put("config", script.getProperties());
        map.put("rules", script.getRules());
        map.put("username", script.getAccountSignHandler().getUsername());
        map.put("password", script.getAccountSignHandler().getPassword());
        map.put("taskAutoComplete", script.getTask().getTaskAutoComplete());
        map.put("autoUseProp", script.getTask().getAutoUseProp());
        map.put("passes", script.getPasses());
        map.put("state", script.getCoreState());
        map.put("runTime", script.getRunTime());
        map.put("suspend", script.isSuspend());
        map.put("levelSummaryMap", script.getTask().getSummary());
        return "detail";
    }

    @GetMapping("/script/{index}")
    public String detail(@PathVariable Integer index,
                         Map<String, Object> map) {
        DDTankCoreScript script = scriptService.getByIndex(index);
        map.put("index", index);
        map.put("name", script.getName());
        map.put("running", ddTankThreadService.isRunning(script));
        map.put("ddtankLog", script.getDDTankLog());
        map.put("config", script.getProperties());
        map.put("rules", script.getRules());
        map.put("username", script.getAccountSignHandler().getUsername());
        map.put("password", script.getAccountSignHandler().getPassword());
        map.put("taskAutoComplete", script.getTask().getTaskAutoComplete());
        map.put("autoUseProp", script.getTask().getAutoUseProp());
        map.put("passes", script.getPasses());
        map.put("state", script.getCoreState());
        map.put("runTime", script.getRunTime());
        map.put("suspend", script.isSuspend());
        map.put("levelSummaryMap", script.getTask().getSummary());
        return "script/detail";
    }
}
