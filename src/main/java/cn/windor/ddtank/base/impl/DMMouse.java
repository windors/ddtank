package cn.windor.ddtank.base.impl;

import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;

import java.io.Serializable;

public class DMMouse implements Mouse, Serializable {
    private static final long serialVersionUID = 1L;
    private int offsetX;
    private int offsetY;
    transient private ActiveXComponent dm;

    public DMMouse(ActiveXComponent dm) {
        this.dm = dm;
    }

    @Override
    public void moveTo(double x, double y) {
        dm.invoke("moveTo", new Variant(x + offsetX), new Variant(y + offsetY));
    }

    @Override
    public void moveTo(Point point) {
        moveTo(point.getX(), point.getY());
    }

    @Override
    public void moveAndClick(double x, double y) {
        moveTo(x, y);
        leftClick();
    }

    @Override
    public void moveAndClick(Point point) {
        dm.invoke("moveTo", new Variant(point.getX()), new Variant(point.getY()));
        leftClick();
    }

    @Override
    public void moveAndDblClick(Point point) {
        moveTo(point);
        dm.invoke("leftDoubleClick");
    }

    @Override
    public void leftClick() {
        dm.invoke("leftClick");
    }

    @Override
    public void leftDown() {
        dm.invoke("leftDown");
    }

    @Override
    public void leftUp() {
        dm.invoke("leftUp");
    }

    @Override
    public void rightClick() {
        dm.invoke("rightClick");
    }

    @Override
    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }
}
