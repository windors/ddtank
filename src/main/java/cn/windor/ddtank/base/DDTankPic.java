package cn.windor.ddtank.base;

public interface DDTankPic {
    boolean isEnterLevel();

    boolean isMyRound();

    void needActiveWindow();

    void needChooseMap();

    void needClickStart();

    void needClickPrepare();

    void needCloseEmail();

    void needCloseTip();

    void needCreateRoom();

    void needDraw(boolean isThirdDraw);

    void needGoingToWharf();
}
