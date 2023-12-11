package cn.windor.ddtank.base.impl;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.util.JacobUtils;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import lombok.extern.slf4j.Slf4j;
import com.jacob.com.Dispatch;
import org.thymeleaf.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.windor.ddtank.util.ThreadUtils.delayPersisted;

/**
 * 大漠库，当前版本必须先进行注册
 */
@Slf4j
public class DMLibrary implements Library, Serializable {

    private long callTimes = 0;
    transient private ActiveXComponent dm;

    private int offsetX;

    private int offsetY;

    private final Map<Object, Variant> paramMap = new ConcurrentHashMap<>();

    public DMLibrary() {

    }

    public DMLibrary(ActiveXComponent dm) {
        this.dm = dm;
    }

    private Variant getParam(Object param) {
        callTimes++;
        return JacobUtils.getParam(param);
    }

    public long getCallTimes() {
        return callTimes;
    }

    private Variant getParam(Object param, boolean ref) {
        callTimes++;
        if (!ref) return getParam(param);
        return new Variant(param, true);
    }

    @Override
    public long getLastError() {
        return dm.invoke("getLastError").getInt();
    }

    @Override
    public ActiveXComponent getSource() {
        return dm;
    }

    @Override
    public boolean capture(int x1, int y1, int x2, int y2, String filepath) {
        return Dispatch.call(dm, "capture", getParam(x1 +  + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY), getParam(filepath)).getInt() == 1;
    }

    public static boolean capture(Library dm, long hwnd, String filepath) {
        int[] size = dm.getClientSize(hwnd);
        int width = size[0];
        int height = size[1];
        return dm.capture(0, 0, width, height, filepath);
    }

    @Override
    public int[] getClientSize(long hwnd) {
        int[] result = new int[2];
        Variant width = getParam(-1, true);
        Variant height = getParam(-2, true);
        if (Dispatch.call(dm, "getClientSize", getParam(hwnd), width, height).getInt() == 1) {
            result[0] = width.getInt();
            result[1] = height.getInt();
        } else {
            return null;
        }
        return result;
    }

    @Override
    public boolean getWindowState(long hwnd, int flag) {
        return Dispatch.call(dm, "getWindowState", getParam(hwnd), getParam(flag)).getInt() == 1;
    }

    /**
     * 获取鼠标指向窗口的句柄
     *
     * @return
     */
    public long getMousePointWindow() {
        return Dispatch.call(dm, "getMousePointWindow").getInt();
    }

    /**
     * 获取指定句柄窗口类名
     *
     * @param hwnd
     * @return
     */
    public String getWindowClass(long hwnd) {
        return Dispatch.call(dm, "getWindowClass", getParam(hwnd)).getString();
    }

    /**
     * 获取(x,y)的颜色,颜色返回格式"RRGGBB",注意,和按键的颜色格式相反
     *
     * @return 颜色字符串(注意这里都是小写字符 ， 和工具相匹配)
     */
    public String getColor(int x, int y) {
        return Dispatch.call(dm, "getColor", getParam(x + offsetX), getParam(y + offsetY)).getString();
    }

    /**
     * 获取(x,y)的颜色,颜色返回格式"BBGGRR
     *
     * @return 颜色字符串(注意这里都是小写字符 ， 和工具相匹配)
     */
    public String getColorBGR(int x, int y) {
        return Dispatch.call(dm, "getColorBGR", getParam(x + offsetX), getParam(y + offsetY)).getString();
    }

