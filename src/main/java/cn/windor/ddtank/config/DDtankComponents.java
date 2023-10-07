package cn.windor.ddtank.config;

import cn.windor.ddtank.base.impl.DMLibrary;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class DDtankComponents {

    /**
     * 新建大漠对象
     * @return
     */
    @Bean ActiveXComponent getDmActiveXComponent() {
        ActiveXComponent dm = new ActiveXComponent("dm.dmsoft");
        int zc = dm.invoke("reg",
                new Variant("xiaohuang24881536f74c595b38f5bff8f28735a4"), new Variant("kh")).getInt();
        if (zc == 1) {
            log.info("大漠插件注册成功");
        } else {
            log.error("大漠插件注册失败");
            System.exit(0);
        }
        return dm;
    }

    /**
     * 新建大漠库
     * @param dm
     * @return
     */
    @Bean
    public DMLibrary getDMLibrary(ActiveXComponent dm) {
        return new DMLibrary(dm);
    }
}
