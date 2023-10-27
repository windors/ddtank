package cn.windor.ddtank.config;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.impl.*;
import cn.windor.ddtank.mapper.DDTankConfigMapper;
import cn.windor.ddtank.util.FileUtils;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

@Configuration
@Slf4j
public class DDtankComponents {

    @Autowired
    private ConfigurableApplicationContext context;

    @Bean
    public DDTankConfigProperties getDDTankConfigProperties() {
        return new DDTankConfigProperties(DDTankConfigMapper.getDefaultConfigProperties());
    }

    /**
     * 新建大漠对象
     *
     * @return
     */
    @Bean
    public ActiveXComponent getDmActiveXComponent() {
        return LibraryFactory.getActiveXCompnent();
    }

    /**
     * 新建大漠库
     *
     * @return
     */
    @Bean
    public DMLibrary getDMLibrary() {
        return new DMLibrary(getDmActiveXComponent());
    }

    /**
     * 由于鼠标是调用dll，所以可以设为单例
     *
     * @return
     */
    @Bean
    public Mouse getMouse() {
        return new DMMouse(getDmActiveXComponent());
    }

    /**
     * 由于键盘是调用dll，所以可以设为单例
     *
     * @return
     */
    @Bean
    public Keyboard getKeyboard() {
        return new DMKeyboard(getDmActiveXComponent());
    }
}
