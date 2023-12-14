package cn.windor.ddtank.core.pic;

import cn.windor.ddtank.base.Point;

import java.util.List;

public interface PicFind {

    boolean findPic();

    boolean findPic(Point point);

    List<Point> findPicEx();
}
