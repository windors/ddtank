package cn.windor.ddtank.service.impl;

import cn.windor.ddtank.core.DDTankCoreScript;
import cn.windor.ddtank.core.DDTankCoreScriptThread;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.config.DDTankStartParam;
import cn.windor.ddtank.mapper.DDTankScriptMapper;
import cn.windor.ddtank.service.DDTankScriptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DDTankScriptServiceImpl implements DDTankScriptService {

    @Autowired
    private DDTankScriptMapper ddTankScriptMapper;

    @Override
    public DDTankCoreScript add(String name, boolean needCorrect, DDTankCoreTaskProperties properties) {
        DDTankCoreScript coreThread = new DDTankCoreScript(-1, name, properties, needCorrect);
        ddTankScriptMapper.add(coreThread);
        return coreThread;
    }

    @Override
    public List<DDTankCoreScript> list() {
        return ddTankScriptMapper.list();
    }

    @Override
    public DDTankCoreScript getByIndex(int index) {
        return ddTankScriptMapper.getByIndex(index);
    }

    @Override
    public boolean removeByIndex(List<Integer> indexList) {
        // 倒序删除
        indexList.sort((a, b) -> b - a);
        for (Integer index : indexList) {
            ddTankScriptMapper.removeByIndex(index);
        }
        return false;
    }


    @Override
    public boolean addOrUpdate(DDTankCoreScript script) {
        if(script == null) {
            return false;
        }
        ddTankScriptMapper.addOrUpdate(script);
        return true;
    }
}