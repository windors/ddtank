package cn.windor.ddtank.mapper;

import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.util.FileUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    private static List<DDTankConfigProperties> list;

    static {
        try {
            list = (List<DDTankConfigProperties>) FileUtils.readSeriaizedObject(DDTankConfigPropertiesListFile);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if(list == null) {
            list = new ArrayList<>();
        }
    }

    public static DDTankConfigProperties getDefaultConfigProperties() {
        try {
            return (DDTankConfigProperties) FileUtils.readSeriaizedObject(DefaultDDTankConfigPropertiesFile);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("读取默认配置列表出错：{}", e.getMessage());
        }
        return null;
    }

    public boolean saveDefaultConfig(DDTankConfigProperties config) {
        return FileUtils.writeObject(config, DefaultDDTankConfigPropertiesFile);
    }

    public List<DDTankConfigProperties> list() {
        return list;
    }

    public DDTankConfigProperties removeByIndex(int index) {
        DDTankConfigProperties result = list.remove(index);
        save();
        return result;
    }

    public DDTankConfigProperties getByIndex(int index) {
        return list.get(index);
    }

    public boolean updateByIndex(int index, DDTankConfigProperties config) {
        list.set(index, config);
        return save();
    }
    public boolean add(DDTankConfigProperties properties) {
        list.add(properties);
        return save();
    }

    public boolean save() {
        return FileUtils.writeObject(list, DDTankConfigPropertiesListFile);
    }
}
