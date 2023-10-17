package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.config.DDTankConfigProperties;
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
    private DDTankConfigProperties config;

    @Autowired
    private DDTankConfigMapper mapper;

    @Override
    public boolean saveDefaultConfig(DDTankConfigProperties newDefaultConfig) {
        config.update(newDefaultConfig);
        return mapper.saveDefaultConfig(config);
    }

    @Override
    public List<DDTankConfigProperties> list() {
        return mapper.list();
    }

    @Override
    public DDTankConfigProperties removeByIndex(int index) {
        return mapper.removeByIndex(index);
    }

    @Override
    public DDTankConfigProperties getByIndex(int index) {
        return mapper.getByIndex(index);
    }

    @Override
    public boolean add(DDTankConfigProperties properties) {
        return mapper.add(properties);
    }

    @Override
    public boolean update(int index, DDTankConfigProperties properties) {
        return mapper.updateByIndex(index, properties);
    }
}
