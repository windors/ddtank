package cn.windor.ddtank.controller;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.dto.HttpDataResponse;
import cn.windor.dto.HttpResponse;
import cn.windor.type.HttpResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.lang.reflect.Method;
import java.util.List;

@RestController
@RequestMapping("/util")
public class UtilController {

    @Autowired
    private DDTankThreadService threadService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankConfigProperties defaultProperties;

    @Autowired
    private Library dm;

    @PostMapping("/test")
    public HttpDataResponse<Object> test(@RequestParam String methodName,
                                         @RequestParam long hwnd) {
        DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if (thread == null) {
            return new HttpDataResponse(HttpResponseEnum.ILLEGAL_INPUT, null);
        }
        try {
            Method method = DDTankPic.class.getMethod(methodName);
            Object result = method.invoke(thread.getDdtankPic());
            return new HttpDataResponse(HttpResponseEnum.OK, result == null ? "null" : result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new HttpDataResponse(HttpResponseEnum.ERROR, e);
        }
    }

    @PostMapping("/suspend")
    public HttpDataResponse<Integer> suspendCoreThread(@RequestParam(name = "hwnd") List<Long> hwnds) {
        int success = 0;
        for (Long hwnd : hwnds) {
            DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
            if (thread == null) {
                continue;
            }
            thread.sendSuspend();
            success++;
        }
        return HttpDataResponse.ok(success);
    }

    @PostMapping("/continue")
    public HttpResponse continueCoreThread(@RequestParam(name = "hwnd") List<Long> hwnds) {
        int success = 0;
        for (Long hwnd : hwnds) {
            DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
            if (thread == null) {
                continue;
            }
            thread.sendContinue();
            success++;
        }
        return HttpDataResponse.ok(success);
    }

    /**
     * TODO 将返回值映射到前端
     *
     * @param hwnd
     * @return
     */
    @PostMapping("/state")
    public HttpDataResponse<CoreThreadStateEnum> getCoreThreadState(@RequestParam long hwnd) {
        DDTankCoreThread thread = threadService.getAllStartedThreadMap().get(hwnd);
        if (thread == null) {
            return HttpDataResponse.ok(CoreThreadStateEnum.NOT_STARTED);
        }
        return HttpDataResponse.ok(thread.getCoreState());
    }

    @PostMapping("/position")
    public HttpResponse capture(@RequestParam long hwnd) {
        dm.setWindowState(hwnd, 1);
        dm.setWindowState(hwnd, 7);
        return HttpResponse.ok();
    }

    @PostMapping("/start")
    public HttpResponse start(@RequestParam long hwnd,
                              @RequestParam String name,
                              @RequestParam String version,
                              @RequestParam int propertiesMode,
                              Integer levelLine,
                              Integer levelRow,
                              Double levelDifficulty,
                              String attackSkill,
                              Integer enemyFindMode,
                              Boolean isHandleCalcDistance,
                              Double handleDistance) {
//        threadService.start(hwnd, version, propertiesMode, name);
        DDTankConfigProperties startProperties;
        if (propertiesMode == 0) {
            startProperties = defaultProperties.clone();
        } else {
            startProperties = configService.getByIndex(propertiesMode - 1).clone();
        }
        if (levelLine != null) {
            startProperties.setLevelLine(levelLine);
        }
        if (levelRow != null) {
            startProperties.setLevelRow(levelRow);
        }
        if (levelDifficulty != null) {
            startProperties.setLevelDifficulty(levelDifficulty);
        }
        if (attackSkill != null) {
            startProperties.setAttackSkill(attackSkill);
        }
        if (enemyFindMode != null) {
            startProperties.setEnemyFindMode(enemyFindMode);
        }
        if (isHandleCalcDistance != null) {
            startProperties.setIsHandleCalcDistance(isHandleCalcDistance);
        }
        if (handleDistance != null) {
            startProperties.setHandleDistance(handleDistance);
        }
        return HttpResponse.auto(threadService.start(hwnd, version, name, startProperties));
    }

    @PostMapping("/restart")
    public HttpResponse restart(@RequestParam(name = "hwnd") List<Long> hwnds) {
        for (Long hwnd : hwnds) {
            threadService.restart(hwnd);
        }
        return HttpResponse.ok();
    }

    @PostMapping("/stop")
    public HttpResponse stop(@RequestParam(name = "hwnd") List<Long> hwnds) {
        for (Long hwnd : hwnds) {
            threadService.stop(hwnd);
        }
        return HttpResponse.ok();
    }

    @PostMapping("/remove")
    public HttpResponse remove(@RequestParam(name = "hwnd") List<Long> hwnds) {
        for (Long hwnd : hwnds) {
            threadService.remove(hwnd);
        }
        return HttpResponse.ok();
    }
}
