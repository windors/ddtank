package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.handler.DDTankAngleAdjustMoveHandler;
import cn.windor.ddtank.type.TowardEnum;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class SimpleDDTankAngleAdjustMoveHandlerHandlerImpl implements DDTankAngleAdjustMoveHandler {

    private Keyboard keyboard;

    public SimpleDDTankAngleAdjustMoveHandlerHandlerImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public boolean move(TowardEnum targetToward, int targetAngle, int triedTimes) {
        if (targetToward == TowardEnum.LEFT) {
            for (int i = 0; i < 3; i++) {
                keyboard.keyDown('a');
                delay(50, true);
                keyboard.keyUp('a');
                delay(100, true);
            }
        } else if (targetToward == TowardEnum.RIGHT) {
            for (int i = 0; i < 3; i++) {
                keyboard.keyDown('d');
                delay(50, true);
                keyboard.keyUp('d');
                delay(100, true);
            }
        }
        return triedTimes <= 3;
    }
}
