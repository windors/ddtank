package cn.windor.ddtank.mapper;

import cn.windor.ddtank.core.DDTankCoreThread;
import cn.windor.ddtank.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class DDTankScriptMapper extends BaseMapper {
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

    public List<DDTankCoreThread> list() {
        return scriptsList;
    }

    public DDTankCoreThread removeByIndex(int index) {
        DDTankCoreThread script = scriptsList.remove(index);
        save();
        return script;
    }

    public DDTankCoreThread getByIndex(int index) {
        return scriptsList.get(index);
    }

    public boolean updateByIndex(int index, DDTankCoreThread script) {
        scriptsList.set(index, script);
        return save();
    }

    public boolean add(DDTankCoreThread script) {
        scriptsList.add(script);
        return save();
    }

    public boolean save() {
        return FileUtils.writeObject(scriptsList, DDTankScriptsFile);
    }
}
