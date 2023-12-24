package cn.windor.ddtank.config;


import lombok.Getter;
import lombok.Setter;

public class DDTankSetting {
    /**
     * 失败捕获，当角度、风力等获取失败时，是否将图片保存至文件（fail目录）
     */
    private static boolean failCapture = false;

    /**
     * 翻牌结算截图（draw目录）
     */
    private static boolean drawCapture = false;

    public static boolean isFailCapture() {
        return failCapture;
    }

    public static void setFailCapture(boolean failCapture) {
        DDTankSetting.failCapture = failCapture;
    }

    public static boolean isDrawCapture() {
        return drawCapture;
    }

    public static void setDrawCapture(boolean drawCapture) {
        DDTankSetting.drawCapture = drawCapture;
    }
}
