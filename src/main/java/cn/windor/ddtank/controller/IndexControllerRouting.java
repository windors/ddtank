package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDtankCoreThread;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class IndexControllerRouting {

    @Autowired
    private DDTankThreadService dDtankThreadService;

    @GetMapping({"", "/", "/index"})
    public String index(Map<String, Object> map) {
        Map<Long, DDtankCoreThread> threadMap = dDtankThreadService.getAllStartedThreadMap();
        map.put("threadMap", threadMap);
        return "index";
    }
}