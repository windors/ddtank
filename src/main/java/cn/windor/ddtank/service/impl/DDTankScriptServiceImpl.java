package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.mapper.DDTankScriptMapper;
import cn.windor.ddtank.service.DDTankScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Service
public class DDTankScriptServiceImpl implements DDTankScriptService {

    @Autowired
    private DDTankScriptMapper ddTankScriptMapper;

    /**
     * 记录哪些脚本已经在首页，
     */
    private static final Map<DDTankCoreThread, Boolean> runScripts = new ConcurrentHashMap<>();

    @Override
    public DDTankCoreThread add(String name, String version, boolean needCorrect, DDTankConfigProperties properties) {
        DDTankStartParam startParam = new DDTankStartParam(needCorrect);
        startParam.setName(name);
        DDTankCoreThread coreThread = new DDTankCoreThread(-1, version, properties, startParam);
        ddTankScriptMapper.add(coreThread);
        return coreThread;
    }

    @Override
    public List<DDTankCoreThread> list() {
        return ddTankScriptMapper.list();
    }

    @Override
    public DDTankCoreThread getByIndex(int index) {
        return ddTankScriptMapper.getByIndex(index);
    }

    @Override
    public int start(List<Integer> indexList) {
        int success = 0;
        for (Integer index : indexList) {
            DDTankCoreThread script = getByIndex(index);
            // 直接启动
            success++;
        }
        return success;
    }
}