package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.DDTankLog;
import cn.windor.ddtank.handler.DDTankAutoUsePropHandler;

import java.io.Serializable;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class DDTankAutoUsePropHandlerImpl implements DDTankAutoUsePropHandler, Serializable {

    private static final long serialVersionUID = 1L;

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
