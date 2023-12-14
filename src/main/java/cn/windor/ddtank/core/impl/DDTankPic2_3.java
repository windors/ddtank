package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.util.ColorUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankPic2_3 extends DDTankPic10_4 implements Serializable {
    private static final long serialVersionUID = 1L;

    public DDTankPic2_3(Library dm, String path, DDTankCoreTaskProperties properties, Mouse mouse) {
        super(dm, path, properties, mouse);
    }


    @Override
    public boolean needClickStart() {
        Point point = new Point();
        if (getPicFind("needClickStart").findPic(point)) {
//            mouse.moveAndClick(560, 844);
//            mouse.moveAndClick(801, 137);
//            mouse.moveAndClick(882, 141);
//            mouse.moveAndClick(949, 346);
//            mouse.moveAndClick(949, 346);
//            mouse.moveAndClick(949, 346);
            mouse.moveAndClick(point.setOffset(10, 10));
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseEmail() {
        if (getPicFind("needCloseEmail").findPic()) {
            mouse.moveAndClick(836, 52);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseTip() {
        boolean result = false;
        if (getPicFind("needCloseTip").findPic()) {
            mouse.moveAndClick(428, 346);
            result = true;
        }
        Point point = new Point();
        if (getPicFind("needCloseTip2").findPic(point)) {
            point.setOffset(20, 10);
            mouse.moveAndClick(point);
        }
        return result;
    }

    @Override
    public boolean needCreateRoom() {
        Point point = new Point();
        if (getPicFind("needCreateRoom").findPic(point)) {
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
        while ((cardList = getPicFind("needDraw").findPicEx()) != null) {
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
            if (getPicFind("needDraw2").findPic()) {
                over = true;
                if (properties.getIsThirdDraw()) {
                    mouse.moveAndClick(433, 343);
                    mouse.moveAndClick(400, 340);
                }
            }
            delay(1000, true);
        }
        // 如果已经翻过牌或找到了全选进被背包
        if (find || getPicFind("needDraw3").findPic()) {
            putInBag = new Point();
            int failTimes = 0;
            // 只要没找到全选按钮，那么就一直找
            while (!getPicFind("needDraw3").findPic(putInBag)) {
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
        if (getPicFind("needGoingToWharf").findPic()) {
            mouse.moveAndClick(608, 171);
            mouse.moveAndClick(608, 171);
        }
        // 蛋2.3大厅检测需要移动下鼠标
        mouse.moveTo(558, 531);
        Point point = new Point();
        if (getPicFind("needGoingToWharf2").findPic(point)) {
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