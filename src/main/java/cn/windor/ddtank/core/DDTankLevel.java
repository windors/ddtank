package cn.windor.ddtank.core;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * index
 */

public class DDTankLevel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Getter
    // 副本模式值
    private final double mode;

    @Getter
    // 副本行数
    private final int row;

    @Getter
    // 副本列数
    private final int line;

    @Getter
    // 难度值
    private final double difficulty;

    @Getter
    private final LocalDateTime time;

    private static final Map<String, DDTankLevel> instanceMap = new HashMap<>();

    private DDTankLevel(double mode, int row, int line, double difficulty) {
        this.mode = mode;
        this.row = row;
        this.line = line;
        this.difficulty = difficulty;
        this.time = LocalDateTime.now();
    }

    public static DDTankLevel getInstance(DDTankCoreTaskProperties properties) {
        DDTankLevel result;
        String key = getKey(properties);
        if ((result = instanceMap.get(key)) == null) {
            synchronized (DDTankLevel.class) {
                if ((result = instanceMap.get(getKey(properties))) == null) {
                    result = new DDTankLevel(properties.getLevelMode(), properties.getLevelRow(), properties.getLevelLine(), properties.getLevelDifficulty());
                    instanceMap.put(key, result);
                }
            }
        }
        return result;
    }

    private static String getKey(DDTankCoreTaskProperties properties) {
        return String.valueOf(Math.ceil(properties.getLevelMode())) + properties.getLevelLine() + properties.getLevelRow() + Math.ceil(properties.getLevelDifficulty());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DDTankLevel that = (DDTankLevel) o;
        return Math.ceil(this.mode) == Math.ceil(that.mode) && this.row == that.row && this.line == that.line && Math.ceil(this.difficulty) == Math.ceil(that.difficulty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, row, line, difficulty);
    }
}
