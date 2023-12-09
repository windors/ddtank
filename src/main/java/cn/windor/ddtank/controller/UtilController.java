package cn.windor.ddtank.controller;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.core.impl.DDTankCoreAttackHandlerImpl;
import cn.windor.ddtank.mapper.DDTankConfigMapper;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankMarkHwndService;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.ddtank.type.CoreThreadStateEnum;
import cn.windor.ddtank.util.FileUtils;
import cn.windor.dto.HttpDataResponse;
import cn.windor.dto.HttpResponse;
import cn.windor.type.HttpResponseEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/util")
public class UtilController {

    @Autowired
    private DDTankThreadService threadService;

    @Autowired
    private DDTankMarkHwndService markHwndService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankCoreTaskProperties defaultProperties;

    @Autowired
    private Library dm;

    @PostMapping("/test")
    public HttpDataResponse<Object> test(@RequestParam String methodName,
                                         @RequestParam long hwnd) {
        DDTankCoreScript thread = threadService.get(hwnd);
        if (thread == null) {
            return new HttpDataResponse(HttpResponseEnum.ILLEGAL_INPUT, null);
        }
        thread.refreshPic();
        try {
            Method method = DDTankPic.class.getMethod(methodName);
            Object result = method.invoke(thread.getDdtankPic());
            return new HttpDataResponse(HttpResponseEnum.OK, result == null ? "null" : result.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return new HttpDataResponse(HttpResponseEnum.ERROR, e);
        }
    }

    @GetMapping("/screenshot")
    public StreamingResponseBody getScreenshot(@RequestParam long hwnd) {
        DDTankCoreScript script = threadService.get(hwnd);
        if (script == null) {
            return null;
        }
        String path = DDTankFileConfigProperties.getScreenshotPath();
        try {
            script.screenshot(path);
        }catch (IllegalStateException ignore) {
            return outputStream -> {

            };
        }
        // 将文件返回给前端
        return outputStream -> {
            FileCopyUtils.copy(Files.newInputStream(new File(path).toPath()), outputStream);
        };
    }

    @PostMapping("/suspend")
    public HttpDataResponse<Integer> suspendCoreThread(@RequestParam(name = "hwnd") List<Long> hwnds) {
        int success = 0;
        for (Long hwnd : hwnds) {
            DDTankCoreScript thread = threadService.get(hwnd);
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
            DDTankCoreScript thread = threadService.get(hwnd);
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
        DDTankCoreScript thread = threadService.get(hwnd);
        if (thread == null) {
            return HttpDataResponse.ok(CoreThreadStateEnum.NOT_STARTED);
        }
        return HttpDataResponse.ok(thread.getCoreState());
    }

    @PostMapping("/start")
    public HttpResponse start(@RequestParam long hwnd,
                              String name,
                              Integer propertiesMode,
                              Integer levelLine,
                              Integer levelRow,
                              Double levelDifficulty,
                              String attackSkill,
                              Integer enemyFindMode,
                              Boolean isHandleCalcDistance,
                              Double handleDistance) {
//        threadService.start(hwnd, version, propertiesMode, name);
        DDTankCoreScript script = markHwndService.get(hwnd);
        if(propertiesMode != null) {
            if (propertiesMode == 0) {
                script.setProperties(defaultProperties.clone());
            } else {
                script.setProperties(configService.getByIndex(propertiesMode - 1).clone());
            }
        }
        if(name != null) {
            script.setName(name);
        }
        DDTankCoreTaskProperties properties = script.getProperties();
        if(levelLine != null) {
            properties.setLevelLine(levelLine);
        }
        if(levelRow != null) {
            properties.setLevelRow(levelRow);
        }
        if(levelDifficulty != null) {
            properties.setLevelDifficulty(levelDifficulty);
        }
        if(attackSkill != null) {
            properties.setAttackSkill(attackSkill);
        }
        if(enemyFindMode != null) {
            properties.setEnemyFindMode(enemyFindMode);
        }
        if(isHandleCalcDistance != null) {
            properties.setIsHandleCalcDistance(isHandleCalcDistance);
        }
        if(handleDistance != null && properties.getIsHandleCalcDistance()) {
            properties.setHandleDistance(handleDistance);
        }
        return HttpResponse.auto(threadService.start(script));
    }

    @PostMapping("/restart")
    public HttpResponse restart(@RequestParam(name = "hwnd") List<Long> hwnds) throws InterruptedException {
        threadService.restart(hwnds);
        return HttpResponse.ok();
    }

    @PostMapping("/stop")
    public HttpResponse stop(@RequestParam(name = "hwnd") List<Long> hwnds) throws InterruptedException {
        threadService.stop(hwnds);
        return HttpResponse.ok();
    }

    @PostMapping("/rebind")
    public HttpResponse rebind(@RequestParam(name = "hwnd") long hwnd,
                               @RequestParam long newHwnd) {
        return HttpResponse.auto(threadService.rebind(hwnd, newHwnd));
    }

    @PostMapping("/remove")
    public HttpResponse remove(@RequestParam(name = "hwnd") List<Long> hwnds) {
        for (Long hwnd : hwnds) {
            threadService.remove(hwnd);
        }
        return HttpResponse.ok();
    }

    @PostMapping("/refreshPic")
    public HttpResponse refreshPic(long hwnd) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        return HttpResponse.auto(coreThread.refreshPic());
    }

    @PostMapping("/rename")
    public HttpResponse rename(long hwnd, String newName) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        coreThread.setName(newName);
        return HttpResponse.ok();
    }

    @PostMapping("/strength")
    public HttpResponse saveStrength() {
        Map<String, Double> calcedMap = DDTankCoreAttackHandlerImpl.getCalcedMap();
        FileUtils.writeObject(calcedMap, DDTankConfigMapper.getDDTankStrengthFile());
        return HttpResponse.ok();
    }

    @PostMapping("/strength/load")
    public HttpResponse loadStrength() throws IOException, ClassNotFoundException {
        File file = DDTankConfigMapper.getDDTankStrengthFile();
        if(!file.exists()) {
            return HttpResponse.auto(false);
        }
        Map<String, Double> calcedMap = (Map<String, Double>) FileUtils.readSeriaizedObject(file);
        DDTankCoreAttackHandlerImpl.setCalcedMap(calcedMap);
        return HttpResponse.ok();
    }
}
