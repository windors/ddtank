package cn.windor.ddtank.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "ddtank")
@Slf4j
public class DDTankFileConfigProperties {
    private static String dir = "C:/tmp/";

    private static String tmpPicDir = dir + "tmp/pic";

    private static String failDir = dir + "fail";
    public static String getDir() {
        return dir;
    }

    public static String getTmpPicDir() {
        File file = new File(tmpPicDir);
        if(!file.exists()) {
            if(!file.mkdirs()) {
                log.error("文件夹{}创建失败，部分功能将不可用", file.getAbsolutePath());
            }
        }
        return tmpPicDir;
    }

    public static String getFailDir(String dir) {
        File file = new File(failDir, dir);
        if(!file.exists()) {
            if(!file.mkdirs()) {
                log.error("文件夹{}创建失败，部分功能将不可用", file.getAbsolutePath());
            }
        }
        return file.getAbsolutePath();
    }

    /**
     * 截图用的文件临时保存位置
     * @return
     */
    public static String getScreenshotPath() {
        File file = new File(tmpPicDir);
        if(!file.exists()) {
            if(!file.mkdirs()) {
                log.error("文件夹{}创建失败，部分功能将不可用", file.getAbsolutePath());
            }
        }
        return new File(file, "screenshot-" + Thread.currentThread().getName() + ".bmp").getAbsolutePath();
    }

    public void setDir(String dir) {
        DDTankFileConfigProperties.dir = dir;
    }

    public void setTmpPicDir(String tmpPicDir) {
        DDTankFileConfigProperties.tmpPicDir = tmpPicDir;
    }
}
