package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.DDTankOperate;
import cn.windor.ddtank.base.DDTankPic;
import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.core.DDTankAngleAdjust;
import cn.windor.ddtank.type.TowardEnum;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class SimpleDDTankAngleAdjust implements DDTankAngleAdjust {

    private Keyboard keyboard;

    public SimpleDDTankAngleAdjust(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public boolean move(TowardEnum targetToward, int targetAngle, int triedTimes) {
        if (targetToward == TowardEnum.LEFT) {
            for (int i = 0; i < 3; i++) {
                keyboard.keyDown('a');
                delay(50);
                keyboard.keyUp('a');
                delay(100);
            }
        } else if (targetToward == TowardEnum.RIGHT) {
            for (int i = 0; i < 3; i++) {
                keyboard.keyDown('d');
                delay(50);
                keyboard.keyUp('d');
                delay(100);
            }
        }
        return triedTimes <= 3;
    }
}
