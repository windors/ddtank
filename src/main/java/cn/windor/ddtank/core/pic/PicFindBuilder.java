package cn.windor.ddtank.core.pic;

import cn.windor.ddtank.base.Library;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

@Slf4j
@Accessors(chain = true)
@Setter
public class PicFindBuilder {
    private int x1;
    private int x2;
    private int y1;
    private int y2;
    private String picName;
    private String deltaColor;
    private double sim;
    private int dir;

    public PicFindBuilder(int x1, int x2, int y1, int y2, String picName, String deltaColor, double sim, int dir) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.picName = picName;
        this.deltaColor = deltaColor;
        this.sim = sim;
        this.dir = dir;
    }

    /**
     * 得到目标路径下符合要求的文件有多少个
     * @return
     */
    public File[] searchLegalFiles(Path dirPath) {
        File dirFile = dirPath.toFile();
        if(!dirFile.exists() || !dirFile.isDirectory()) {
            log.error("指定文件夹[{}]不存在", dirFile.getAbsolutePath());
            return null;
        }

        String pattern = picName.split("(\\.bmp)+$")[0] + "\\d*";
        File[] files = dirFile.listFiles((dir, name) -> {
            File file = new File(dir, name);
            if(file.isFile()) {
                name = name.split("(\\.bmp)+$")[0];
                return Pattern.matches(pattern, name);
            }
            // 跳过文件夹的检测
            return false;
        });

        if(files == null || files.length == 0) {
            return null;
        }
        return files;
    }

    public PicFind build(String dirPath, Library dm) {
        File[] files = searchLegalFiles(new File(dirPath).toPath());
        if(files == null || files.length == 0) {
            log.warn("未在指定文件夹[{}]下找到[{}]!", dirPath, picName);
            return new PicFindEmptyImpl();
        }
        StringBuilder picName = new StringBuilder();
        for (File file : files) {
            picName.append(file.getAbsolutePath()).append("|");
        }
        return new PicFindLibraryImpl(x1, x2, y1, y2, picName.substring(0, picName.length() - 1), deltaColor, sim, dir, dm);
    }
}