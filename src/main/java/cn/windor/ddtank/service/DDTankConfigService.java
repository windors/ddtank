package cn.windor.ddtank.service;

import cn.windor.ddtank.core.DDTankCoreTaskProperties;

import java.util.List;

public interface DDTankConfigService {
    boolean saveDefaultConfig(DDTankCoreTaskProperties newDefaultConfig);

    List<DDTankCoreTaskProperties> list();


    DDTankCoreTaskProperties removeByIndex(int index);

    DDTankCoreTaskProperties getByIndex(int index);

    boolean add(DDTankCoreTaskProperties properties);

    boolean update(int index, DDTankCoreTaskProperties properties);
}
