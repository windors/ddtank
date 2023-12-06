package cn.windor.ddtank.service;

import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.entity.LevelRule;

public interface DDTankDetailService {
    boolean setTaskAutoComplete(DDTankCoreThread coreThread, int taskAutoComplete);

    boolean setAutoUseProp(DDTankCoreThread coreThread, int autoUseProp);

    boolean setAutoReconnect(DDTankCoreThread coreThread, String username, String password);

    boolean addRule(DDTankCoreThread coreThread, LevelRule levelRule);

    boolean removeRule(DDTankCoreThread coreThread, int index);
}
