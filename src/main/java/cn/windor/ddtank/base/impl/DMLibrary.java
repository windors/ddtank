package cn.windor.ddtank.base.impl;

import cn.windor.ddtank.base.Library;
import com.jacob.activeX.ActiveXComponent;
import com.jacob.com.Variant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 大漠库，当前版本必须先进行注册
 */
@Slf4j
public class DMLibrary implements Library {
    private ActiveXComponent dm;

    public DMLibrary(ActiveXComponent dm) {
        this.dm = dm;
    }

    public static void main(String[] args) {
        ActiveXComponent dm = new ActiveXComponent("dm.dmsoft");
        DMLibrary dmLibrary = new DMLibrary(dm);
        Point point = new Point();
        boolean pic = dmLibrary.findPic(0, 0, 1000, 1000, "C:\\Users\\17122\\Desktop\\1.bmp", "000000",
                0.8, 0, point);
        if(pic) {
            System.out.println(point);
        }
    }

    /**
     * 获取鼠标指向窗口的句柄
     * @return
     */
    public long getMousePointWindow() {
        return dm.invoke("getMousePointWindow").getInt();
    }

    /**
     * 获取指定句柄窗口类名
     * @param hwnd
     * @return
     */
    public String getWindowClass(long hwnd) {
        return dm.invoke("getWindowClass", new Variant(hwnd)).getString();
    }

    /**
     * 获取(x,y)的颜色,颜色返回格式"RRGGBB",注意,和按键的颜色格式相反
     * @return 颜色字符串(注意这里都是小写字符 ， 和工具相匹配)
     */
    public String getColor(int x, int y) {
        return dm.invoke("getColor", new Variant(x), new Variant(y)).getString();
    }

    /**
     * 获取(x,y)的颜色,颜色返回格式"BBGGRR
     * @return 颜色字符串(注意这里都是小写字符，和工具相匹配)
     */
    public String getColorBGR(int x, int y) {
        return dm.invoke("getColorBGR", new Variant(x), new Variant(y)).getString();
    }

    /**
     * 查找指定区域内的颜色,颜色格式"RRGGBB-DRDGDB",注意,和按键的颜色格式相反
     * @param color 颜色 格式为"RRGGBB-DRDGDB",比如"123456-000000|aabbcc-202020". 也可以支持反色模式. 前面加@即可. 比如"@123456-000000|aabbcc-202020". 具体可以看下放注释. 注意，这里只支持RGB颜色
     * @param sim 相似度,取值范围0.1-1.0
     * @param dir   0: 从左到右,从上到下
     *              1: 从左到右,从下到上
     *              2: 从右到左,从上到下
     *              3: 从右到左,从下到上
     *              4：从中心往外查找
     *              5: 从上到下,从左到右
     *              6: 从上到下,从右到左
     *              7: 从下到上,从左到右
     *              8: 从下到上,从右到左
     * @param result 找到颜色后返回的坐标
     * @return
     */
    public boolean findColor(int x1, int y1, int x2, int y2, String color, double sim, int dir, Point result) {
        Variant resultX = new Variant(-1, true);
        Variant resultY = new Variant(-1, true);
        if(dm.invoke("findColor", new Variant(x1), new Variant(y1), new Variant(x2), new Variant(y2),
                new Variant(color), new Variant(sim), new Variant(dir), resultX, resultY).getInt() == 0) {
            return false;
        } else {
            if(result != null) {
                result.setX(resultX.getInt());
                result.setY(resultY.getInt());
            }
            return true;
        }
    }

    /**
     * 查找指定区域内的 <b>所有颜色</b>,颜色格式"RRGGBB-DRDGDB",注意,和按键的颜色格式相反
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
     * @return 返回所有颜色信息的坐标值,然后通过GetResultCount等接口来解析 (由于内存限制,返回的颜色数量最多为1800个左右)
     */
    public String findColorEx(int x1, int y1, int x2, int y2, String color, double sim, int dir) {
        return dm.invoke("findColorEx", new Variant(x1), new Variant(y1), new Variant(x2), new Variant(y2),
                new Variant(color), new Variant(sim), new Variant(dir)).getString();
    }


