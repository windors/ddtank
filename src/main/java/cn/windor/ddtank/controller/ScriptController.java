package cn.windor.ddtank.controller;


import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.dto.*;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankMarkHwndService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/script")
public class ScriptController extends BaseScriptController {

    @Autowired
    private DDTankScriptService scriptService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankThreadService ddTankThreadService;

    @Autowired
    private DDTankCoreTaskProperties defaultProperties;

    @Autowired
    private DDTankMarkHwndService markHwndService;


    /**
     * 添加脚本（直接序列化到硬盘）
     *
     * @param name           脚本名称
     * @param needCorrect    可选值，表示脚本是否需要矫正
     * @param propertiesMode 脚本配置索引，如果为0则使用默认配置，否则从本地配置列表中按值获取
     */
    @PostMapping("/add")
    public HttpResponse addScript(@RequestParam String name,
                                  Boolean needCorrect,
                                  @RequestParam Integer propertiesMode) {
        DDTankCoreTaskProperties startProperties;
        if (propertiesMode == 0) {
            startProperties = defaultProperties.clone();
        } else {
            startProperties = configService.getByIndex(propertiesMode - 1).clone();
        }
        if (needCorrect == null) {
            needCorrect = false;
        }
        DDTankCoreScript coreThread = scriptService.add(name, needCorrect, startProperties);
        return HttpResponse.auto(coreThread != null);
    }

    /**
     * 根据从首页脚本列表中获取脚本进行序列化保存
     * TODO 改完之后明天计划加一个重绑定链，这样前端没刷新的情况下调用不会出现传参异常了
     */
    @PostMapping("/save")
    public HttpResponse addScriptByHwnd(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                        @RequestParam(name = "index", required = false) List<Integer> indexList) {
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            scriptService.addOrUpdate(script);
        }
        return HttpResponse.ok();
    }

    /**
     * 将序列化的脚本启动
     * TODO 已序列化的脚本调用启动方法时需要优化窗口失效的情况：窗口失效时，key要取一个负数向后排以区分脚本，防止出现两个脚本hwnd值相同的情况下程序认为窗口已绑定，进而无法启动脚本。
     * TODO 将启动拆分为参数设定-启动
     *
     * @param indexList 要启动的脚本
     * @return 成功启动脚本的个数
     */
    @PostMapping("/start")
    public HttpDataResponse startScripts(
            @RequestParam(name = "hwnd", required = false) List<Long> hwnds,
            @RequestParam(name = "index", required = false) List<Integer> indexList) {
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        return HttpDataResponse.ok(ddTankThreadService.start(scripts));
    }

    /**
     * 暂停指定脚本
     */
    @PostMapping("/suspend")
    public HttpDataResponse<Integer> suspendCoreThread(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                                       @RequestParam(name = "index", required = false) List<Integer> indexList) {
        int success = 0;
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            script.sendSuspend();
            success++;
        }
        return HttpDataResponse.ok(success);
    }

    /**
     * 使指定脚本恢复运行
     */
    @PostMapping("/continue")
    public HttpResponse continueCoreThread(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                           @RequestParam(name = "index", required = false) List<Integer> indexList) {
        int success = 0;
        List<DDTankCoreScript> scripts = getScripts(hwnds, indexList);
        for (DDTankCoreScript script : scripts) {
            script.sendContinue();
            success++;
        }
        return HttpDataResponse.ok(success);
    }

    /**
     * 查询指定脚本运行状态
     */
    @PostMapping("/state")
    public HttpDataResponse<CoreThreadStateEnum> getCoreThreadState(@RequestParam long hwnd) {
        DDTankCoreScript thread = threadService.get(hwnd);
        if (thread == null) {
            return HttpDataResponse.err(DDTankResponseEnum.PARAM_LOST, CoreThreadStateEnum.NOT_STARTED);
        }
        return HttpDataResponse.ok(thread.getCoreState());
    }

