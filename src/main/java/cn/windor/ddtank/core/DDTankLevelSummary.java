package cn.windor.ddtank.core;

import java.io.Serializable;
import java.util.Map;

public interface DDTankLevelSummary extends Serializable {
    /**
     * 副本通关后会调用该接口
     */
    void summary();

    Map<DDTankLevel, Integer> getSummary();
}
