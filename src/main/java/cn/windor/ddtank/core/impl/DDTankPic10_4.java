package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.type.TowardEnum;
import cn.windor.ddtank.util.BinaryPicProcess;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankPic10_4 implements DDTankPic {
    protected Library dm;

    protected DDTankConfigProperties properties;

    /**
     * 存放资源的路径
     */
    protected String path;

    protected Mouse mouse;

    protected BinaryPicProcess binaryPicProcess;

    public DDTankPic10_4(Library dm, String path, DDTankConfigProperties properties, Mouse mouse) {
        this.dm = dm;
        this.mouse = mouse;
        this.properties = properties;
        this.binaryPicProcess = new BinaryPicProcess(dm);

        if (path.endsWith("/")) {
            this.path = path;
        } else {
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
        if (dm.findPic(955, 160, 990, 200,
                path + "蛋10.4-出手判定2.bmp", "000000", 0.7, 0, null)
                || dm.findPic(955, 160, 990, 200,
                path + "蛋10.4-出手判定1.bmp", "000000", 0.7, 0, null)) {
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
        if (dm.findPic(463, 339, 541, 363,
                path + "需要激活窗口.bmp", "202020", 0.8, 0, null)) {
            mouse.moveAndClick(84, 578);
            return true;
        }
        return false;
    }

    @Override
    public boolean needChooseMap() {
        Point point = new Point();
        if (dm.findPic(500, 400, 740, 500,
                path + "蛋10.4-随机地图.bmp", "101010", 0.8, 0, point)) {
            point.setOffset(10, 10);
            mouse.moveAndClick(point);
            delay(300, true);
        }
        return dm.findPic(200, 30, 320, 80,
                path + "蛋10.4-大副本页标识.bmp", "101010", 0.8, 0, null);
    }

    @Override
    public boolean needClickStart() {
        Point point = new Point();
        if (dm.findPic(900, 430, 990, 550,
                path + "蛋10.4-开始1.bmp", "101010", 0.9, 0, point)) {
            point.setOffset(10, 10);
            mouse.moveAndClick(point);
            return true;
        }
        return false;
    }

    @Override
    public boolean needClickPrepare() {
        Point point = new Point();
        if (dm.findPic(900, 430, 990, 550,
                path + "蛋10.4-准备.bmp", "101010", 0.9, 0, point)) {
            mouse.moveAndClick(point);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseEmail() {
        if (dm.findPic(570, 190, 760, 280,
                path + "蛋10.4-邮件.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(850, 60);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseTip() {
        boolean result = false;
        if (dm.findPic(610, 210, 790, 270,
                path + "蛋10.4-单人模式提示.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(376, 319);
            mouse.moveAndClick(405, 352);
            result = true;
        }
        Point point = new Point();
        if (dm.findPic(60, 1, 981, 545,
                path + "蛋10.4-tip.bmp", "101010", 0.8, 0, point)) {
            point.setOffset(20, 10);
            mouse.moveAndClick(point);
            result = true;
        }
        return result;
    }

    @Override
    public boolean needCreateRoom() {
        Point point = new Point();
        if (dm.findPic(560, 490, 700, 550,
                path + "蛋10.4-大厅标识.bmp", "101010", 1, 0, point)) {
            mouse.moveAndClick(point);
            delay(1000, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean needDraw() {
        List<Point> cardList;
        boolean over = false;
        boolean find = false;
        while ((cardList = dm.findPicEx(88, 59, 904, 571,
                path + "蛋10.4-卡牌.bmp", "101010", 0.8, 0)) != null) {
            find = true;
            if (!over) {
                for (int i = 0; i < 10; i++) {
                    Point point = cardList.get((int) (System.currentTimeMillis() % cardList.size()));
                    mouse.moveAndClick(point);
                }

                if (dm.findPic(590, 185, 750, 290, path + "蛋10.4-翻第三张牌.bmp", "101010", 0.8, 0, null)) {
                    over = true;
                    if (properties.getIsThirdDraw()) {
                        mouse.moveAndClick(400, 347);
                        mouse.moveAndClick(400, 347);
                    }
                }
            }
            // TODO 完善翻牌结束后的截图架构（尽快返回）
            delay(1000, true);
        }
        return find;
    }

    @Override
    public boolean needGoingToWharf() {
        Point point = new Point();
        if (dm.findPic(100, 90, 1000, 300,
                path + "蛋10.4-大厅.bmp", "101010", 1, 0, point)) {
            mouse.moveAndClick(point);
            delay(3000, true);
            return true;
        }
        return false;
    }

    @Override
    public Integer getAngle() {
        delay(100, true);
        String reuslt = dm.ocr(23, 552, 77, 590, "000000-000000", 0.9);
        reuslt = reuslt.replaceAll("\\D", "");
        if ("".equals(reuslt)) {
            return null;
        }
        return Integer.parseInt(reuslt);
    }

    /**
     * 理论上来说封装成一个类会见简单点，但考虑到用的地方很少就在这里写了
     *
     * @return
     */
    @Override
    public Point getMyPosition() {
        return binaryPicProcess.findRoundRole(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                properties.getColorRole(), 1.0);
    }

    @Override
    public Point getEnemyPosition() {
        Point result = new Point();
        String[] colors = properties.getColorEnemy().split("\\|");
        for (String color : colors) {
            if (dm.findColor(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                    color, 1, properties.getEnemyFindMode(), result)) {
                switch (properties.getEnemyFindMode()) {
                    case 0:
                    case 5:
                        // 找的是左上角
                        result.setOffset(2, 2);
                        break;
                    case 1:
                    case 7:
                        // 找的是左下角
                        result.setOffset(2, -2);
                        break;
                    case 2:
                    case 6:
                        // 找的是右上角
                        result.setOffset(-2, 2);
                        break;
                    case 3:
                    case 8:
                        // 找的是右下角
                        result.setOffset(-2, -2);
                        break;
                }
                return result;
            }
        }
        return null;
    }

    @Override
    public TowardEnum getToward() {
        List<Point> linePoint = binaryPicProcess.findLine(10, 503, 90, 589, "ff0000-402020|e0b040-202010", 1);
        if (linePoint == null) {
            return TowardEnum.UNKNOWN;
        } else {
            int center = (90 - 10) / 2 + 10;
            int left = 0;
            int right = 0;
            for (Point point : linePoint) {
                if (point.getX() > center) {
                    right++;
                } else if (point.getX() < center) {
                    left++;
                }
            }
            if (left > right) {
                return TowardEnum.LEFT;
            } else if (left < right) {
                return TowardEnum.RIGHT;
            } else {
                return TowardEnum.BOTH;
            }
        }
    }

    @Override
    public double calcUnitDistance() {
        double rectangleWidth = binaryPicProcess.findRectangleWidth(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                "999999", 1);
        return rectangleWidth / 10;
    }
}