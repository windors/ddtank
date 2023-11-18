package cn.windor.ddtank.dto;

import cn.windor.ddtank.core.DDTankCoreThread;
import lombok.Getter;

public class StartedDDTankCoreThreadDTO {
    @Getter
    private final long hwnd;

    @Getter
    private final DDTankCoreThread coreThread;

    public StartedDDTankCoreThreadDTO(long hwnd, DDTankCoreThread coreThread) {
        this.hwnd = hwnd;
        this.coreThread = coreThread;
    }
}
