package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.service.DDTankDetailService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.dto.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/detail")
public class DetailController extends BaseScriptController {

    @Autowired
    private DDTankDetailService ddTankDetailService;


    /**
     * 设置自动领取任务
     */
    @PostMapping("/taskAutoComplete")
    public HttpResponse setTaskAutoComplete(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                            @RequestParam(name = "index", required = false) List<Integer> indexList,
                                            @RequestParam int taskAutoComplete) {
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            ddTankDetailService.setTaskAutoComplete(script, taskAutoComplete);
        }
        return HttpResponse.ok();
    }

    /**
     * 设置自动使用道具轮数
     */
    @PostMapping("/autoUseProp")
    public HttpResponse setAutoUseProp(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                       @RequestParam(name = "index", required = false) List<Integer> indexList,
                                       @RequestParam int autoUseProp) {
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            ddTankDetailService.setAutoUseProp(script, autoUseProp);
        }
        return HttpResponse.ok();
    }

    /**
     * 设置自动重连
     */
    @PostMapping("/reconnect")
    public HttpResponse setAutoUseProp(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                       @RequestParam(name = "index", required = false) List<Integer> indexList,
                                       @RequestParam String username,
                                       @RequestParam String password) {
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            ddTankDetailService.setAutoReconnect(script, username, password);
        }
        return HttpResponse.ok();
    }

    /**
     * 添加任务
     */
    @PostMapping("/task")
    public HttpResponse addTask(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                @RequestParam(name = "index", required = false) List<Integer> indexList,
                                @RequestParam int passed,
                                @RequestParam int levelLine,
                                @RequestParam int levelRow,
                                @RequestParam double levelDifficulty) {
        BigDecimal difficulty = new BigDecimal(levelDifficulty);
        LevelRule levelRule = new LevelRule(levelLine, levelRow, passed, difficulty.setScale(1, RoundingMode.UP).doubleValue());
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            ddTankDetailService.addRule(script, levelRule);
        }
        return HttpResponse.ok();
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/task")
    public HttpResponse removeTask(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                   @RequestParam(name = "index", required = false) List<Integer> indexList,
                                   @RequestParam(name = "taskIndex") int index) {
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            ddTankDetailService.removeRule(script, index);
        }
        return HttpResponse.ok();
    }
}
