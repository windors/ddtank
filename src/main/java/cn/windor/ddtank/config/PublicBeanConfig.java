package cn.windor.ddtank.config;

import cn.windor.config.RestTemplateConfiguration;
import cn.windor.constant.SessionConstant;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RestTemplateConfiguration.class)
public class PublicBeanConfig {
    @Bean
    public SessionConstant sessionConstant() {
        return new SessionConstant();
    }
}