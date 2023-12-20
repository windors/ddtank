package cn.windor.ddtank.controller;

import cn.windor.ddtank.core.DDTankCoreScriptThread;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.dto.DDTankHttpResponseEnum;
import cn.windor.ddtank.dto.HttpDataResponse;
import cn.windor.ddtank.dto.HttpResponse;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.service.DDTankScriptService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/script")
public class ScriptController {

    @Autowired
    private DDTankScriptService scriptService;

    @Autowired
    private DDTankConfigService configService;

    @Autowired
    private DDTankThreadService ddTankThreadService;

    @Autowired
    private DDTankCoreTaskProperties defaultProperties;

    /**
     * 添加脚本（直接序列化版）
     * @param name 脚本名称
     * @param needCorrect 可选值，表示脚本是否需要矫正
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
        if(needCorrect == null) {
            needCorrect = false;
        }
        DDTankCoreScript coreThread = scriptService.add(name, needCorrect, startProperties);
        return HttpResponse.auto(coreThread != null);
    }

    /**
     * 根据句柄从首页脚本列表中获取脚本进行序列化保存
     * TODO 改完之后明天计划加一个重绑定链，这样前端没刷新的情况下调用不会出现传参异常了
     * @param hwnd 句柄值
     * @return
     */
    @PostMapping("/add/{hwnd}")
    public HttpResponse addScriptByHwnd(@PathVariable long hwnd) {
        DDTankCoreScript script = ddTankThreadService.get(hwnd);
        if(script == null) {
            return HttpResponse.err(DDTankHttpResponseEnum.PARAM_LOST, "未找到窗口[" + hwnd + "]所绑定的脚本");
        }
        scriptService.addOrUpdate(script);
        return HttpResponse.ok();
    }

    /**
     * 将序列化的脚本启动
     * TODO 已序列化的脚本调用启动方法时需要优化窗口失效的情况：窗口失效时，key要取一个负数向后排以区分脚本，防止出现两个脚本hwnd值相同的情况下程序认为窗口已绑定，进而无法启动脚本。
     * TODO 优化为二合一接口
     * @param indexList 要启动的脚本
     * @return 成功启动脚本的个数
     */
    @PostMapping("/start")
    public HttpResponse startScripts(@RequestParam(name = "index") List<Integer> indexList) {
        List<DDTankCoreScript> scripts = new ArrayList<>(indexList.size());
        for (Integer index : indexList) {
            scripts.add(scriptService.getByIndex(index));
        }
        int success = 0;
        for (DDTankCoreScript script : scripts) {
            if(ddTankThreadService.start(script)) {
                success++;
            }
        }
        return HttpDataResponse.ok(success);
    }

    /**
     * 移除已序列化的脚本
     * @param indexList 待移除的脚本索引
     * @return 成功移除的脚本个数
     */
    @PostMapping("/remove")
    public HttpResponse removeScripts(@RequestParam(name = "index") List<Integer> indexList) {
        return HttpDataResponse.ok(scriptService.removeByIndex(indexList));
    }
}
