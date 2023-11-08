package cn.windor.ddtank.service;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.entity.LevelRule;

import java.util.Map;

public interface DDTankThreadService {

    /**
     * 改变启动快捷键
     * @param modifier
     * @param keycode
     */
    void changeStartShortcut(int modifier, char keycode);

    /**
     * 移除启动快捷键
     */
    void removeStartShortcut();

    /**
     * 获取所有已经启动的脚本
     */
    Map<Long, DDTankCoreThread> getAllStartedThreadMap();

    /**
     * 获取所有等待调用start的脚本
     */
    Map<Long, DDTankStartParam> getWaitStartMap();

    /**
     * 指定配置并运行脚本
     */
    boolean start(long hwnd, String version, String name, DDTankConfigProperties startProperties);

    /**
     * 将当前鼠标所指向的窗口计入待启动列表
     * @return 脚本句柄，返回null表示当前窗口不符合脚本要求
     */
    Long mark();

    /**
     * 停止指定脚本
     */
    void stop(long hwnd);

    /**
     * 更新指定脚本的配置
     */
    boolean updateProperties(long hwnd, DDTankConfigProperties config);

    /**
     * 重启脚本
     * @param hwnd
     * @return
     */
    boolean restart(long hwnd);

    /**
     * 移除脚本
     * @param hwnd
     * @return
     */
    boolean remove(long hwnd);

    /**
     * 重绑定
     * @param hwnd
     * @param newHwnd
     * @return
     */
    boolean rebind(long hwnd, long newHwnd);

    boolean addRule(long hwnd, LevelRule rule);

    boolean removeRule(long hwnd, int index);
}