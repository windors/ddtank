package cn.windor.ddtank.mapper;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class BaseMapper {
    static final File SerializeDir = new File("serializeDir");

    static {
        if (!SerializeDir.exists()) {
            if (!SerializeDir.mkdir()) {
                log.error("文件夹创建失败，请手动创建文件夹[{}]或使用管理员模式启动本程序", SerializeDir.getAbsolutePath());
            }
        }
    }
}
