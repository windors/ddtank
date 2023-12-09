package cn.windor.ddtank.mapper;

import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.util.FileUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class DDTankConfigMapper extends BaseMapper {

    private static final File DefaultDDTankConfigPropertiesFile = new File(SerializeDir, "defaultDDTankConfigProperties");

    private static final File DDTankConfigPropertiesListFile = new File(SerializeDir, "dDTankConfigPropertiesList");

    @Getter
    private static final File DDTankStrengthFile = new File(SerializeDir, "strengthTable");
    private static List<DDTankCoreTaskProperties> list;

    static {
        try {
            list = (List<DDTankCoreTaskProperties>) FileUtils.readSeriaizedObject(DDTankConfigPropertiesListFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(list == null) {
            list = new ArrayList<>();
        }
    }

    public static DDTankCoreTaskProperties getDefaultConfigProperties() {
        try {
            return (DDTankCoreTaskProperties) FileUtils.readSeriaizedObject(DefaultDDTankConfigPropertiesFile);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("读取默认配置列表出错：{}", e.getMessage());
        }
        return null;
    }

    public boolean saveDefaultConfig(DDTankCoreTaskProperties config) {
        return FileUtils.writeObject(config, DefaultDDTankConfigPropertiesFile);
    }

    public List<DDTankCoreTaskProperties> list() {
        return list;
    }

    public DDTankCoreTaskProperties removeByIndex(int index) {
        DDTankCoreTaskProperties result = list.remove(index);
        save();
        return result;
    }

    public DDTankCoreTaskProperties getByIndex(int index) {
        return list.get(index);
    }

    public boolean updateByIndex(int index, DDTankCoreTaskProperties config) {
        list.set(index, config);
        return save();
    }
    public boolean add(DDTankCoreTaskProperties properties) {
        list.add(properties);
        return save();
    }

    public boolean save() {
        return FileUtils.writeObject(list, DDTankConfigPropertiesListFile);
    }
}
