package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.service.DDTankDetailService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.dto.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
        DDTankCoreThread coreThread = threadService.getAllStartedThreadMap().get(hwnd);
        return HttpResponse.auto(ddTankDetailService.setTaskAutoComplete(coreThread, taskAutoComplete));
    }
    @PostMapping("/script/{index}/taskAutoComplete")
    public HttpResponse scriptTaskAutoComplete(@PathVariable int index,
                                               @RequestParam int taskAutoComplete) {
        DDTankCoreThread coreThread = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.setTaskAutoComplete(coreThread, taskAutoComplete));
    }

    /**
     * 设置自动使用道具轮数
     */
    @PostMapping("/run/{hwnd}/autoUseProp")
    public HttpResponse autoUseProp(@PathVariable long hwnd,
                                       @RequestParam int autoUseProp) {
        DDTankCoreThread coreThread = threadService.getAllStartedThreadMap().get(hwnd);
        return HttpResponse.auto(ddTankDetailService.setAutoUseProp(coreThread, autoUseProp));
    }
    @PostMapping("/script/{index}/autoUseProp")
    public HttpResponse scriptAutoUseProp(@PathVariable int index,
                                       @RequestParam int autoUseProp) {
        DDTankCoreThread coreThread = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.setAutoUseProp(coreThread, autoUseProp));
    }

    /**
     * 设置自动重连
     */
    @PostMapping("/run/{hwnd}/reconnect")
    public HttpResponse autoReconnect(@PathVariable long hwnd,
                                      @RequestParam String username,
                                      @RequestParam String password) {
        DDTankCoreThread coreThread = threadService.getAllStartedThreadMap().get(hwnd);
        return HttpResponse.auto(ddTankDetailService.setAutoReconnect(coreThread, username, password));
    }
    @PostMapping("/script/{index}/reconnect")
    public HttpResponse scriptAutoReconnect(@PathVariable int index,
                                            @RequestParam String username,
                                            @RequestParam String password) {
        DDTankCoreThread coreThread = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.setAutoReconnect(coreThread, username, password));
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
        DDTankCoreThread coreThread = threadService.getAllStartedThreadMap().get(hwnd);
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
        DDTankCoreThread coreThread = scriptService.getByIndex(index);
        return HttpResponse.auto(ddTankDetailService.addRule(coreThread, levelRule));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/run/{hwnd}/task")
    public HttpResponse removeTask(@PathVariable long hwnd,
                                   @RequestParam int index) {
        DDTankCoreThread coreThread = threadService.getAllStartedThreadMap().get(hwnd);
        return HttpResponse.auto(ddTankDetailService.removeRule(coreThread, index));
    }
    @DeleteMapping("/script/{scriptIndex}/task")
    public HttpResponse scriptRemoveTask(@PathVariable int scriptIndex,
                                   @RequestParam int index) {
        DDTankCoreThread coreThread = scriptService.getByIndex(scriptIndex);
        return HttpResponse.auto(ddTankDetailService.removeRule(coreThread, index));
    }
}
