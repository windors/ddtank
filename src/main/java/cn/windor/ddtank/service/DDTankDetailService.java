package cn.windor.ddtank.service;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.entity.LevelRule;

public interface DDTankDetailService {
    boolean setTaskAutoComplete(DDTankCoreScript coreThread, int taskAutoComplete);

    boolean setAutoUseProp(DDTankCoreScript coreThread, int autoUseProp);

    boolean setAutoReconnect(DDTankCoreScript coreThread, String username, String password);

    boolean addRule(DDTankCoreScript coreThread, LevelRule levelRule);

    boolean removeRule(DDTankCoreScript coreThread, int index);
}
