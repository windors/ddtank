package cn.windor.ddtank.core;

import org.springframework.context.annotation.Description;

public interface DDTankPic {

    @Description("是否进入关卡内部")
    boolean isEnterLevel();

    @Description("是否是我的回合")
    boolean isMyRound();

    @Description("需要激活窗口")
    boolean needActiveWindow();

    @Description("需要选择地图")
    boolean needChooseMap();

    @Description("需要点击开始")
    boolean needClickStart();

    @Description("需要点击准备")
    boolean needClickPrepare();

    @Description("需要关闭邮件")
    boolean needCloseEmail();

    @Description("需要关闭提示")
    boolean needCloseTip();

    @Description("需要创建房间")
    boolean needCreateRoom();

    @Description("需要翻牌")
    boolean needDraw();

    @Description("需要进入远征码头")
    boolean needGoingToWharf();

    @Description("获取角度")
    Integer getAngle();
}
