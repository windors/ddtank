package cn.windor.ddtank.config;

import cn.windor.ddtank.base.DDTankOperate;
import cn.windor.ddtank.base.DDTankPic;
import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.impl.*;
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
     * @return
     */
    @Bean
    public DMLibrary getDMLibrary() {
        return new DMLibrary(getDmActiveXComponent());
    }

    /**
     * 由于鼠标是调用dll，所以可以设为单例
     * @return
     */
    @Bean
    public Mouse getMouse() {
        return new DMMouse(getDmActiveXComponent());
    }

    /**
     * 由于键盘是调用dll，所以可以设为单例
     * @return
     */
    @Bean
    public Keyboard getKeyboard() {
        return new DMKeyboard(getDmActiveXComponent());
    }

    /**
     * 由于找图没有自己的成员变量，所以可以是单例
     * @return
     */
    @Bean
    public DDTankPic getDDTankPic() {
        return new DMDDtankPic10_4(getDMLibrary(), "C:/tmp/", getMouse());
    }
}
