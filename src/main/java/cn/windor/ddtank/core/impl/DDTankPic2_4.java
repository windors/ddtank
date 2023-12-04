package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankConfigProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import cn.windor.ddtank.util.BinaryPicProcess;
import cn.windor.ddtank.util.ColorUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankPic2_4 extends DDTankPic2_3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public DDTankPic2_4(Library dm, String path, DDTankConfigProperties properties, Mouse mouse) {
        super(dm, path, properties, mouse);
    }

    @Override
    public boolean needChooseMap() {
        Point point = new Point();
        if (dm.findPic(511, 437, 685, 527,
                path + "蛋2_4-随机地图.bmp", "101010", 0.8, 0, point)) {
            mouse.moveAndClick(point.getX() + 10, point.getY() + 10);
            delay(300, true);
        }
        return dm.findPic(261, 71, 349, 122,
                path + "蛋2-大副本页标识.bmp", "101010", 0.8, 0, null);
    }

    @Override
    public boolean needCloseTip() {
        boolean result = false;
        if (dm.findPic(640, 222, 698, 262,
                path + "蛋2_4-单人模式提示.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(428, 346);
            result = true;
        }
        Point point = new Point();
        if (dm.findPic(0, 0, 1000, 600, path + "蛋2_4-提示.bmp", "101010", 0.8, 0, point)) {
            point.setOffset(20, 10);
            mouse.moveAndClick(point);
        }
        return result;
    }

    @Override
    public boolean needCreateRoom() {
        Point point = new Point();
        if (dm.findPic(681, 442, 759, 531,
                path + "蛋2_4-大厅标识.bmp", "101010", 0.8, 0, point)) {
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
            find = true;
            if (!over) {
                for (int i = 0; i < 10; i++) {
                    Point point = cardList.get((int) (System.currentTimeMillis() % cardList.size()));
                    mouse.moveAndClick(point);
                    delay(100, true);
                }
            }
            // TODO 等待检测到第三张牌后结束
            if (dm.findPic(636, 224, 724, 291, path + "蛋2_4-翻第三张牌.bmp", "101010", 0.8, 0, null)) {
                over = true;
                if (properties.getIsThirdDraw()) {
                    mouse.moveAndClick(433, 343);
                    mouse.moveAndClick(400, 340);
                }
            }
            delay(1000, true);
        }
        if (find || dm.findPic(385, 218, 407, 246, path + "蛋2-全选进背包.bmp", "101010", 0.8, 0, null)) {
            putInBag = new Point();
            // 找全选按钮
            while (!dm.findPic(385, 218, 407, 246, path + "蛋2-全选进背包.bmp", "101010", 0.8, 0, putInBag)) {
                delay(1000, true);
            }


            // 使用罐子
            List<Point> waitOpens = dm.findPicEx(412, 121, 735, 442, path + "蛋2-罐子1.bmp|" + path + "蛋2-罐子2.bmp|" + path + "蛋2-罐子3.bmp", "101010", 0.8, 0);
            if(waitOpens != null) {
                for (Point waitOpen : waitOpens) {
                    if(waitOpen.isCloseTo(putInBag, 130, 90)) {
                        continue;
                    }
                    mouse.moveAndClick(waitOpen);
                    delay(500, true);
                    waitOpen.setOffset(20, 15);
                    mouse.moveAndClick(waitOpen);
                    delay(800, true);
                }
            }
            // 叠加勋章
            List<Point> picEx = dm.findPicEx(412, 121, 735, 442, path + "蛋2-勋章.bmp", "101010", 0.8, 0);
            if(picEx != null && picEx.size() > 1) {
                Random random = new Random();
                for (int i = 0; i < picEx.size(); i++) {
                    Point now = picEx.remove(i);
                    if(now.isCloseTo(putInBag, 130, 90)) {
                        continue;
                    }
                    if(picEx.size() == 0) break;
                    mouse.moveAndClick(now);
                    delay(100, true);
                    mouse.moveAndClick(picEx.get(random.nextInt(picEx.size())));
                    delay(100, true);
                }
            }
            mouse.moveAndClick(putInBag);
        }
        return find;
    }

    @Override
    public boolean needGoingToWharf() {
        if (dm.findPic(767, 359, 993, 537, path + "蛋2_4-大厅.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(785, 448);
            return true;
        }
        return false;
    }

    @Override
    public Integer getAngle() {
        String reuslt = dm.ocr(42, 548, 86, 588, "b33731-404040|67d5e4-404040|48b1cf-404040|c43ba7-404040|60ca32-404040|eb780a-404040|cf5b4c-404040|c1dc3b-404040|1eb458-404040", 0.8);
        boolean success = true;
        if ("".equals(reuslt)) {
            log.error("角度获取失败，请更新字库！");
            success = false;
        }
        int angle = 0;
        try {
            angle = Integer.parseInt(reuslt);
        }catch (NumberFormatException e) {
            log.error("角度获取失败，字库精准度不足！");
            success = false;
        }
        if(success) {
            return angle;
        } else {
            dm.capture(42, 548, 86, 588, DDTankFileConfigProperties.getFailDir("angle") + "/" + System.currentTimeMillis() + ".bmp");
            delay(10000, true);
            return null;
        }
    }

    @Override
    public boolean needActiveWindow() {
        if(dm.findPic(480, 250, 520, 300, path + "蛋2_4-需要激活窗口.bmp", "101010", 0.8, 0, null)) {
            mouse.moveAndClick(84, 578);
            return true;
        }
        return false;
    }
}
