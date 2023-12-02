package cn.windor.ddtank.mapper;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DDTankMapper extends BaseMapper {
    private static final File DDTankScriptsFile = new File(SerializeDir, "scripts");

    private static List<DDTankCoreThread> scriptsList;

    static {
        try {
            scriptsList = (List<DDTankCoreThread>) FileUtils.readSeriaizedObject(DDTankScriptsFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(scriptsList == null) {
            scriptsList = new ArrayList<>();
        }
    }
}
