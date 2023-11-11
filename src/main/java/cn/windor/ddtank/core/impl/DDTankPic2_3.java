package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.util.BinaryPicProcess;
import cn.windor.ddtank.util.ColorUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankPic2_3 extends DDTankPic10_4 {
    public DDTankPic2_3(Library dm, String path, DDTankConfigProperties properties, Mouse mouse) {
        super(dm, path, properties, mouse);
    }

    @Override
    public boolean isEnterLevel() {
        return dm.findPic(969, 470, 998, 502,
                path + "蛋2-本内退出.bmp", "303030", 0.7, 0, null);
    }

    @Override
    public boolean isMyRound() {
        return dm.findPic(110, 90, 210, 140,
                path + "蛋2-该出手了.bmp", "303030", 0.7, 0, null)
                || dm.findPic(110, 90, 210, 140,
                path + "蛋2-该出手了2.bmp", "303030", 0.7, 0, null);
    }

    @Override
    public boolean needChooseMap() {
        Point point = new Point();
        if (dm.findPic(511, 437, 685, 527,
                path + "蛋2-随机地图.bmp", "101010", 0.8, 0, point)) {
            mouse.moveAndClick(point.getX() + 10, point.getY() + 10);
            delay(300, true);
        }
        return dm.findPic(261, 71, 349, 122,
                path + "蛋2-大副本页标识.bmp", "101010", 0.8, 0, null);
    }

    @Override
    public boolean needClickStart() {
        Point point = new Point();
        if (dm.findPic(891, 454, 981, 495,
                path + "蛋2-开始1.bmp", "101010", 0.9, 0, point)
                || dm.findPic(891, 454, 981, 495,
                path + "蛋2-开始2.bmp", "101010", 0.9, 0, point)) {
//            mouse.moveAndClick(560, 844);
//            mouse.moveAndClick(801, 137);
//            mouse.moveAndClick(882, 141);
//            mouse.moveAndClick(949, 346);
//            mouse.moveAndClick(949, 346);
//            mouse.moveAndClick(949, 346);
            mouse.moveAndClick(point.getX() + 10, point.getY() + 10);
            return true;
        }
        return false;
    }

    @Override
    public boolean needClickPrepare() {
        Point point = new Point();
        if (dm.findPic(891, 454, 981, 545,
                path + "蛋2-准备.bmp", "101010", 0.9, 0, point)) {
            mouse.moveAndClick(point);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseEmail() {
        Point point = new Point();
        if (dm.findPic(506, 164, 829, 404, path + "蛋2-邮件.bmp", "101010", 0.8, 0, point)) {
            mouse.moveAndClick(836, 52);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseTip() {
        boolean result = false;
        if (dm.findPic(640, 222, 698, 262,
                path + "蛋2-单人模式提示.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(428, 346);
            result = true;
        }
        Point point = new Point();
        if (dm.findPic(0, 0, 1000, 600, path + "蛋2-提示.bmp", "101010", 0.8, 0, point)) {
            point.setOffset(20, 10);
            mouse.moveAndClick(point);
        }
        return result;
    }

    @Override
    public boolean needCreateRoom() {
        Point point = new Point();
        if (dm.findPic(665, 466, 743, 540,
                path + "蛋2-大厅标识.bmp", "101010", 1, 0, point)) {
            point.setOffset(30, 10);
            mouse.moveAndClick(point);
            mouse.moveAndClick(408, 442);
            delay(1000, true);
            return true;
        }
        return false;
    }

    @Override
    public boolean needDraw() {
        List<Point> cardList;
        boolean find = false;
        boolean over = false;
        Point putInBag = null;
        while ((cardList = dm.findPicEx(88, 59, 904, 571,
                path + "蛋2-卡牌.bmp", "101010", 0.8, 0)) != null) {
            // 只要能找到卡牌，那么就循环
            find = true;
            if (!over) {
                for (int i = 0; i < 10; i++) {
                    Point point = cardList.get((int) (System.currentTimeMillis() % cardList.size()));
                    mouse.moveAndClick(point);
                    delay(100, true);
                }
            }
            // TODO 等待检测到第三张牌后结束
            if (dm.findPic(636, 224, 724, 291, path + "蛋2-翻第三张牌.bmp", "101010", 0.8, 0, null)) {
                over = true;
                if (properties.getIsThirdDraw()) {
                    mouse.moveAndClick(433, 343);
                    mouse.moveAndClick(400, 340);
                }
            }
            delay(1000, true);
        }
        // 如果已经翻过牌或找到了全选进被背包
        if (find || dm.findPic(385, 218, 407, 246, path + "蛋2-全选进背包.bmp", "101010", 0.8, 0, null)) {
            putInBag = new Point();
            int failTimes = 0;
            // 只要没找到全选按钮，那么就一直找
            while (!dm.findPic(385, 218, 407, 246, path + "蛋2-全选进背包.bmp", "101010", 0.8, 0, putInBag)) {
                failTimes++;
                delay(1000, true);
                if(failTimes > 30) {
                    // 30秒未找到全选按钮，那么就直接返回
                    return true;
                }
            }
            mouse.moveAndClick(putInBag);
        }
        return find;
    }

    @Override
    public boolean needGoingToWharf() {
        if (dm.findPic(500, 466, 711, 554, path + "蛋2-频道.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(608, 171);
            mouse.moveAndClick(608, 171);
        }
        mouse.moveTo(558, 531);
        Point point = new Point();
        if (dm.findPic(767, 359, 993, 537, path + "蛋2-大厅.bmp", "101010", 0.8, 0, point)) {
            mouse.moveAndClick(point);
            return true;
        }
        return false;
    }

    @Override
    public Integer getAngle() {
        String reuslt = dm.ocr(42, 548, 86, 588, "1a1a1a-000000|1a260d-000000|101724-000000|1a2016-000000|28222b-000000|260d0d-000000|1c1d20-000000|211d1d-000000|171d32-000000|1c0d03-000000", 0.95);
        reuslt = reuslt.replaceAll("\\D", "");
        if ("".equals(reuslt)) {
            log.error("角度获取失败，请更新字库！");
            dm.capture(42, 548, 86, 588, DDTankFileConfigProperties.getFailDir("angle") + "/" + System.currentTimeMillis() + ".bmp");
            return null;
        }
        return Integer.parseInt(reuslt);
    }

    @Override
    public double calcUnitDistance() {
        String color = dm.getAveRGB(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2());
        // 希望将颜色偏白606060
        color = ColorUtils.add(color, "303030") + "-303030";
        log.debug("计算屏距-颜色：{}", color);
        double rectangleWidth = binaryPicProcess.findRectangleWidth(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                color, 1);
        return rectangleWidth / 10;
    }
}