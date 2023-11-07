package cn.windor.ddtank.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
@ConfigurationProperties(prefix = "ddtank")
@Slf4j
public class DDTankFileConfigProperties {
    private static String baseDir = "C:/tmp/";

    private static String tmpPicDir = baseDir + "tmp/pic";

    private static String failDir = baseDir + "fail";

    private static String drawDir = baseDir + "draw";
    public static String getBaseDir() {
        return baseDir;
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
        return new File(getFileDir(new File(tmpPicDir)), "screenshot-" + Thread.currentThread().getName() + ".bmp").getAbsolutePath();
    }

    public static String getDrawDir(String dir) {
        File fileDr = getFileDir(new File(drawDir, dir));
        return fileDr.getAbsolutePath();
    }

    public void setDir(String dir) {
        DDTankFileConfigProperties.baseDir = dir;
    }

    public void setTmpPicDir(String tmpPicDir) {
        DDTankFileConfigProperties.tmpPicDir = tmpPicDir;
    }

    private static File getFileDir(File file) {
        if(!file.exists()) {
            if(!file.mkdirs()) {
                log.error("文件夹{}创建失败，部分功能将不可用", file.getAbsolutePath());
            }
        }
        return file;
    }
}
