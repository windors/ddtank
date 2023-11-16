package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.core.DDTankCoreTask;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.service.DDTankDetailService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DDTankDetailServiceImpl implements DDTankDetailService {
    private final Map<Long, DDTankCoreThread> threadMap;

    public DDTankDetailServiceImpl(DDTankThreadService ddTankThreadService) {
        this.threadMap = ddTankThreadService.getAllStartedThreadMap();
    }

    public boolean setTaskAutoComplete(long hwnd, int taskAutoComplete) {
        if(!check(hwnd)) {
            return false;
        }
        DDTankCoreThread thread = threadMap.get(hwnd);
        DDTankCoreTask task = thread.getTask();
        task.setTaskAutoComplete(taskAutoComplete);
        return true;
    }

    @Override
    public boolean setAutoUseProp(long hwnd, int autoUseProp) {
        if(!check(hwnd)) {
            return false;
        }

        DDTankCoreThread thread = threadMap.get(hwnd);
        DDTankCoreTask task = thread.getTask();
        task.setAutoUseProp(autoUseProp);
        return true;
    }

    private boolean check(long hwnd) {
        DDTankCoreThread thread = threadMap.get(hwnd);
        if(thread == null) {
            return false;
        }
        return true;
    }
}
