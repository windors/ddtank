package cn.windor.ddtank.util;

import cn.windor.ddtank.config.DDTankConfigProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;

@Slf4j
public class DDTankFileUtils {
    private static final File serializeDir = new File("serializeDir");

    private static final File defaultDDTankConfigPropertiesFile = new File(serializeDir, "defaultDDTankConfigProperties");

    static {
        if (!serializeDir.exists()) {
            if(!serializeDir.mkdir()) {
                log.error("文件夹创建失败，请手动创建文件夹[{}]或使用管理员模式启动本程序", serializeDir.getAbsolutePath());
            }
        }
    }

    /**
     * 获取默认配置
     * @return 返回保存在本地的默认配置，null表示无默认配置
     */
    public static DDTankConfigProperties readDefaultConfig() {
        try {
            return (DDTankConfigProperties) readSeriaizedObject(defaultDDTankConfigPropertiesFile);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            log.error("读取默认配置列表出错：{}", e.getMessage());
        }
        return null;
    }

    public static boolean writeDefaultConfig(DDTankConfigProperties ddTankConfigProperties) {
        return writeObject(ddTankConfigProperties, defaultDDTankConfigPropertiesFile);
    }


    private static boolean writeObject(Object object, File file) {
        try {
            ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(file.toPath()));
            oos.writeObject(object);
            oos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            log.error("将对象{}写入到[{}]失败，错误原因：{}", object, file.getAbsolutePath(), e.getMessage());
            return false;
        }
    }

    private static Object readSeriaizedObject(File file) throws IOException, ClassNotFoundException {
        if(!file.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(Files.newInputStream(file.toPath()));
            return ois.readObject();
        }catch (IOException | ClassNotFoundException e) {
            throw e;
        } finally {
            if(ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    ois = null;
                }
            }
        }
    }
}
