package cn.windor.ddtank.config;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicInteger;


public class DDTankStartParam {

    private static final AtomicInteger idAdder = new AtomicInteger(1);

    // 线程名
    @Getter
    @Setter
    private String name;


    public DDTankStartParam() {
        this.name = "脚本" + idAdder.getAndIncrement();
    }
}
