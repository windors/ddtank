package cn.windor.ddtank.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "ddtank")
@Getter
@Setter
public class DDtankConfigProperties {
    private String bind;
}
