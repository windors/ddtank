package cn.windor.ddtank.mapper;

import cn.windor.ddtank.core.DDTankCoreScript;
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

    private static List<DDTankCoreScript> scriptsList;

    static {
        try {
            scriptsList = (List<DDTankCoreScript>) FileUtils.readSeriaizedObject(DDTankScriptsFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(scriptsList == null) {
            scriptsList = new ArrayList<>();
        }
    }

    public List<DDTankCoreScript> list() {
        return scriptsList;
    }

    public DDTankCoreScript removeByIndex(int index) {
        DDTankCoreScript script = scriptsList.remove(index);
        save();
        return script;
    }

    public DDTankCoreScript getByIndex(int index) {
        return scriptsList.get(index);
    }

    public boolean updateByIndex(int index, DDTankCoreScript script) {
        scriptsList.set(index, script);
        return save();
    }

    public boolean add(DDTankCoreScript script) {
        scriptsList.add(script);
        return save();
    }

    public boolean save() {
        return FileUtils.writeObject(scriptsList, DDTankScriptsFile);
    }

    public void addOrUpdate(DDTankCoreScript script) {
        if(scriptsList.contains(script)) {
            scriptsList.set(scriptsList.indexOf(script), script);
        }else {
            scriptsList.add(script);
        }
        save();
    }
}
