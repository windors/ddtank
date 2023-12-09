package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.handler.DDTankAngleAdjustMoveHandler;
import cn.windor.ddtank.handler.DDTankFindPositionMoveHandler;
import cn.windor.ddtank.handler.impl.SimpleDDTankAngleAdjustMoveHandlerHandlerImpl;
import cn.windor.ddtank.handler.impl.SimpleDDTankFindPositionMoveHandlerImpl;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DDTankCoreHandlerSelector implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Map<Integer, DDTankAngleAdjustMoveHandler> angleMoveHandlerMap = new ConcurrentHashMap<>();
    private final Map<Integer, DDTankFindPositionMoveHandler> positionMoveHandlerMap = new ConcurrentHashMap<>();

    private final DDTankCoreTaskProperties properties;

    public DDTankCoreHandlerSelector(Keyboard keyboard, DDTankCoreTaskProperties properties) {
        this.properties = properties;
        angleMoveHandlerMap.put(0, new SimpleDDTankAngleAdjustMoveHandlerHandlerImpl(keyboard));
        positionMoveHandlerMap.put(0, new SimpleDDTankFindPositionMoveHandlerImpl(keyboard));
    }

    public DDTankAngleAdjustMoveHandler getAngleMoveHandler() {
        return angleMoveHandlerMap.get(properties.getAngleMoveMode());
    }

    public DDTankFindPositionMoveHandler getPositionMoveHandler() {
        return positionMoveHandlerMap.get(properties.getPositionMoveMode());
    }
}
