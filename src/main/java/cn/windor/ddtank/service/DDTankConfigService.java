package cn.windor.ddtank.service;

import cn.windor.ddtank.config.DDTankConfigProperties;

import java.util.List;

public interface DDTankConfigService {
    boolean saveDefaultConfig(DDTankConfigProperties newDefaultConfig);

    List<DDTankConfigProperties> list();


    DDTankConfigProperties removeByIndex(int index);

    DDTankConfigProperties getByIndex(int index);

    boolean add(DDTankConfigProperties properties);

    boolean update(int index, DDTankConfigProperties properties);
}
