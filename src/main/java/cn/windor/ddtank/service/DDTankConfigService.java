package cn.windor.ddtank.service;

import cn.windor.ddtank.config.DDTankConfigProperties;

public interface DDTankConfigService {
    boolean saveDefaultConfig(DDTankConfigProperties newDefaultConfig);
}
