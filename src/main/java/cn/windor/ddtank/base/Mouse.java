package cn.windor.ddtank.base;

public interface Mouse {
    void moveTo(double x, double y);

    void moveTo(Point point);

    void moveAndClick(double x, double y);
    void moveAndClick(Point point);

    void leftClick();



    void leftDown();

    void leftUp();
}
