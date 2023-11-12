package cn.windor.ddtank.base;

import com.jacob.activeX.ActiveXComponent;

import java.util.List;

public interface Library {
    long getLastError();

    ActiveXComponent getSource();

    boolean capture(int x1, int y1, int x2, int y2, String filepath);

    int[] getClientSize(long hwnd);

    boolean getWindowState(long hwnd, int flag);

    long getMousePointWindow();
    String getWindowClass(long hwnd);
    String getColor(int x, int y);
    String getColorBGR(int x, int y);

    String getAveRGB(int x1, int y1, int x2, int y2);
    boolean findColor(int x1, int y1, int x2, int y2, String color, double sim, int dir, Point result);

    String[] findColorEx(int x1, int y1, int x2, int y2, String color, double sim, int dir);

    boolean findPic(int x1, int y1, int x2, int y2, String picName, String deltaColor, double sim, int dir, Point result);

    boolean freePic(String picName);
    List<Point> findPicEx(int x1, int y1, int x2, int y2, String picName, String deltaColor, double sim, int dir);
    boolean findStr(int x1, int y1, int x2, int y2, String str, String colorFormat, double sim, Point result);
    public boolean setDict(int index, String file);
    public boolean useDict(int index);
    String ocr(int x1, int y1, int x2, int y2, String colorFormat, double sim);
    long getResultCount(String ret);
    boolean bindWindowEx(long hwnd, String display, String mouse, String keypad, String publicAttr, int mode);
    boolean unbindWindow();
    boolean setWindowState(long hwnd, int state);

    void setFindOffset(int x, int y);

    int getOffsetX();

    int getOffsetY();

    /**
     * 获取给定窗口相关的窗口句柄
     */
    long getWindow(long hwnd, int flag);

    long findWindow(String className, String title);

    String getWindowTitle(long hwnd);

    long getWindowProcessId(long hwnd);

    long findWindowByProcessId(long pid, String className, String title);

    boolean sendString(long hwnd, String str);

    /**
     * 向绑定的窗口发送文本数据，必须配合dx.public.input.ime属性
     * @param str
     * @return
     */
    boolean sendStringIme(String str);
}
