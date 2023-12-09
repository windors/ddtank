package cn.windor.ddtank.dto;

import cn.windor.ddtank.core.DDTankCoreScript;
import lombok.Getter;

public class ScriptDDTankCoreThreadDTO {
    @Getter
    private final int index;

    @Getter
    private final DDTankCoreScript coreThread;

    public ScriptDDTankCoreThreadDTO(int index, DDTankCoreScript coreThread) {
        this.index = index;
        this.coreThread = coreThread;
    }
}
