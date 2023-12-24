package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.exception.DDTankScriptNotFoundException;
import cn.windor.ddtank.service.DDTankMarkHwndService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


@Slf4j
public class BaseScriptController {
    @Autowired
    protected DDTankScriptService scriptService;

    @Autowired
    protected DDTankMarkHwndService markHwndService;

    @Autowired
    protected DDTankThreadService threadService;

    /**
     * 根据hwnds和indexList来获取内存中的脚本，如果找不到指定脚本则会抛出
     * @return
     */
    protected final List<DDTankCoreScript> getScripts(List<Long> hwnds, List<Integer> indexList) {
        List<DDTankCoreScript> scripts = new ArrayList<>();
        if(hwnds == null && indexList == null) {
            throw new DDTankScriptNotFoundException();
        }

        if(hwnds != null) {
            for (Long hwnd : hwnds) {
                DDTankCoreScript script = threadService.get(hwnd);
                if (script != null) {
                    // 先从运行脚本中获取
                    scripts.add(script);
                }else {
                    // 若运行脚本中未获取到，则去markHwndService中获取
                    script = markHwndService.get(hwnd);
                    if(script != null) {
                        scripts.add(script);
                    }
                }
            }
        }
        if (indexList != null) {
            for (Integer index : indexList) {
                scripts.add(scriptService.getByIndex(index));
            }
        }
        if(scripts.size() == 0) {
            // TODO 异常处理
            throw new DDTankScriptNotFoundException();
        }
        return scripts;
    }

    protected final DDTankCoreScript getScript(Long hwnd, Integer index) {
        DDTankCoreScript script;
        if(hwnd != null && index != null) {
            log.warn("参数传递不正确，请传递hwnd或index值");
        }
        if(hwnd != null) {
            script = threadService.get(hwnd);
        }else if(index != null) {
            script = scriptService.getByIndex(index);
        }else {
            // TODO 异常处理
            throw new DDTankScriptNotFoundException();
        }
        return script;
    }
}
