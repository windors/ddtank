package cn.windor.ddtank.service;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankCoreThread;

import java.util.List;

public interface DDTankScriptService {
    DDTankCoreThread add(String name, String version, boolean needCorrect, DDTankConfigProperties properties);

    List<DDTankCoreThread> list();

    public DDTankCoreThread getByIndex(int index);

    int start(List<Integer> indexList);
}
