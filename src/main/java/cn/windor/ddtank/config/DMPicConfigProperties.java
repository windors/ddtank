package cn.windor.ddtank.config;


import cn.windor.ddtank.core.pic.PicFindBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

@ConfigurationProperties(prefix = "windor")
@Slf4j
public class DMPicConfigProperties {

    @Getter
    private static List<String> srcConfigs;

    /**
     * 项目启动后由Spring调用本方法，根据application.yaml内定义的属性完成项目中所有需要用到图片识别的配置
     * config格式：
     * 格式1：namespace.method\\d+, x1, y1, x2, y2, picName, deltaColor, sim, dir
     * prefix表示命名空间, key为调用时的key
     * 格式2：($)key, x1, y1, x2, y2, picName, deltaColor, sim, dir
     * $表示
     */
    public void setSrcConfigs(List<String> srcConfigs) {
        DMPicConfigProperties.srcConfigs = srcConfigs;
        // 根据configs对picConfigMap进行更新
        namespaceMethodSrcConfigMap = new HashMap<>();
        for (String srcConfigStr : srcConfigs) {
            String[] split = srcConfigStr.split(",");
            String srcKey = split[0].trim();
            String namespace = srcKey.substring(0, srcKey.lastIndexOf("."));

            // namespaceSrcConfigMap: namespace对应的methodSrcConfigMap
            Map<String, List<String>> namespaceSrcConfigMap = namespaceMethodSrcConfigMap.computeIfAbsent(namespace, k -> new HashMap<>());
            String methodName = srcKey.substring(srcKey.lastIndexOf('.') + 1).split("\\d+")[0];

            // methodSrcConfigMap: namespace.method对应的所有srcConfig
            List<String> methodSrcConfigMap = namespaceSrcConfigMap.computeIfAbsent(methodName, k -> new ArrayList<>());
            methodSrcConfigMap.add(srcConfigStr);
        }
    }

    /**
     * 内部维护的配置信息映射表，通过namespace来获取namespace下所有的图片配置
     * 结构：
     *      key：namespace
     *      Map(String, List)的key：method
     *      Map(String, List)的value：源配置信息
     *  通过namespaceMethodSrcConfigMap.get(类名).get(方法名)来获取指定的配置信息，注意null值判断，防止出现空指针异常
     */
    private static Map<String, Map<String, List<String>>> namespaceMethodSrcConfigMap = new HashMap<>();

    /**
     * 根据传入的类名来查找所有符合要求的图片检测配置
     *
     * @param clazz 类名，会自动检测本类及其父类设置的所有图片检测配置
     * @return 图片检测配置
     */
    public static Map<String, PicFindBuilder> getKeyPicFindBuilderMap(Class<?> clazz) {
        Map<String, PicFindBuilder> result = new HashMap<>();
        Set<String> recordedKey = new HashSet<>();
        do {
            // 获取clazz.getName()配置的所有图片类
            Map<String, List<String>> methodSrcConfigMap = namespaceMethodSrcConfigMap.get(clazz.getSimpleName());
            if(methodSrcConfigMap != null) {
                // 指定namespace下有method配置了信息
                for (String methodName : methodSrcConfigMap.keySet()) {
                    if(recordedKey.contains(methodName)) {
                        // 已经记录过methodName, 说明子类下的配置已经进行了覆盖重写，跳过本methodName
                        continue;
                    }
                    recordedKey.add(methodName);
                    // 将指定的所有配置加入到结果集中即可
                    for (String srcConfig : methodSrcConfigMap.get(methodName)) {
                        PicFindBuilder oldBuilder = result.put(getCallKey(srcConfig), getPicFindBuilder(srcConfig));
                        if(oldBuilder != null) {
                            log.error("[{}]图片配置错误，错误原因：[{}]重名", srcConfig, methodName);
                        }
                    }
                }
            }
            // 在继承链上挨个检测设置的所有图片
            clazz = clazz.getSuperclass();
        } while (clazz != null && clazz != Object.class);
        return result;
    }

    /**
     * 获取调用的key，例如：cn.windor.DDTankPic10_4.needCloseTips2，会返回needCloseTips2
     */
    private static String getCallKey(String srcConfigStr) {
        String[] split = srcConfigStr.split(",");
        String namespaceMethodKey = split[0];
        return namespaceMethodKey.substring(namespaceMethodKey.lastIndexOf(".") + 1);
    }

    /**
     * 根据源配置字符串获取PicFindBuilder
     */
    private static PicFindBuilder getPicFindBuilder(String srcConfigStr) {
        String[] split = srcConfigStr.split(",");
        int x1 = Integer.parseInt(split[1].trim());
        int y1 = Integer.parseInt(split[2].trim());
        int x2 = Integer.parseInt(split[3].trim());
        int y2 = Integer.parseInt(split[4].trim());
        String picName = split[5].trim();
        String deltaColor = split[6].trim();
        double sim = Double.parseDouble(split[7].trim());
        int dir = Integer.parseInt(split[8].trim());
        return new PicFindBuilder(x1, x2, y1, y2, picName, deltaColor, sim, dir);
    }

    public static void main(String[] args) {
        String str = "DDTankPic10_4.needChooseMap2";
        String substring = str.substring(str.lastIndexOf('.') + 1).split("\\d+")[0];
        System.out.println(substring);
    }
}