    /**
     * 大漠找图 dmLibrary.findPic(0, 0, 1000, 1000, "C:\\Users\\17122\\Desktop\\1.bmp", "000000",
     *                 0.8, 0, point);
     * @param x1 左上x
     * @param y1 左上y
     * @param x2 右下x
     * @param y2 右下y
     * @param picName 图片名称（带路径）
     * @param deltaColor 颜色色差（RGB）
     * @param sim 相似度
     * @param dir 查找方式：0: 从左到右,从上到下 1: 从左到右,从下到上 2: 从右到左,从上到下 3: 从右到左, 从下到上
     * @param result 若找到了坐标，则将坐标放至point对象内
     * @return 是否找到了图片
     */
    public boolean findPic(int x1, int y1, int x2, int y2, String picName, String deltaColor, double sim, int dir, Point result) {
        Variant resultX = new Variant(-1, true);
        Variant resultY = new Variant(-1, true);
        if(dm.invoke("findPic",
                new Variant(x1), new Variant(y1), new Variant(x2), new Variant(y2),
                new Variant(picName), new Variant(deltaColor),
                new Variant(sim), new Variant(dir), resultX, resultY).getInt() == -1) {
            log.debug("调用查找图片，未找到");
            return false;
        }else{
            if (result != null) {
                result.setX(resultX.getInt());
                result.setY(resultY.getInt());
            }
            return true;
        }
    }

    /**
     *
     * @param str 待查找的字符串,可以是字符串组合，比如"长安|洛阳|大雁塔",中间用"|"来分割字符串
     * @param colorFormat 颜色格式串, 可以包含换行分隔符,语法是","后加分割字符串. 具体可以查看下面的示例 .注意，RGB和HSV,以及灰度格式都支持.
     * @param sim 相似度,取值范围0.1-1.0
     * @param result 若找到则将结果坐标封装到次变量中
     * @return 是否找到了指定字符串
     */
    public boolean findStr(int x1, int y1, int x2, int y2, String str, String colorFormat, double sim, Point result) {
        Variant resultX = new Variant(-1, true);
        Variant resultY = new Variant(-1, true);
        if(dm.invoke("findStr", new Variant(x1), new Variant(y1), new Variant(x2), new Variant(y2),
                new Variant(str), new Variant(colorFormat), new Variant(sim), resultX, resultY).getInt() == -1) {
            return false;
        }else{
            if (result != null) {
                result.setX(resultX.getInt());
                result.setY(resultY.getInt());
            }
            return true;
        }
    }


    /**
     * 设置字库文件
     * @param index 整形数:字库的序号,取值为0-99,目前最多支持100个字库
     * @param file  字符串:字库文件名（带路径）
     * @return 是否成功
     */
    public boolean setDict(int index, String file) {
        return dm.invoke("setDict", new Variant(index), new Variant(file)).getBoolean();
    }

    /**
     * 表示使用哪个字库文件进行识别(index范围:0-99)
     * 设置之后，永久生效，除非再次设定
     * @return 是否成功
     */
    public boolean useDict(int index) {
        return dm.invoke("useDict", new Variant(index)).getBoolean();
    }




    /**
     * 识别屏幕范围(x1,y1,x2,y2)内符合color_format的字符串,并且相似度为sim,sim取值范围(0.1-1.0)，这个值越大越精确,越大速度越快,越小速度越慢,请斟酌使用!
     * @param colorFormat 颜色格式串. 可以包含换行分隔符,语法是","后加分割字符串. 具体可以查看下面的示例
     * @param sim 相似度,取值范围0.1-1.0
     * @return 识别到的字符串
     */
    public String ocr(int x1, int y1, int x2, int y2, String colorFormat, double sim) {
        return dm.invoke("ocr", new Variant(x1), new Variant(y1), new Variant(x2), new Variant(y2),
                new Variant(colorFormat), new Variant(sim)).getString();
    }


    /**
     * 对插件部分接口的返回值进行解析,并返回ret中的坐标个数
     * @param ret 部分接口的返回串
     * @return 返回ret中的坐标个数
     */
    public long getResultCount(String ret) {
        return dm.invoke("getResultCount", new Variant(ret)).getInt();
    }

    /**
     * 详见大漠文档绑定说明
     */
    public boolean bindWindowEx(long hwnd, String display, String mouse, String keypad, String publicAttr, int mode) {
        return dm.invoke("bindWindowEx", new Variant(hwnd), new Variant(mouse), new Variant(keypad),
                new Variant(publicAttr), new Variant(mode)).getBoolean();
    }

    public boolean unbindWindow() {
        return dm.invoke("unbindWindow").getBoolean();
    }
}
