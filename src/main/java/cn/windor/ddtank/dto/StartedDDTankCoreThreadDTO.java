package cn.windor.ddtank.dto;

import cn.windor.ddtank.core.DDTankCoreScript;
import lombok.Getter;

public class StartedDDTankCoreThreadDTO {
    @Getter
    private final long hwnd;

    @Getter
    private final DDTankCoreScript script;

    public StartedDDTankCoreThreadDTO(long hwnd, DDTankCoreScript script) {
        this.hwnd = hwnd;
        this.script = script;
    }
}
