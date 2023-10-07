package cn.windor.ddtank.base.impl;

import cn.windor.ddtank.base.DDTankPic;

public class DDtankPicDM implements DDTankPic {
    private DMLibrary dm;

    /**
     * 存放资源的路径
     */
    private String path;

    public DDtankPicDM(DMLibrary dm, String path) {
        this.dm = dm;
        this.path = path;
    }

    @Override
    public boolean isEnterLevel() {
        return false;
    }

    @Override
    public boolean isMyRound() {
        return false;
    }

    @Override
    public void needActiveWindow() {

    }

    @Override
    public void needChooseMap() {

    }

    @Override
    public void needClickStart() {

    }

    @Override
    public void needClickPrepare() {

    }

    @Override
    public void needCloseEmail() {

    }

    @Override
    public void needCloseTip() {

    }

    @Override
    public void needCreateRoom() {

    }

    @Override
    public void needDraw(boolean isThirdDraw) {

    }

    @Override
    public void needGoingToWharf() {

    }
}
