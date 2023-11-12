package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.handler.DDTankAccountSignHandler;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class DDTankAccountSignHandlerImpl implements DDTankAccountSignHandler {
    private Mouse mouse;

    private Keyboard keyboard;

    private Library dm;

    public DDTankAccountSignHandlerImpl(Library dm, Mouse mouse, Keyboard keyboard) {
        this.dm = dm;
        this.mouse = mouse;
        this.keyboard = keyboard;
    }

    @Override
    public void login(String username, String password) {
        // 修仙端的自动登录
        mouse.moveAndClick(1, 1);
        keyboard.keyPressChar("tab");
        keyboard.keyPressChar("tab");
        keyboard.keyPressChar("tab");
        dm.sendStringIme(username);
        delay(500, true);
        keyboard.keyPressChar("tab");
        dm.sendStringIme(password);
        delay(500, true);
        keyboard.keyPressChar("tab");
        keyboard.keyPressChar("enter");
    }
}
