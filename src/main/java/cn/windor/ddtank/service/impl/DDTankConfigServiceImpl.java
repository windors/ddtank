package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.service.DDTankConfigService;
import cn.windor.ddtank.util.DDTankFileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DDTankConfigServiceImpl implements DDTankConfigService {

    @Autowired
    private DDTankConfigProperties config;

    @Override
    public boolean saveDefaultConfig(DDTankConfigProperties newDefaultConfig) {
        config.update(newDefaultConfig);
        return DDTankFileUtils.writeDefaultConfig(config);
    }
}
