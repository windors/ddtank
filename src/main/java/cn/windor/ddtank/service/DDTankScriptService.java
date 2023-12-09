package cn.windor.ddtank.service;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreScriptThread;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;

import java.util.List;

public interface DDTankScriptService {
    DDTankCoreScript add(String name, boolean needCorrect, DDTankCoreTaskProperties properties);

    List<DDTankCoreScript> list();

    DDTankCoreScript getByIndex(int index);

    boolean addOrUpdate(DDTankCoreScript script);
}
