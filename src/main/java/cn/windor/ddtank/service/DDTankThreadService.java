package cn.windor.ddtank.service;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreScriptThread;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.dto.Response;
import cn.windor.ddtank.entity.LevelRule;

import java.util.List;
import java.util.Map;

public interface DDTankThreadService {
    /**
     * 指定配置并运行脚本
     */
    Map<DDTankCoreScript, Response> start(List<DDTankCoreScript> coreScript);

    /**
     * 停止指定脚本
     */
    Map<DDTankCoreScript, Response> stop(List<DDTankCoreScript> scripts) throws InterruptedException;

    /**
     * 更新指定脚本的配置
     */
    Response updateProperties(long hwnd, DDTankCoreTaskProperties config);


    /**
     * 重启脚本
     * @param hwnds
     * @return
     * @throws InterruptedException
     */
    Map<DDTankCoreScript, Response> restart(List<DDTankCoreScript> hwnds) throws InterruptedException;

    /**
     * 移除脚本
     * @param hwnd
     * @return
     */
    Response remove(long hwnd);

    /**
     * 手动重绑定
     * @param hwnd
     * @param newHwnd
     * @return
     */
    Response rebind(DDTankCoreScript script, long newHwnd);

    DDTankCoreScript get(long hwnd);

    Map<Long, DDTankCoreScript> getAllStartedScriptMap();

    DDTankCoreScriptThread getThread(long hwnd);

    boolean isRunning(DDTankCoreScript script);
}