package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.core.DDTankCoreTask;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.service.DDTankDetailService;
import cn.windor.ddtank.service.DDTankThreadService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DDTankDetailServiceImpl implements DDTankDetailService {
    public boolean setTaskAutoComplete(DDTankCoreThread coreThread, int taskAutoComplete) {
        if(coreThread == null) {
            return false;
        }
        DDTankCoreTask task = coreThread.getTask();
        task.setTaskAutoComplete(taskAutoComplete);
        return true;
    }

    @Override
    public boolean setAutoUseProp(DDTankCoreThread coreThread, int autoUseProp) {
        if(coreThread == null) {
            return false;
        }
        DDTankCoreTask task = coreThread.getTask();
        task.setAutoUseProp(autoUseProp);
        return true;
    }

    @Override
    public boolean setAutoReconnect(DDTankCoreThread coreThread, String username, String password) {
        if(coreThread == null) {
            return false;
        }
        coreThread.setAutoReconnect(username, password);
        return true;
    }

    @Override
    public boolean addRule(DDTankCoreThread coreThread, LevelRule levelRule) {
        if(coreThread == null) {
            return false;
        }
        coreThread.addRule(levelRule);
        return true;
    }

    @Override
    public boolean removeRule(DDTankCoreThread coreThread, int index) {
        if(coreThread == null) {
            return false;
        }
        coreThread.removeRule(index);
        return true;
    }
}
