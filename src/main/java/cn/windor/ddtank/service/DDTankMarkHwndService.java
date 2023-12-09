package cn.windor.ddtank.service;

import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.DDTankCoreScript;

import java.util.Map;

public interface DDTankMarkHwndService {

    /**
     * 将当前鼠标所指向的窗口计入待启动列表
     * @return 脚本句柄，返回null表示当前窗口不符合脚本要求
     */
    Long mark();

    Long getLegalHwnd(long hwnd);

    DDTankCoreScript removeByHwnd(long hwnd);

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
     * 获取所有等待调用start的脚本
     */
    Map<Long, DDTankCoreScript> getWaitStartMap();

    DDTankCoreScript get(long hwnd);
}
