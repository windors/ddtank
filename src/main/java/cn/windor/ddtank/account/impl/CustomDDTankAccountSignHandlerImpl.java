package cn.windor.ddtank.account.impl;

import cn.windor.ddtank.account.DDTankAccountSignHandler;
import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CustomDDTankAccountSignHandlerImpl implements DDTankAccountSignHandler {
    private Mouse mouse;
    private Keyboard keyboard;
    private Library dm;
    private List<String> operates;

    @Setter
    @Getter
    private String username;

    @Setter
    @Getter
    private String password;

    @Override
    public void login() {
        for (String operateStr : operates) {
            resolve(operateStr);
        }
    }

    /**
     * 键鼠操作解析方法
     * 发送文本：send .+
     * 按某个键：keyPress .+
     * 鼠标移动：moveTo \\d+, \\d+
     * 鼠标点击：leftClick \\d+
     * 延迟：delay \\d+
     */
    private void resolve(String operateStr) {
        // TODO resolve
    }

    public void setOperates(String operatesStr) {
        // 赋值为原子操作，不必担心线程安全问题
        operates = new ArrayList<>(Arrays.asList(operatesStr.split("\\n")));
    }
}
