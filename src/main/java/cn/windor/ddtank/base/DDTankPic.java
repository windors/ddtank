package cn.windor.ddtank.base;

public interface DDTankPic {
    boolean isEnterLevel();

    boolean isMyRound();

    boolean needActiveWindow();

    boolean needChooseMap();

    boolean needClickStart();

    boolean needClickPrepare();

    boolean needCloseEmail();

    boolean needCloseTip();

    boolean needCreateRoom();

    boolean needDraw(boolean isThirdDraw);

    boolean needGoingToWharf();

    int getAngle();
}
