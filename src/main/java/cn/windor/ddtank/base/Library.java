package cn.windor.ddtank.base;

import cn.windor.ddtank.base.impl.DMLibrary;
import com.jacob.activeX.ActiveXComponent;

import java.util.List;

public interface Library {
    long getLastError();

    ActiveXComponent getSource();
    long getMousePointWindow();
    String getWindowClass(long hwnd);
    String getColor(int x, int y);
    String getColorBGR(int x, int y);
    boolean findColor(int x1, int y1, int x2, int y2, String color, double sim, int dir, Point result);

    String findColorEx(int x1, int y1, int x2, int y2, String color, double sim, int dir);

    boolean findPic(int x1, int y1, int x2, int y2, String picName, String deltaColor, double sim, int dir, Point result);

    List<Point> findPicEx(int x1, int y1, int x2, int y2, String picName, String deltaColor, double sim, int dir);
    boolean findStr(int x1, int y1, int x2, int y2, String str, String colorFormat, double sim, Point result);
    public boolean setDict(int index, String file);
    public boolean useDict(int index);
    String ocr(int x1, int y1, int x2, int y2, String colorFormat, double sim);
    long getResultCount(String ret);
    boolean bindWindowEx(long hwnd, String display, String mouse, String keypad, String publicAttr, int mode);
    boolean unbindWindow();
}
