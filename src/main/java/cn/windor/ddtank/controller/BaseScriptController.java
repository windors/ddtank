package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


public class BaseScriptController {
    @Autowired
    protected DDTankScriptService scriptService;

    @Autowired
    protected DDTankThreadService threadService;

    protected final List<DDTankCoreScript> getScripts(List<Long> hwnds, List<Integer> indexList) {
        List<DDTankCoreScript> scripts = new ArrayList<>();
        if(hwnds == null && indexList == null) {
            return scripts;
        }

        if(hwnds != null) {
            for (Long hwnd : hwnds) {
                DDTankCoreScript script = threadService.get(hwnd);
                if (script != null) {
                    scripts.add(script);
                }
            }
        }
        if (indexList != null) {
            for (Integer index : indexList) {
                scripts.add(scriptService.getByIndex(index));
            }
        }
        return scripts;
    }
}
