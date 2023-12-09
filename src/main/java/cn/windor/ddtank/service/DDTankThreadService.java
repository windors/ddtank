package cn.windor.ddtank.service;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreScriptThread;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.entity.LevelRule;

import java.util.List;
import java.util.Map;

public interface DDTankThreadService {
    /**
     * 指定配置并运行脚本
     */
    boolean start(DDTankCoreScript coreScript);

    boolean start(DDTankCoreScriptThread coreScriptThread);

    /**
     * 停止指定脚本
     */
    int stop(List<Long> hwnds) throws InterruptedException;

    /**
     * 更新指定脚本的配置
     */
    boolean updateProperties(long hwnd, DDTankCoreTaskProperties config);


    /**
     * 重启脚本
     * @param hwnds
     * @return
     * @throws InterruptedException
     */
    int restart(List<Long> hwnds) throws InterruptedException;

    /**
     * 移除脚本
     * @param hwnd
     * @return
     */
    boolean remove(long hwnd);

    /**
     * 手动重绑定
     * @param hwnd
     * @param newHwnd
     * @return
     */
    boolean rebind(long hwnd, long newHwnd);

    boolean addRule(long hwnd, LevelRule rule);

    boolean removeRule(long hwnd, int index);

    boolean setAutoReconnect(DDTankCoreScript thread, String username, String password);

    DDTankCoreScript get(long hwnd);

    Map<Long, DDTankCoreScript> getAllStartedScriptMap();

    DDTankCoreScriptThread getThread(long hwnd);

    boolean isRunning(DDTankCoreScript script);
}