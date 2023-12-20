package cn.windor.ddtank.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.FileCopyUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;

@Slf4j
public class FileUtils {
    /**
     * 将对象写入到file中
     *
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
     *
     * @param file
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public synchronized static Object readSeriaizedObject(File file) throws IOException, ClassNotFoundException {
        if (!file.exists()) {
            return null;
        }
        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(Files.newInputStream(file.toPath()));
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw e;
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    ois = null;
                }
            }
        }
    }

    /**
     * 将文件释放到指定目录
     */
    public static void putAttachment(String filename, File dir) {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = FileUtils.class.getResourceAsStream("/" + filename);
            if(is == null) {
                throw new RuntimeException(filename + "文件不存在");
            }
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    log.error("将文件释放到指定目录失败：指定目录[{}]不存在，且自动创建失败！", dir.getAbsolutePath());
                }
            } else {
                try {
                    FileCopyUtils.copy(is, Files.newOutputStream(new File(dir, filename.replace("/", "-").replace("\\", "-")).toPath()));
                    log.info("成功将附件[{}]放入到目录[{}]中", filename, dir.getAbsolutePath());
                } catch (IOException e) {
                    log.error("将文件释放到指定目录失败：复制过程中出现了异常[{}]", e.toString());
                    throw new RuntimeException(e);
                }
            }
        } catch (Exception e) {
            log.error("将文件释放到指定目录失败", filename);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
