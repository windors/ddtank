package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.handler.DDTankAutoUsePropHandler;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class DDTankAutoUsePropHandlerImpl implements DDTankAutoUsePropHandler {


    private Mouse mouse;

    private Keyboard keyboard;

    private Library library;

    private DDTankLog ddTankLog;


    public DDTankAutoUsePropHandlerImpl(Mouse mouse, Keyboard keyboard, Library library, DDTankLog ddTankLog) {
        this.mouse = mouse;
        this.keyboard = keyboard;
        this.library = library;
        this.ddTankLog = ddTankLog;
    }

    @Override
    public boolean update(Object... complexObject) {
        boolean success = true;
        for (Object param : complexObject) {
            if (param instanceof Mouse) {
                this.mouse = (Mouse) param;
                continue;
            }
            if (param instanceof Keyboard) {
                this.keyboard = (Keyboard) param;
                continue;
            }
            if (param instanceof Library) {
                this.library = (Library) param;
                continue;
            }
            if(param instanceof DDTankLog) {
                this.ddTankLog = (DDTankLog) param;
                continue;
            }
            success = false;
        }
        return success;
    }

    @Override
    public void useProp() {
        // 点击聊天框上方按钮，将鼠标从聊天框中取消激活
        mouse.moveAndClick(21, 519);
        // 打开背包
        keyboard.keyPress('b');
        delay(1000, true);

        Point prop = new Point();
        boolean find = findProp(prop);
        if (!find) {
            // 尝试找第二页
            mouse.moveAndClick(833, 436);
            find = findProp(prop);
        }
        if(!find) {
            ddTankLog.warn("未找到活力药水");
            return;
        }
        // 使用道具
        mouse.moveAndClick(prop.setOffset(20, 20));
        delay(500, true);
        mouse.moveTo(prop.setOffset(20, 10));
        mouse.leftClick();
        delay(1000, true);

        // 关闭背包
        keyboard.keyPress('b');
    }

    private boolean findProp(Point prop) {
        boolean find = false;
        for (int i = 0; i < 3; i++) {
            // 点击道具栏
            mouse.moveAndClick(882, 298);
            // 等待1秒程序相应
            delay(1000, true);

            if (library.findPic(527, 136, 855, 465, DDTankFileConfigProperties.getBaseDir() + "/活力药水.bmp", "101010", 0.8, 0, prop)) {
                find = true;
                break;
            }
        }
        return find;
    }
}
