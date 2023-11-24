package cn.windor.ddtank.base;

public interface Mouse {
    /**
     * 带偏移量的移动方法
     * @param x
     * @param y
     */
    void moveTo(double x, double y);

    /**
     * 精确移动到某个点
     * @param point 使用某些接口找图时的返回值（根据找图而非自己定义的点，自己定义的点会带有偏移量，找图直接就是精准的，所以不需要偏移）
     */
    void moveTo(Point point);

    void moveAndClick(double x, double y);

    void moveAndClick(Point point);

    void moveAndDblClick(Point point);

    void leftClick();



    void leftDown();

    void leftUp();

    void rightClick();

    void setOffset(int x, int y);
}
