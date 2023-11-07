package cn.windor.ddtank.core;

import cn.windor.ddtank.base.Keyboard;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.handler.DDTankAngleAdjustMoveHandler;
import cn.windor.ddtank.handler.DDTankFindPositionMoveHandler;
import cn.windor.ddtank.handler.impl.SimpleDDTankAngleAdjustMoveHandlerHandlerImpl;
import cn.windor.ddtank.handler.impl.SimpleDDTankFindPositionMoveHandlerImpl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DDTankCoreHandlerSelector {

    private final Map<Integer, DDTankAngleAdjustMoveHandler> angleMoveHandlerMap = new ConcurrentHashMap<>();
    private final Map<Integer, DDTankFindPositionMoveHandler> positionMoveHandlerMap = new ConcurrentHashMap<>();

    private final DDTankConfigProperties properties;

    public DDTankCoreHandlerSelector(Keyboard keyboard, DDTankConfigProperties properties) {
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