    @Override
    public String getAveRGB(int x1, int y1, int x2, int y2) {
        return Dispatch.call(dm, "getAveRGB", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY)).getString();
    }

    /**
     * 查找指定区域内的颜色,颜色格式"RRGGBB-DRDGDB",注意,和按键的颜色格式相反
     *
     * @param color  颜色 格式为"RRGGBB-DRDGDB",比如"123456-000000|aabbcc-202020". 也可以支持反色模式. 前面加@即可. 比如"@123456-000000|aabbcc-202020". 具体可以看下放注释. 注意，这里只支持RGB颜色
     * @param sim    相似度,取值范围0.1-1.0
     * @param dir    0: 从左到右,从上到下
     *               1: 从左到右,从下到上
     *               2: 从右到左,从上到下
     *               3: 从右到左,从下到上
     *               4：从中心往外查找
     *               5: 从上到下,从左到右
     *               6: 从上到下,从右到左
     *               7: 从下到上,从左到右
     *               8: 从下到上,从右到左
     * @param result 找到颜色后返回的坐标
     * @return
     */
    public boolean findColor(int x1, int y1, int x2, int y2, String color, double sim, int dir, Point result) {

        if (Dispatch.call(dm, "findColor", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                getParam(color), getParam(sim), getParam(dir), getParam(-1), getParam(-1)).getInt() == 0) {
            return false;
        } else {
            if (result != null) {
                Variant resultX = getParam(-1, true);
                Variant resultY = getParam(-2, true);
                if(Dispatch.call(dm, "findColor", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                        getParam(color), getParam(sim), getParam(dir), resultX, resultY).getInt() != 0)
                result.setX(resultX.getInt());
                result.setY(resultY.getInt());
            }
            return true;
        }
    }

    /**
     * 查找指定区域内的 <b>所有颜色</b>,颜色格式"RRGGBB-DRDGDB",注意,和按键的颜色格式相反
     *
     * @param color 颜色 格式为"RRGGBB-DRDGDB" 比如"aabbcc-000000|123456-202020".也可以支持反色模式. 前面加@即可. 比如"@123456-000000|aabbcc-202020". 具体可以看下放注释.注意，这里只支持RGB颜色.
     * @param sim   相似度,取值范围0.1-1.0
     * @param dir   0: 从左到右,从上到下
     *              1: 从左到右,从下到上
     *              2: 从右到左,从上到下
     *              3: 从右到左,从下到上
     *              5: 从上到下,从左到右
     *              6: 从上到下,从右到左
     *              7: 从下到上,从左到右
     *              8: 从下到上,从右到左
     * @return 返回所有颜色信息的坐标值, 然后通过GetResultCount等接口来解析 (由于内存限制,返回的颜色数量最多为1800个左右)
     */
    public String[] findColorEx(int x1, int y1, int x2, int y2, String color, double sim, int dir) {
        String findColorEx = Dispatch.call(dm, "findColorEx", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                getParam(color), getParam(sim), getParam(dir)).getString();
        if ("".equals(findColorEx)) {
            return new String[0];
        }
        int count = Dispatch.call(dm, "getResultCount", getParam(findColorEx)).getInt();
        String[] result = new String[count];
        for (int i = 0; i < count; i++) {
            Variant x = getParam(-1, true);
            Variant y = getParam(-2, true);
            if (Dispatch.call(dm, "getResultPos", getParam(findColorEx), getParam(i), getParam(x), getParam(y)).getInt() == 1) {
                result[i] = x.getInt() + "|" + y.getInt();
            } else {
                log.error("调用解析函数失败！");
            }
        }
        return result;
    }


    /**
     * 大漠找图 dmLibrary.findPic(0, 0, 1000, 1000, "C:\\Users\\17122\\Desktop\\1.bmp", "000000",
     * 0.8, 0, point);
     *
     * @param x1         左上x
     * @param y1         左上y
     * @param x2         右下x
     * @param y2         右下y
     * @param picName    图片名称（带路径）
     * @param deltaColor 颜色色差（RGB）
     * @param sim        相似度
     * @param dir        查找方式：0: 从左到右,从上到下 1: 从左到右,从下到上 2: 从右到左,从上到下 3: 从右到左, 从下到上
     * @param result     若找到了坐标，则将坐标放至point对象内
     * @return 是否找到了图片
     */
    public boolean findPic(int x1, int y1, int x2, int y2, String picName, String deltaColor, double sim, int dir, Point result) {
        if (Dispatch.call(dm, "findPic",
                getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                getParam(picName), getParam(deltaColor),
                getParam(sim), getParam(dir), getParam(-1), getParam(-1)).getInt() == -1) {
            return false;
        } else {
            if (result != null) {
                Variant resultX = getParam(-1, true);
                Variant resultY = getParam(-2, true);
                if(Dispatch.call(dm, "findPic",
                        getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                        getParam(picName), getParam(deltaColor),
                        getParam(sim), getParam(dir), resultX, resultY).getInt() != -1) {
                    result.setX(resultX.getInt());
                    result.setY(resultY.getInt());
                }else {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public List<Point> findPicEx(int x1, int y1, int x2, int y2, String picName, String deltaColor, double sim, int dir) {
        Dispatch.call(dm, "loadPic", getParam(picName));
        String dmResultStr = Dispatch.call(dm, "findPicEx", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                getParam(picName), getParam(deltaColor), getParam(sim), getParam(dir)).getString();
        if ("".equals(dmResultStr)) {
            return null;
        }
        String[] pointsStr = dmResultStr.split("\\|");
        List<Point> result = new ArrayList<>(pointsStr.length);
        for (String pointStr : pointsStr) {
            String[] pointArr = pointStr.split(",");
            result.add(new Point(Integer.parseInt(pointArr[1]), Integer.parseInt(pointArr[2])));
        }
        return result;
    }

    @Override
    public boolean freePic(String picName) {
        return Dispatch.call(dm, "freePic", getParam(picName)).getInt() == 1;
    }

    /**
     * @param str         待查找的字符串,可以是字符串组合，比如"长安|洛阳|大雁塔",中间用"|"来分割字符串
     * @param colorFormat 颜色格式串, 可以包含换行分隔符,语法是","后加分割字符串. 具体可以查看下面的示例 .注意，RGB和HSV,以及灰度格式都支持.
     * @param sim         相似度,取值范围0.1-1.0
     * @param result      若找到则将结果坐标封装到次变量中
     * @return 是否找到了指定字符串
     */
    public boolean findStr(int x1, int y1, int x2, int y2, String str, String colorFormat, double sim, Point result) {

        if (Dispatch.call(dm, "findStr", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                getParam(str), getParam(colorFormat), getParam(sim), getParam(-1), getParam(-1)).getInt() == -1) {
            return false;
        } else {
            if (result != null) {
                Variant resultX = getParam(-1, true);
                Variant resultY = getParam(-2, true);
                if (Dispatch.call(dm, "findStr", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                        getParam(str), getParam(colorFormat), getParam(sim), resultX, resultY).getInt() != -1) {
                    result.setX(resultX.getInt());
                    result.setY(resultY.getInt());
                } else {
                    return false;
                }
            }
            return true;
        }
    }


    /**
     * 设置字库文件
     *
     * @param index 整形数:字库的序号,取值为0-99,目前最多支持100个字库
     * @param file  字符串:字库文件名（带路径）
     * @return 是否成功
     */
    public boolean setDict(int index, String file) {
        return Dispatch.call(dm, "setDict", getParam(index), getParam(file)).getInt() == 1;
    }

    /**
     * 表示使用哪个字库文件进行识别(index范围:0-99)
     * 设置之后，永久生效，除非再次设定
     *
     * @return 是否成功
     */
    public boolean useDict(int index) {
        return Dispatch.call(dm, "useDict", getParam(index)).getInt() == 1;
    }


    /**
     * 识别屏幕范围(x1,y1,x2,y2)内符合color_format的字符串,并且相似度为sim,sim取值范围(0.1-1.0)，这个值越大越精确,越大速度越快,越小速度越慢,请斟酌使用!
     *
     * @param colorFormat 颜色格式串. 可以包含换行分隔符,语法是","后加分割字符串. 具体可以查看下面的示例
     * @param sim         相似度,取值范围0.1-1.0
     * @return 识别到的字符串
     */
    public String ocr(int x1, int y1, int x2, int y2, String colorFormat, double sim) {
        return Dispatch.call(dm, "ocr", getParam(x1 + offsetX), getParam(y1 + offsetY), getParam(x2 + offsetX), getParam(y2 + offsetY),
                getParam(colorFormat), getParam(sim)).getString();
    }


    /**
     * 对插件部分接口的返回值进行解析,并返回ret中的坐标个数
     *
     * @param ret 部分接口的返回串
     * @return 返回ret中的坐标个数
     */
    public long getResultCount(String ret) {
        return Dispatch.call(dm, "getResultCount", getParam(ret)).getInt();
    }

    /**
     * 详见大漠文档绑定说明
     */
    public boolean bindWindowEx(long hwnd, String display, String mouse, String keypad, String publicAttr, int mode) {
        delayPersisted(1000, false);
        return Dispatch.call(dm, "bindWindowEx", getParam(hwnd), getParam(display), getParam(mouse), getParam(keypad),
                getParam(publicAttr), getParam(mode)).getInt() == 1;
    }

    public boolean unbindWindow() {
        delayPersisted(1000, false);
        return Dispatch.call(dm, "unbindWindow").getInt() == 1;
    }

    @Override
    public boolean setWindowState(long hwnd, int state) {
        return Dispatch.call(dm, "setWindowState", getParam(hwnd), getParam(state)).getInt() == 1;
    }

    @Override
    public void setFindOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }

    @Override
    public int getOffsetX() {
        return this.offsetX;
    }

    @Override
    public int getOffsetY() {
        return this.offsetY;
    }

    @Override
    public long getWindow(long hwnd, int flag) {
        return Dispatch.call(dm, "getWindow", getParam(hwnd), getParam(flag)).getInt();
    }

    @Override
    public List<Long> enumWindow(long parent, String title, String className, int filter) {
        String returnHwnds = Dispatch.call(dm, "enumWindow", getParam(parent), getParam(title), getParam(className), getParam(filter)).getString();
        // 转换为java对象
        List<Long> resultList = new ArrayList<>();
        if(StringUtils.isEmpty(returnHwnds)) {
            return resultList;
        }
        String[] hwnds = returnHwnds.split(",");
        for (String hwnd : hwnds) {
            resultList.add(Long.parseLong(hwnd));
        }
        return resultList;
    }

    @Override
    public long findWindow(String className, String title) {
        return Dispatch.call(dm, "findWindow", getParam(className), getParam(title)).getInt();
    }

    @Override
    public String getWindowTitle(long hwnd) {
        return Dispatch.call(dm, "getWindowTitle", getParam(hwnd)).getString();
    }

    @Override
    public long getWindowProcessId(long hwnd) {
        return Dispatch.call(dm, "getWindowProcessId", getParam(hwnd)).getInt();
    }

    @Override
    public long findWindowByProcessId(long pid, String className, String title) {
        return Dispatch.call(dm, "findWindowByProcessId", getParam(pid), getParam(className), getParam(title)).getInt();
    }

    @Override
    public boolean sendString(long hwnd, String str) {
        return Dispatch.call(dm, "sendString", getParam(hwnd), getParam(str)).getInt() == 1;
    }

    @Override
    public boolean sendStringIme(String str) {
        return Dispatch.call(dm, "sendStringIme", getParam(str)).getInt() == 1;
    }

    @Override
    public List<Long> enumWindowByProcess(String processName, String title, String className, int filter) {
        String hwndsStr = Dispatch.call(dm, "enumWindowByProcess", getParam(processName), getParam(title), getParam(className), getParam(filter)).getString();
        List<Long> result = new ArrayList<>();
        if(StringUtils.isEmpty(hwndsStr)) {
            return result;
        }
        String[] hwnds = hwndsStr.split(",");
        for (String hwnd : hwnds) {
            result.add(Long.parseLong(hwnd));
        }
        return result;
    }

    @Override
    public Long findWindowEx(Long hwnd, String className, String title) {
        return (long) Dispatch.call(dm, "findWindowEx", getParam(hwnd), getParam(className), getParam(title)).getInt();
    }

    public static void main(String[] args) throws IOException {
        Library dm = new DMLibrary(JacobUtils.getActiveXCompnent());
        Long hwnd = dm.findWindowEx(986392l, "AfxWnd80su", "PageLabelBar");
        System.out.println(hwnd);
    }
}
