package cn.windor.ddtank.base.impl;

import cn.windor.ddtank.base.*;
import com.sun.org.apache.xpath.internal.operations.And;

import java.util.List;

import static cn.windor.ddtank.util.ThreadUtils.delay;

public class DMDDtankPic10_4 implements DDTankPic {
    private Library dm;

    /**
     * 存放资源的路径
     */
    private String path;

    private Mouse mouse;

    public DMDDtankPic10_4(Library dm, String path, Mouse mouse) {
        this.dm = dm;
        this.mouse = mouse;
        if(path.endsWith("/")) {
            this.path = path;
        }else{
            this.path = path.substring(0, path.length() - 1);
        }
    }

    @Override
    public boolean isEnterLevel() {
        return dm.findPic(960, 0, 999, 32,
                path + "蛋10.4-本内退出.bmp", "303030", 0.7, 0, null)
                || dm.findPic(960, 0, 999, 32,
                path + "蛋10.4-本内退出2.bmp", "303030", 0.7, 0, null);
    }

    @Override
    public boolean isMyRound() {
        if(dm.findPic(955, 160, 990, 200,
                path + "蛋10.4-出手判定1.bmp", "000000", 0.7, 0, null)
                || dm.findPic(955, 160, 990, 200,
                path + "蛋10.4-出手判定2.bmp", "000000", 0.7, 0, null)) {
            return dm.findPic(476, 158, 525, 176,
                    path + "蛋10.4-该出手了.bmp", "202020", 0.5, 0, null)
                    || dm.findPic(476, 158, 525, 176,
                    path + "蛋10.4-该出手了2.bmp", "202020", 0.5, 0, null)
                    || dm.findPic(476, 158, 525, 176,
                    path + "蛋10.4-该出手了3.bmp", "202020", 0.5, 0, null);
        }
        return false;
    }

    @Override
    public boolean needActiveWindow() {
        if (dm.findPic(450, 250, 530, 330,
                path + "蛋10.4-需要激活窗口.bmp", "303030", 0.8, 0, null)) {
            mouse.moveAndClick(870, 130);
            return true;
        }
        return false;
    }

    @Override
    public boolean needChooseMap() {
        Point point = new Point();
        if (dm.findPic(500, 400, 740, 500,
                path + "蛋10.4-随机地图.bmp", "101010", 0.8, 0, point)) {
            mouse.moveAndClick(point.getX() + 10, point.getY() + 10);
            delay(300);
        }
        return dm.findPic(200, 30, 320, 80,
                path + "蛋10.4-大副本页标识.bmp", "101010", 0.8, 0, null);
    }

    @Override
    public boolean needClickStart() {
        Point point = new Point();
        if(dm.findPic(900, 430, 990, 550,
                path + "蛋10.4-开始1.bmp", "101010", 0.9, 0, point)) {
            mouse.moveAndClick(point.getX() + 10, point.getY() + 10);
            return true;
        }
        return false;
    }

    @Override
    public boolean needClickPrepare() {
        Point point = new Point();
        if(dm.findPic(900, 430, 990, 550,
                path + "蛋10.4-准备.bmp", "101010", 0.9, 0, point)) {
            mouse.moveTo(point.getX(), point.getY());
            mouse.leftClick();
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseEmail() {
        if(dm.findPic(570, 190, 760, 280,
                path + "蛋10.4-邮件.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(850, 60);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseTip() {
        boolean result = false;
        if(dm.findPic(610, 210, 790, 270,
                path + "蛋10.4-单人模式提示.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(376, 319);
            mouse.moveAndClick(405, 352);
            result = true;
        }
        Point point = new Point();
        if(dm.findPic(900, 330, 980, 400,
                path + "蛋10.4-tip.bmp", "101010", 0.8, 0, point)) {
            mouse.moveAndClick(point);
            result = true;
        }
        return result;
    }

    @Override
    public boolean needCreateRoom() {
        Point point = new Point();
        if(dm.findPic(560, 490, 700, 550,
                path + "蛋10.4-大厅标识.bmp", "101010", 1, 0, point)) {
            mouse.moveAndClick(point);
            delay(1000);
            return true;
        }
        return false;
    }

    // TODO 完善架构
    @Override
    public boolean needDraw(boolean isThirdDraw) {
        List<Point> cardList;
        boolean over = false;
        while((cardList = dm.findPicEx(88, 59, 904, 571,
                path + "蛋10.4-卡牌.bmp", "101010", 0.8, 0)) != null) {
            if(!over) {
                for (int i = 0; i < 10; i++) {
                    Point point = cardList.get((int) (System.currentTimeMillis() % cardList.size()));
                    mouse.moveAndClick(point);
                }

                if(isThirdDraw && dm.findPic(650, 200, 750, 280, "蛋10.4-翻第三张牌.bmp", "101010", 0.8, 0, null)) {
                    mouse.moveAndClick(400, 340);
                    mouse.moveAndClick(400, 340);
                    over = true;
                }
            }
            delay(1000);
        }
        return false;
    }

    @Override
    public boolean needGoingToWharf() {
        Point point = new Point();
        if(dm.findPic(500, 90, 1000, 180,
                path + "蛋10.4-大厅.bmp", "101010", 1, 0, point)) {
            mouse.moveAndClick(point);
            delay(3000);
            return true;
        }
        return false;
    }

    @Override
    public int getAngle() {
        String reuslt = dm.ocr(23, 552, 77, 590, "000000-000000", 0.95);
        reuslt = reuslt.replaceAll("\\D", "");
        return Integer.parseInt(reuslt);
    }
}
