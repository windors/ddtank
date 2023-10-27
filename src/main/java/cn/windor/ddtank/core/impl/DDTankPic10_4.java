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
            mouse.moveAndClick(point.getX() + 10, point.getY() + 10);
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
            mouse.moveAndClick(point.getX() + 10, point.getY() + 10);
            return true;
        }
        return false;
    }

    @Override
    public boolean needClickPrepare() {
        Point point = new Point();
        if (dm.findPic(900, 430, 990, 550,
                path + "蛋10.4-准备.bmp", "101010", 0.9, 0, point)) {
            mouse.moveTo(point.getX(), point.getY());
            mouse.leftClick();
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
        if (dm.findPic(900, 330, 980, 400,
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

    // TODO 完善架构
    @Override
    public boolean needDraw() {
        List<Point> cardList;
        boolean over = false;
        while ((cardList = dm.findPicEx(88, 59, 904, 571,
                path + "蛋10.4-卡牌.bmp", "101010", 0.8, 0)) != null) {
            if (!over) {
                for (int i = 0; i < 10; i++) {
                    Point point = cardList.get((int) (System.currentTimeMillis() % cardList.size()));
                    mouse.moveAndClick(point);
                    delay(100, true);
                }

                if (dm.findPic(650, 200, 750, 280, "蛋10.4-翻第三张牌.bmp", "101010", 0.8, 0, null)) {
                    over = true;
                    if (properties.getIsThirdDraw()) {
                        mouse.moveAndClick(400, 340);
                        mouse.moveAndClick(400, 340);
                    }
                }
            }
            delay(1000, true);
        }
        return false;
    }

    @Override
    public boolean needGoingToWharf() {
        Point point = new Point();
        if (dm.findPic(500, 90, 1000, 180,
                path + "蛋10.4-大厅.bmp", "101010", 1, 0, point)) {
            mouse.moveAndClick(point);
            delay(3000, true);
            return true;
        }
        return false;
    }

    @Override
    public Integer getAngle() {
        String reuslt = dm.ocr(23, 552, 77, 590, "000000-000000", 0.95);
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
        if (dm.findColor(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                properties.getColorEnemy(), 1, properties.getEnemyFindMode(), result)) {
            switch (properties.getEnemyFindMode()) {
                case 0:
                    break;
                case 1:
                case 7:
                    result.setOffset(-1, -6);
                    break;
                case 2:
                case 6:
                    result.setOffset(-4, 0);
                    break;
                case 3:
                case 8:
                    result.setOffset(-3, -6);
                    break;
            }
        } else {
            return null;
        }
        return result;
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
        int count = 0;
        Point result = new Point();
        int[] arr = new int[5];
        // 计算多次，将结果存入arr中，最终取平均值
        for (int i = 0; i < arr.length; i++) {
            arr[i] = -1;
            // 多次检测防止未检测到屏字
            for (int j = 0; j < 5; j++) {
                if (dm.findStr(properties.getStaticX1(), properties.getStaticY1(), properties.getStaticX2(), properties.getStaticY2(),
                        "屏", "999999-000000", 1, result)) {
                    int x = result.getX(), y = result.getY();
                    count = 1;
                    while (x < properties.getStaticX2() && dm.findColor(x, y, properties.getStaticX2(), y + 1, "999999-000000", 1, 0, null)) {
                        count++;
                        x++;
                    }
                    arr[i] = count;
                    break;
                }
            }
        }
        Arrays.sort(arr);

        // 选择出出现元素最多的计算结果
        int times = 0, nowDistance = -1, maxTimes = -1, maxDistance = -1;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == -1) {
                continue;
            }
            if (arr[i] == nowDistance) {
                times++;
            } else {
                times = 1;
                nowDistance = arr[i];
            }
            if (times > maxTimes) {
                maxTimes = times;
                maxDistance = nowDistance;
            }
        }
        if (maxDistance == -1) {
            log.error("屏距自动检测失败！本轮副本使用使用手动屏距！");
            return properties.getHandleDistance();
        }

        return (double) maxDistance / 10;
    }
}