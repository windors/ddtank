package cn.windor.ddtank;


import com.jacob.activeX.ActiveXComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@SpringBootApplication
@Slf4j
public class MainApplication {

    private static ActiveXComponent dm = null;
    public static void main(String[] args) throws ClassNotFoundException {
        SpringApplication.run(MainApplication.class, args);
        log.info("项目已启动");
    }
}
