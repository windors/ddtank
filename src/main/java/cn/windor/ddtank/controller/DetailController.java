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

@RestController
@RequestMapping("/detail")
public class DetailController {

    @Autowired
    private DDTankDetailService ddTankDetailService;

    @Autowired
    private DDTankScriptService scriptService;

    @Autowired
    private DDTankThreadService threadService;

    /**
     * 设置自动领取任务
     */
    @PostMapping("/run/{hwnd}/taskAutoComplete")
    public HttpResponse taskAutoComplete(@PathVariable long hwnd,
                                         @RequestParam int taskAutoComplete) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        return HttpResponse.auto(ddTankDetailService.setTaskAutoComplete(coreThread, taskAutoComplete));
    }
    @PostMapping("/script/{index}/taskAutoComplete")
    public HttpResponse scriptTaskAutoComplete(@PathVariable int index,
                                               @RequestParam int taskAutoComplete) {
        DDTankCoreScript script = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.setTaskAutoComplete(script, taskAutoComplete));
    }

    /**
     * 设置自动使用道具轮数
     */
    @PostMapping("/run/{hwnd}/autoUseProp")
    public HttpResponse autoUseProp(@PathVariable long hwnd,
                                       @RequestParam int autoUseProp) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        return HttpResponse.auto(ddTankDetailService.setAutoUseProp(coreThread, autoUseProp));
    }
    @PostMapping("/script/{index}/autoUseProp")
    public HttpResponse scriptAutoUseProp(@PathVariable int index,
                                       @RequestParam int autoUseProp) {
        DDTankCoreScript script = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.setAutoUseProp(script, autoUseProp));
    }

    /**
     * 设置自动重连
     */
    @PostMapping("/run/{hwnd}/reconnect")
    public HttpResponse autoReconnect(@PathVariable long hwnd,
                                      @RequestParam String username,
                                      @RequestParam String password) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        return HttpResponse.auto(ddTankDetailService.setAutoReconnect(coreThread, username, password));
    }
    @PostMapping("/script/{index}/reconnect")
    public HttpResponse scriptAutoReconnect(@PathVariable int index,
                                            @RequestParam String username,
                                            @RequestParam String password) {
        DDTankCoreScript script = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.setAutoReconnect(script, username, password));
    }

    /**
     * 添加任务
     */
    @PostMapping("/run/{hwnd}/task")
    public HttpResponse addTask(@PathVariable long hwnd,
                                @RequestParam int passed,
                                @RequestParam int levelLine,
                                @RequestParam int levelRow,
                                @RequestParam double levelDifficulty) {
        BigDecimal difficulty = new BigDecimal(levelDifficulty);
        LevelRule levelRule = new LevelRule(levelLine, levelRow, passed, difficulty.setScale(1, RoundingMode.UP).doubleValue());
        DDTankCoreScript coreThread = threadService.get(hwnd);
        return HttpResponse.auto(ddTankDetailService.addRule(coreThread, levelRule));
    }
    @PostMapping("/script/{index}/task")
    public HttpResponse scriptAddTask(@PathVariable int index,
                                @RequestParam int passed,
                                @RequestParam int levelLine,
                                @RequestParam int levelRow,
                                @RequestParam double levelDifficulty) {
        BigDecimal difficulty = new BigDecimal(levelDifficulty);
        LevelRule levelRule = new LevelRule(levelLine, levelRow, passed, difficulty.setScale(1, RoundingMode.UP).doubleValue());
        DDTankCoreScript script = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.addRule(script, levelRule));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/run/{hwnd}/task")
    public HttpResponse removeTask(@PathVariable long hwnd,
                                   @RequestParam int index) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        return HttpResponse.auto(ddTankDetailService.removeRule(coreThread, index));
    }
    @DeleteMapping("/script/{scriptIndex}/task")
    public HttpResponse scriptRemoveTask(@PathVariable int scriptIndex,
                                   @RequestParam int index) {
        DDTankCoreScript script = scriptService.getByIndex(scriptIndex);
        return HttpResponse.auto(ddTankDetailService.removeRule(script, index));
    }
}
