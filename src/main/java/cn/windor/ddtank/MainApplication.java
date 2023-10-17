package cn.windor.ddtank;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.awt.*;
import java.net.Inet4Address;
import java.net.URI;

@SpringBootApplication
@Slf4j
public class MainApplication {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        ConfigurableApplicationContext context = SpringApplication.run(MainApplication.class, args);
        ConfigurableEnvironment environment = context.getEnvironment();
        String port = environment.getProperty("server.port");
        log.info("项目已启动");
        try {
            URI uri = URI.create("http://" + Inet4Address.getLocalHost().getHostAddress() + ":" + port);
            Desktop desktop = Desktop.getDesktop();
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                // 调用默认浏览器打开网页
                desktop.browse(uri);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.warn("自动打开脚本首页地址失败，请手动打开网页查看脚本：http://localhost:" + port);
        }
    }
}