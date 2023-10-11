package cn.windor.ddtank.service;

import cn.windor.ddtank.core.DDtankCoreThread;

import java.util.Map;

public interface DDtankThreadService {

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

    Map<Long, DDtankCoreThread> getAllStartedThreadMap();
}