//    /**
//     * TODO 二合一
//     *
//     * @return
//     */
//    @PostMapping("/start")
//    public HttpResponse start(@RequestParam long hwnd,
//                              String name,
//                              Integer propertiesMode,
//                              Integer levelLine,
//                              Integer levelRow,
//                              Double levelDifficulty,
//                              String attackSkill,
//                              Integer enemyFindMode,
//                              Boolean isHandleCalcDistance,
//                              Double handleDistance) {
////        threadService.start(hwnd, version, propertiesMode, name);
//        DDTankCoreScript script = markHwndService.get(hwnd);
//        if (propertiesMode != null) {
//            if (propertiesMode == 0) {
//                script.setProperties(defaultProperties.clone());
//            } else {
//                script.setProperties(configService.getByIndex(propertiesMode - 1).clone());
//            }
//        }
//        if (name != null) {
//            script.setName(name);
//        }
//        DDTankCoreTaskProperties properties = script.getProperties();
//        if (levelLine != null) {
//            properties.setLevelLine(levelLine);
//        }
//        if (levelRow != null) {
//            properties.setLevelRow(levelRow);
//        }
//        if (levelDifficulty != null) {
//            properties.setLevelDifficulty(levelDifficulty);
//        }
//        if (attackSkill != null) {
//            properties.setAttackSkill(attackSkill);
//        }
//        if (enemyFindMode != null) {
//            properties.setEnemyFindMode(enemyFindMode);
//        }
//        if (isHandleCalcDistance != null) {
//            properties.setIsHandleCalcDistance(isHandleCalcDistance);
//        }
//        if (handleDistance != null && properties.getIsHandleCalcDistance()) {
//            properties.setHandleDistance(handleDistance);
//        }
//        return HttpResponse.auto(threadService.start(script));
//    }

    /**
     * 重启指定脚本
     */
    @PostMapping("/restart")
    public HttpResponse restart(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                                @RequestParam(name = "index", required = false) List<Integer> indexList) throws InterruptedException {
        return HttpDataResponse.ok(threadService.restart(getScripts(hwnds, indexList)));
    }


    /**
     * 使选中的脚本停止运行
     */
    @PostMapping("/stop")
    public HttpResponse stop(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                             @RequestParam(name = "index", required = false) List<Integer> indexList) {
        return HttpDataResponse.ok(threadService.stop(getScripts(hwnds, indexList)));
    }

    /**
     * 指定脚本重绑定操作，若脚本未启动则会直接启动
     */
    @PostMapping("/rebind")
    public HttpResponse rebind(@RequestParam(name = "hwnd", required = false) Long hwnd,
                               @RequestParam(name = "index", required = false) Integer index,
                               @RequestParam long newHwnd) {
        return HttpResponse.auto(threadService.rebind(getScript(hwnd, index), newHwnd));
    }

    /**
     * 移除指定脚本
     */
    @PostMapping("/remove")
    public HttpResponse remove(@RequestParam(name = "hwnd", required = false) List<Long> hwnds,
                               @RequestParam(name = "index", required = false) List<Integer> indexList) {
        if (hwnds != null) {
            for (Long hwnd : hwnds) {
                threadService.remove(hwnd);
            }
            return HttpResponse.ok();
        } else {
            return HttpDataResponse.ok(scriptService.removeByIndex(indexList));
        }
    }

    /**
     * 一键更新配置文件
     * @param hwnd 先根据hwnd从运行中的脚本中获取脚本，若未获取到则再从mark中获取，因此标记队列hwnd和脚本和hwnd不能发生冲突
     * @param propertiesMode 配置索引+1，若为0表示使用默认配置
     */
    @PutMapping("/config")
    public HttpResponse updateConfig(@RequestParam(required = false) Long hwnd,
                                 @RequestParam(required = false) Integer index,
                                 String name,
                                 Integer propertiesMode,
                                 Boolean suspend,
                                 Integer levelLine,
                                 Integer levelRow,
                                 Double levelDifficulty,
                                 String attackSkill,
                                 Integer enemyFindMode,
                                 Boolean isHandleCalcDistance,
                                 Double handleDistance) {
        DDTankCoreScript script = null;
        if (hwnd != null) {
            // 先从运行中的线程中获取Script，若未获取到再从markHwnd中获取Script
            script = threadService.get(hwnd);
            if (script == null) {
                script = markHwndService.get(hwnd);
            }
        } else if (index != null) {
            script = scriptService.getByIndex(index);
        }

        if(script == null) {
            return HttpResponse.auto(DDTankThreadResponseEnum.WINDOW_SCRIPT_IS_NOT_EXISTS);
        }

        if (propertiesMode != null) {
            if (propertiesMode == 0) {
                script.setProperties(defaultProperties.clone());
            } else {
                script.setProperties(configService.getByIndex(propertiesMode - 1).clone());
            }
        }

        if(suspend != null && suspend) {
            script.sendSuspend();
        }

        if (name != null) {
            script.setName(name);
        }
        DDTankCoreTaskProperties properties = script.getProperties();
        if (levelLine != null) {
            properties.setLevelLine(levelLine);
        }
        if (levelRow != null) {
            properties.setLevelRow(levelRow);
        }
        if (levelDifficulty != null) {
            properties.setLevelDifficulty(levelDifficulty);
        }
        if (attackSkill != null) {
            properties.setAttackSkill(attackSkill);
        }
        if (enemyFindMode != null) {
            properties.setEnemyFindMode(enemyFindMode);
        }
        if (isHandleCalcDistance != null) {
            properties.setIsHandleCalcDistance(isHandleCalcDistance);
        }
        if (handleDistance != null && properties.getIsHandleCalcDistance()) {
            properties.setHandleDistance(handleDistance);
        }
        return HttpResponse.auto(DDTankResponseEnum.OK);
    }
}