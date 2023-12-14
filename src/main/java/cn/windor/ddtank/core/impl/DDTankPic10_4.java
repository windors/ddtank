package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.*;
import cn.windor.ddtank.config.DMPicConfigProperties;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.core.DDTankPic;
import cn.windor.ddtank.type.TowardEnum;
import cn.windor.ddtank.util.BinaryPicProcess;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankPic10_4 extends AbstractDDTankPic implements DDTankPic, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 存放资源的路径
     */
    protected String path;

    protected Mouse mouse;

    protected BinaryPicProcess binaryPicProcess;

    public DDTankPic10_4(Library dm, String path, DDTankCoreTaskProperties properties, Mouse mouse) {
        super(properties, dm, "pic10_4");
        this.mouse = mouse;
        this.binaryPicProcess = new BinaryPicProcess(dm);

        if (path.endsWith("/")) {
            this.path = path;
        } else {
            this.path = path.substring(0, path.length() - 1);
        }
    }

    @Override
    public boolean isEnterLevel() {
        return getPicFind("isEnterLevel").findPic();
    }

    @Override
    public boolean isMyRound() {
        return getPicFind("isMyRound").findPic();
    }

    @Override
    public boolean needActiveWindow() {
        if(getPicFind("needActiveWindow").findPic()) {
            mouse.moveAndClick(84, 578);
            return true;
        }
        return false;
    }

    @Override
    public boolean needChooseMap() {
        Point point = new Point();
        if(getPicFind("needChooseMap").findPic(point)) {
            mouse.moveAndClick(point.setOffset(10, 10));
            delay(300, true);
        }
        return getPicFind("needChooseMap2").findPic();
    }

    @Override
    public boolean needClickStart() {
        Point point = new Point();
        if (getPicFind("needClickStart").findPic(point)) {
            point.setOffset(10, 10);
            mouse.moveAndClick(point);
            return true;
        }
        return false;
    }

    @Override
    public boolean needClickPrepare() {
        Point point = new Point();
        if (getPicFind("needClickPrepare").findPic(point)) {
            mouse.moveAndClick(point);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseEmail() {
        if (getPicFind("needCloseEmail").findPic()) {
            mouse.moveAndClick(850, 60);
            return true;
        }
        return false;
    }

    @Override
    public boolean needCloseTip() {
        boolean result = false;
        if (getPicFind("needCloseTip").findPic()) {
            mouse.moveAndClick(376, 319);
            mouse.moveAndClick(405, 352);
            result = true;
        }
        Point point = new Point();
        if (getPicFind("needCloseTip2").findPic(point)) {
            point.setOffset(20, 10);
            mouse.moveAndClick(point);
            result = true;
        }
        return result;
    }

    @Override
    public boolean needCreateRoom() {
        Point point = new Point();
        if (getPicFind("needCreateRoom").findPic(point)) {
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
        while ((cardList = getPicFind("needDraw").findPicEx()) != null) {
            find = true;
            if (!over) {
                for (int i = 0; i < 10; i++) {
                    Point point = cardList.get((int) (System.currentTimeMillis() % cardList.size()));
                    mouse.moveAndClick(point);
                }

                if (getPicFind("needDraw2").findPic()) {
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
        if (getPicFind("needGoingToWharf").findPic(point)) {
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
        Point result = binaryPicProcess.findRoundRole(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                properties.getColorRole(), 1.0);
        if(result != null) {
            result.setOffset(properties.getMyOffsetX(), properties.getMyOffsetY());
        }
        return result;
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
                result.setOffset(properties.getEnemyOffsetX(), properties.getEnemyOffsetY());
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

    @Override
    public double getWind() {
        String windStr = dm.ocr(461, 20, 536, 42, "000000-000000|ff0000-000000", 1);
        try {
            double wind = Double.parseDouble(windStr);
            long startTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - startTime < 1000) {
                if (dm.findColor(524, 25, 537, 39, "eaeaea|848484", 1, 0, null)) {
                    return -wind;
                } else if (dm.findColor(462, 26, 474, 48, "eaeaea|848484", 1, 0, null)) {
                    return wind;
                }
            }
        }catch (Exception e) {
            log.warn("未找到风力");
            // 保存风力图片
            dm.capture(461, 20, 536, 42, DDTankFileConfigProperties.getFailDir("wind") + "/" + System.currentTimeMillis() + ".bmp");
        }
        return 0;
    }
}