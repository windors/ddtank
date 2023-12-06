package cn.windor.ddtank.dto;

import cn.windor.ddtank.core.DDTankCoreThread;
import lombok.Getter;

public class ScriptDDTankCoreThreadDTO {
    @Getter
    private final int index;

    @Getter
    private final DDTankCoreThread coreThread;

    public ScriptDDTankCoreThreadDTO(int index, DDTankCoreThread coreThread) {
        this.index = index;
        this.coreThread = coreThread;
    }
}
