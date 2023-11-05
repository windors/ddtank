package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.exception.IllegalDDTankHwndException;
import cn.windor.ddtank.handler.HwndMarkHandler;

import java.util.Map;

public class HwndMarkHandlerImpl implements HwndMarkHandler {

    private final Library dm;

    public HwndMarkHandlerImpl(Library dm) {
        this.dm = dm;
    }

    @Override
    public boolean isLegalHwnd(long hwnd) {
        String className = dm.getWindowClass(hwnd);
        return "MacromediaFlashPlayerActiveX".equals(className)
                // 360浏览器/qq浏览器极速模式
                || "Chrome_RenderWidgetHostHWND".equals(className)
                // 360游戏大厅前台模式
                || "NativeWindowClass".equals(className);
    }

    @Override
    public long getLegalHwnd(long hwnd) {
        String className = dm.getWindowClass(hwnd);
        if ("MacromediaFlashPlayerActiveX".equals(className) || "NativeWindowClass".equals(className)) {
            return hwnd;
        } else {
            hwnd = dm.getWindow(hwnd, 7);
        }
        return hwnd;
    }
}