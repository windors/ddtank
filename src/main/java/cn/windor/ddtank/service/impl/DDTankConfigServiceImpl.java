package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.mapper.DDTankConfigMapper;
import cn.windor.ddtank.service.DDTankConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DDTankConfigServiceImpl implements DDTankConfigService {


    @Autowired
    private DDTankCoreTaskProperties config;

    @Autowired
    private DDTankConfigMapper mapper;

    @Override
    public boolean saveDefaultConfig(DDTankCoreTaskProperties newDefaultConfig) {
        config.update(newDefaultConfig);
        return mapper.saveDefaultConfig(config);
    }

    @Override
    public List<DDTankCoreTaskProperties> list() {
        return mapper.list();
    }

    @Override
    public DDTankCoreTaskProperties removeByIndex(int index) {
        return mapper.removeByIndex(index);
    }

    @Override
    public DDTankCoreTaskProperties getByIndex(int index) {
        return mapper.getByIndex(index);
    }

    @Override
    public boolean add(DDTankCoreTaskProperties properties) {
        return mapper.add(properties);
    }

    @Override
    public boolean update(int index, DDTankCoreTaskProperties properties) {
        return mapper.updateByIndex(index, properties);
    }
}
