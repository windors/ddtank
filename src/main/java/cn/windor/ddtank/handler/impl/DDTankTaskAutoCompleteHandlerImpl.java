package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.handler.DDTankTaskAutoCompleteHandler;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class DDTankTaskAutoCompleteHandlerImpl implements DDTankTaskAutoCompleteHandler {

    private Keyboard keyboard;

    private Mouse mouse;

    public DDTankTaskAutoCompleteHandlerImpl(Keyboard keyboard, Mouse mouse) {
        this.keyboard = keyboard;
        this.mouse = mouse;
    }
    @Override
    public boolean update(Object... complexObject) {
        boolean success = true;
        for (Object param : complexObject) {
            if(param instanceof Keyboard) {
                this.keyboard = (Keyboard) param;
                continue;
            }
            if(param instanceof Mouse) {
                this.mouse = (Mouse) param;
                continue;
            }
            success = false;
        }
        return success;
    }

    @Override
    public void completeTask() {
        // 先点击一下其他地方，防止q按在了聊天输入框里。
        mouse.moveAndClick(20,520);
        for (int i = 0; i < 3; i++) {
            keyboard.keyPress('q');
            delay(1000, true);
            // 领取任务按钮的位置
            mouse.moveAndClick(667,517);
            delay(1000, true);
            keyboard.keyPress('q');
            mouse.moveAndClick(667,517);
        }
    }
}
