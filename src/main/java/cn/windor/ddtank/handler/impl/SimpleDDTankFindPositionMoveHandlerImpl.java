package cn.windor.ddtank.handler.impl;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.handler.DDTankFindPositionMoveHandler;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class SimpleDDTankFindPositionMoveHandlerImpl implements DDTankFindPositionMoveHandler {

    private Keyboard keyboard;

    public SimpleDDTankFindPositionMoveHandlerImpl(Keyboard keyboard) {
        this.keyboard = keyboard;
    }

    @Override
    public boolean move(int triedTimes) {
        if (System.currentTimeMillis() % 3 == 2) {
            toRight();
        } else {
            toLeft();
        }
        return true;
    }

    private void toLeft() {
        keyboard.keyPress('a');
        keyboard.keyDown('a');
        delay(200, true);
        keyboard.keyUp('a');
        keyboard.keyPress('d');
    }

    private void toRight() {
        keyboard.keyPress('d');
        keyboard.keyDown('d');
        delay(200, true);
        keyboard.keyUp('d');
        keyboard.keyPress('a');
    }
}
