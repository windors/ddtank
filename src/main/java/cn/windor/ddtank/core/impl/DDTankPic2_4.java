package cn.windor.ddtank.core.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Mouse;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.core.DDTankCoreTaskProperties;
import cn.windor.ddtank.config.DDTankFileConfigProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.Random;

import static cn.windor.ddtank.util.ThreadUtils.delay;

@Slf4j
public class DDTankPic2_4 extends DDTankPic2_3 implements Serializable {

    private static final long serialVersionUID = 1L;

    public DDTankPic2_4(Library dm, String path, DDTankCoreTaskProperties properties, Mouse mouse) {
        super(dm, path, properties, mouse);
    }

    @Override
    public boolean needDraw() {
        List<Point> cardList;
        boolean find = false;
        boolean over = false;
        Point putInBag = null;
        while ((cardList = getPicFind("needDraw").findPicEx()) != null) {
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
        if (find || getPicFind("needDraw3").findPic()) {
            putInBag = new Point();
            // 找全选按钮
            while (!getPicFind("needDraw3").findPic(putInBag)) {
                delay(1000, true);
            }


            // 使用罐子
            List<Point> waitOpens = getPicFind("needDraw4").findPicEx();
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
            List<Point> picEx = getPicFind("needDraw5").findPicEx();
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
        if (getPicFind("needGoingToWharf").findPic()) {
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
}
