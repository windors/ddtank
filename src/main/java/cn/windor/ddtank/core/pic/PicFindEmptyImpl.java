package cn.windor.ddtank.core.pic;

import cn.windor.ddtank.base.Point;

import java.io.Serializable;
import java.util.List;

public class PicFindEmptyImpl implements PicFind, Serializable {

    private static final long serialVersionUID = 1L;
    @Override
    public boolean findPic() {
        return false;
    }

    @Override
    public boolean findPic(Point point) {
        return false;
    }

    @Override
    public List<Point> findPicEx() {
        return null;
    }
}