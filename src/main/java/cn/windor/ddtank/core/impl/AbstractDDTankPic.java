package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.config.DMPicConfigProperties;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.core.pic.PicFind;
import cn.windor.ddtank.core.pic.PicFindBuilder;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDDTankPic implements DDTankPic, Serializable {

    private static final long serialVersionUID = 1L;


    protected Library dm;

    protected DDTankCoreTaskProperties properties;

    private Map<String, PicFind> picFindMap;


    public AbstractDDTankPic(DDTankCoreTaskProperties properties, Library dm, String picKeyPrefix) {
        this.properties = properties;
        this.dm = dm;
        Map<String, PicFindBuilder> keyPicFindBuilderMap = DMPicConfigProperties.getKeyPicFindBuilderMap(this.getClass());
        picFindMap = new HashMap<>();
        for (String key : keyPicFindBuilderMap.keySet()) {
            PicFindBuilder picFindBuilder = keyPicFindBuilderMap.get(key);
            picFindMap.put(key, picFindBuilder.build(new File(DDTankFileConfigProperties.getBaseDir(), properties.getPicDir()).getAbsolutePath(), dm));
        }
    }

    protected PicFind getPicFind(String key) {
        return picFindMap.get(key);
    }
}
