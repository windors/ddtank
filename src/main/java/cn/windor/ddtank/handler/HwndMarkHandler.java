package cn.windor.ddtank.handler;

import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.exception.IllegalDDTankHwndException;

import java.util.Map;

public interface HwndMarkHandler {

    /**
     * 检测hwnd是否合法
     * @param hwnd
     * @return
     */
    boolean isLegalHwnd(long hwnd);


    /**
     * 根据
     * @param hwnd 指针获取到的窗口句柄
     * @return 有效的句柄（可以截的到图的）
     */
    long getLegalHwnd(long hwnd);
}