package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.impl.DDTankCoreAttackHandlerImpl;
import cn.windor.ddtank.dto.HttpResponse;
import cn.windor.ddtank.mapper.DDTankConfigMapper;
import cn.windor.ddtank.service.DDTankThreadService;
import cn.windor.ddtank.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

@RestController
@RequestMapping("/util")
public class UtilController {

    @Autowired
    private DDTankThreadService threadService;

    @PostMapping("/test")
    public HttpResponse test(@RequestParam String methodName,
                                         @RequestParam long hwnd) {
//        DDTankCoreScript thread = threadService.get(hwnd);
//        if (thread == null) {
//            return new HttpResponse(DDTankHttpResponseEnum.PARAM_LOST, "未找到窗口[" + hwnd + "]所绑定的脚本");
//        }
//        thread.refreshPic();
//        try {
//            Method method = DDTankPic.class.getMethod(methodName);
//            Object result = method.invoke(thread.getDdtankPic());
//            return new HttpDataResponse(DDTankHttpResponseEnum.OK, result == null ? "null" : result.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new HttpDataResponse(DDTankHttpResponseEnum.ERROR, e);
//        }
        return HttpResponse.notDev();
    }

    // TODO 待修改逻辑
    @GetMapping("/screenshot")
    public StreamingResponseBody getScreenshot(@RequestParam long hwnd) {
        DDTankCoreScript script = threadService.get(hwnd);
        if (script == null) {
            return null;
        }
        String path = DDTankFileConfigProperties.getScreenshotPath();
        try {
            script.screenshot(path);
        }catch (Exception ignore) {
            return outputStream -> {

            };
        }
        // 将文件返回给前端
        return outputStream -> {
            FileCopyUtils.copy(Files.newInputStream(new File(path).toPath()), outputStream);
        };
    }

    /**
     * 刷新图片缓存
     * @param hwnd
     * @return
     */
    @PostMapping("/refreshPic")
    public HttpResponse refreshPic(long hwnd) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        return HttpResponse.auto(coreThread.refreshPic());
    }

    /**
     * 重命名脚本
     */
    @PostMapping("/rename")
    public HttpResponse rename(long hwnd, String newName) {
        DDTankCoreScript coreThread = threadService.get(hwnd);
        coreThread.setName(newName);
        return HttpResponse.ok();
    }

    /**
     * 保存力度缓存
     */
    @PostMapping("/strength")
    public HttpResponse saveStrength() {
        Map<String, Double> calcedMap = DDTankCoreAttackHandlerImpl.getCalcedMap();
        FileUtils.writeObject(calcedMap, DDTankConfigMapper.getDDTankStrengthFile());
        return HttpResponse.ok();
    }

    /**
     * 从硬盘中获取力度表缓存
     */
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
