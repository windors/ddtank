package cn.windor.ddtank.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;

@Slf4j
public class FileUtils {
    /**
     * 将对象写入到file中
     * @param object
     * @param file
     * @return
     */
    public synchronized static boolean writeObject(Object object, File file) {
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

    /**
     * 从file中读取对象
     * @param file
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public synchronized static Object readSeriaizedObject(File file) throws IOException, ClassNotFoundException {
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
