package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.core.DDTankOperate;
import cn.windor.ddtank.entity.LevelRule;
import cn.windor.ddtank.handler.DDTankSelectMapHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DDTankSelectMapHandlerImpl implements DDTankSelectMapHandler {


    private List<LevelRule> levelRules = new CopyOnWriteArrayList<>();

    private final DDTankConfigProperties properties;
    private DDTankOperate ddtankOperate;
    private DDTankLog ddtLog;

    public DDTankSelectMapHandlerImpl(DDTankConfigProperties properties, DDTankOperate ddtankOperate, DDTankLog ddtLog) {
        this.properties = properties;
        this.ddtankOperate = ddtankOperate;
        this.ddtLog = ddtLog;
    }

    @Override
    public boolean update(Object... complexObject) {
        boolean success = true;
        for (Object param : complexObject) {
            if(param instanceof DDTankOperate) {
                this.ddtankOperate = (DDTankOperate) param;
                continue;
            }
            success = false;
        }
        return success;
    }

    @Override
    public void select(int passed) {
        LevelRule old = new LevelRule(properties.getLevelLine(), properties.getLevelRow(), passed, properties.getLevelDifficulty());
        for (LevelRule levelRule : levelRules) {
            // 最终后面的会将前面的替换掉
            if(passed >= levelRule.passed) {
                properties.setLevelLine(levelRule.levelLine);
                properties.setLevelRow(levelRule.levelRow);
                properties.setLevelDifficulty(levelRule.levelDifficulty);
            }
        }
        if(old.levelLine != properties.getLevelLine() || old.levelRow != properties.getLevelRow() || old.levelDifficulty != properties.getLevelDifficulty()) {
            log("自动执行副本切换任务：" + old.levelLine + "-" + old.levelRow + ", 难度:" + old.levelDifficulty + " -> " +
                    properties.getLevelLine() + "-" + properties.getLevelRow() + ", 难度:" + properties.getLevelDifficulty());
        }
        log("选择副本：" + properties.getLevelLine() + "行" + properties.getLevelRow() + "列");
        ddtankOperate.chooseMap();
    }

    @Override
    public boolean addRule(LevelRule rule) {
        levelRules.add(rule);
        levelRules.sort(Comparator.comparingInt(r -> r.passed));
        return true;
    }

    @Override
    public boolean removeRule(int index) {
        LevelRule remove = levelRules.remove(index);
        return remove != null;
    }

    @Override
    public List<LevelRule> getRules() {
        return levelRules;
    }

    public void log(String msg) {
        ddtLog.info(msg);
        log.debug(msg);
    }
}
