package cn.windor.ddtank.core.pic;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Point;

import java.io.Serializable;
import java.util.List;

public class PicFindLibraryImpl implements PicFind, Serializable {

    private static final long serialVersionUID = 1L;

    private final int x1;
    private final int x2;
    private final int y1;
    private final int y2;
    private final String picName;
    private final String deltaColor;
    private final double sim;
    private final int dir;

    private Library dm;

    public PicFindLibraryImpl(int x1, int x2, int y1, int y2, String picName, String deltaColor, double sim, int dir, Library dm) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.picName = picName;
        this.deltaColor = deltaColor;
        this.sim = sim;
        this.dir = dir;
        this.dm = dm;
    }

    @Override
    public boolean findPic() {
        return dm.findPic(x1, y1, x2, y2, picName, deltaColor, sim, dir, null);
    }

    @Override
    public boolean findPic(Point point) {
        return dm.findPic(x1, y1, x2, y2, picName, deltaColor, sim, dir, point);
    }

    @Override
    public List<Point> findPicEx() {
        return dm.findPicEx(x1, y1, x2, y2, picName, deltaColor, sim, dir);
    }
}
