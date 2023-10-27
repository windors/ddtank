package cn.windor.ddtank.util;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Point;
import cn.windor.ddtank.config.DDTankFileConfigProperties;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class BinaryPicProcess {
    private int[][] data;

    private int height;
    private int width;

    private static final int BLANK = 0;

    private static final int PASS = 1;

    private int x1;
    private int y1;

    private final Library dm;
    private Map<Integer, List<Point>> areasMap;

    private int area;

    // 初始化，初始化完毕后图像是被裁剪过的数组对应原始坐标点为x1, y1
    public BinaryPicProcess(Library dm) {
        this.dm = dm;
        this.areasMap = new HashMap<>();
    }

    private BinaryPicProcess update(int x1, int y1, int x2, int y2, String color, double sim) {
        areasMap = new HashMap<>();
        area = 0;
        this.x1 =x1;
        this.y1 = y1;

        // 将图片保存
        File imgFile = new File(DDTankFileConfigProperties.getTmpPicDir(), Thread.currentThread().getName() + ".bmp");
        dm.capture(x1, y1, x2, y2, imgFile.getAbsolutePath());

        // 初始化参数
        this.width = x2 - x1;
        this.height = y2 - y1;
        data = new int[height][width];
        for (int i = 0; i < height; i++) {
            data[i] = new int[width];
        }

        BufferedImage img;
        try {
            img = ImageIO.read(imgFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Raster raster = img.getData();
        int[] temp = new int[raster.getWidth() * raster.getHeight() * raster.getNumBands()];
        int[] pixels = raster.getPixels(0, 0, raster.getWidth(), raster.getHeight(), temp);
        final int length = pixels.length;
        for (int i = 0; i < length; i += 3) {
            if (ColorUtils.isSimColor(pixels[i], pixels[i + 1], pixels[i + 2], color)) {
                data[i / width / 3][i / 3 % width] = PASS;
            }
        }

        // 裁剪数据
        cropping();

        // 划分区域
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[i][j] == 1) {
                    List<Point> points = new ArrayList<>();
                    int areaId = nextArea();
                    areaSpread(points, i, j, 1, areaId);
                    areasMap.put(areaId, points);
                }
            }
        }

        return this;
    }

    public Point findRoundRole(int x1, int y1, int x2, int y2, String color, double sim) {
        update(x1, y1, x2, y2, color, sim);
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (isArea(i, j)) {
                    int area = data[i][j];
                    boolean passed = false;
                    int offset = 0;
                    if (pass(i, area, 5, 10)
                            && pass(i + 1, area, 1, 4)
                            && pass(i + 2, area, 1, 3)
                            || (pass(i, area, 3, 5)
                            && pass(i + 1, area, 1, 3))) {
                        offset = 3;
                        passed = true;
                    } else if (pass(i, area, 2, 4)
                            && pass(i + 1, area, 0, 0)) {
                        passed = true;
                    }
                    if (passed) {
                        // 向下查找第一个其他区域的颜色
                        while (!isOtherArea(i + offset, j + 2, area)) {
                            if (!check(i + offset, j + 2)) {
                                break;
                            }
                            offset++;
                        }
                        if (isOtherArea(i + offset, j + 2, area)) {
                            return new Point(j + this.x1, i + offset + this.y1);
                        }
                        break;
                    } else {
                        while (check(i, ++j) && data[i][j] == area) ;
                    }
                }
            }
        }
        return null;
    }

    public int findRectangleWidth(int x1, int y1, int x2, int y2, String color, double sim) {
        update(x1, y1, x2, y2, color, sim);
        List<Point> rectPoints = null;
        int maxSize = 0;
        // 面积最大的为矩形
        for (List<Point> points : areasMap.values()) {
            if (points.size() > maxSize) {
                rectPoints = points;
                maxSize = points.size();
            }
        }
        if (rectPoints == null || maxSize <= 4) {
            return -1;
        }

        Map<Integer, List<Integer>> rectPointMap = new HashMap<>();
        // 根据y将点映射到map内，统计出每一行的点，用于确定矩形的长度
        for (int i = 0; i < rectPoints.size(); i++) {
            Point point = rectPoints.get(i);
            List<Integer> xList;
            if ((xList = rectPointMap.get(point.getY())) == null) {
                xList = new ArrayList<>();
            }
            xList.add(point.getX());
            rectPointMap.put(point.getY(), xList);
        }
        // 检测每一行的点是否是连续的
        Map<Integer, Integer> lineContinuous = new HashMap<>();
        for (Integer y : rectPointMap.keySet()) {
            List<Integer> xList = rectPointMap.get(y);
            if (xList.size() < 10) {
                continue;
            }
            Collections.sort(xList);
            Integer now = xList.get(0);
            boolean continuous = true;
            for (int i = 0; i < xList.size(); i++) {
                if (!xList.contains(now + i)) {
                    continuous = false;
                    break;
                }
            }
            if (continuous) {
                lineContinuous.merge(xList.size(), 1, Integer::sum);
            }
        }

        // 找出次数最多的
        int maxWidth = -1;
        int maxCount = -1;
        for (Integer width : lineContinuous.keySet()) {
            if (lineContinuous.get(width) > maxCount) {
                maxCount = lineContinuous.get(width);
                maxWidth = width;
            }
        }
        return maxWidth;
    }

    public List<Point> findLine(int x1, int y1, int x2, int y2, String color, double sim) {
        update(x1, y1, x2, y2, color, sim);
        // 1. 将区域再合并
        areaMerge(2);
        // 2. 找到每个区域最左和最右的点，并计算它们的距离、是直线的可能性
        Map<Integer, Double> areasDistanceMap = new HashMap<>();
        for (Integer area : areasMap.keySet()) {
            int maxLeft = Integer.MAX_VALUE;
            int maxRight = 0;
            Point left = null;
            Point right = null;
            for (Point point : areasMap.get(area)) {
                if (point.getX() < maxLeft) {
                    left = point;
                    maxLeft = point.getX();
                }
                if (point.getX() > maxRight) {
                    right = point;
                    maxRight = point.getX();
                }
            }
            if (left == null || right == null || left == right) {
                continue;
            }
            areasDistanceMap.put(area, Math.sqrt(Math.pow(left.getX() - right.getX(), 2) + Math.pow(left.getY() - right.getY(), 2)));
        }
        // 3. 找出综合 【距离】【面积】最长的
        double maxDistance = Integer.MIN_VALUE;
        int maxPossibleArea = Integer.MAX_VALUE;
        for (Integer area : areasDistanceMap.keySet()) {
            if (areasDistanceMap.get(area) > maxDistance) {
                maxPossibleArea = area;
                maxDistance = areasDistanceMap.get(area);
            }
        }
        if (maxPossibleArea != Integer.MAX_VALUE) {
            return areasMap.get(maxPossibleArea);
        }
        return null;
    }

    private synchronized void cropping() {
        int up = 0, left = 0, right = width, down = height;
        // 裁剪数据集
        for (int i = 0; i < height; i++) {
            boolean end = false;
            for (int j = 0; j < width; j++) {
                if (data[i][j] != BLANK) {
                    end = true;
                    break;
                }
            }
            if (end) {
                // 此行有满足要求的，此时前i行都没有用
                up = i;
                break;
            }
        }
        for (int i = height - 1; i >= 0; i--) {
            boolean end = false;
            for (int j = 0; j < width; j++) {
                if (data[i][j] != BLANK) {
                    end = true;
                    break;
                }
            }
            if (end) {
                // 此行有满足要求的，此时前i行都没有用
                down = height - i;
                break;
            }
        }
        for (int i = 0; i < width; i++) {
            boolean end = false;
            for (int j = 0; j < height; j++) {
                if (data[j][i] != BLANK) {
                    end = true;
                    break;
                }
            }
            if (end) {
                // 此列有满足要求的，此时前i列都没有用
                left = i;
                break;
            }
        }
        for (int i = width - 1; i >= 0; i--) {
            boolean end = false;
            for (int j = 0; j < height; j++) {
                if (data[j][i] != BLANK) {
                    end = true;
                    break;
                }
            }
            if (end) {
                // 此列有满足要求的，此时前i列都没有用
                right = width - i;
                break;
            }
        }
        this.x1 = x1 + left;
        this.y1 = y1 + up;
        this.width = width - left - right;
        this.height = height - up - down;
        int[][] newData = new int[this.height][this.width];
        for (int i = 0; i < this.height; i++) {
            System.arraycopy(data[i + up], left, newData[i], 0, this.width);
        }
        data = newData;
    }

    private boolean pass(int line, int area, int minLen, int maxLen) {
        int len = 0;
        for (int i = 0; i < width; i++) {
            if (check(line, i) && data[line][i] == area) {
                len++;
                if (len > maxLen) {
                    return false;
                }
            }
        }
        return len >= minLen;
    }

    private int nextArea() {
        return --area;
    }

    private boolean isArea(int x, int y) {
        return data[x][y] < 0;
    }

    private boolean isOtherArea(int x, int y, int areaId) {
        return check(x, y) && data[x][y] != BLANK && data[x][y] != areaId;
    }

    /**
     * 区域扩散
     *
     * @return 该区域个数
     */
    private int areaSpread(List<Point> points, int i, int j, int canSpreadId, int areaId) {
        int result = 0;
        if (!check(i, j) || (data[i][j] != canSpreadId) && !canSpread(i, j, canSpreadId)) {
            return 0;
        }
        data[i][j] = areaId;
        if (points != null) {
            points.add(new Point(j + this.x1, i + this.y1));
        }

        // 向4个方向扩散，并记录下这些点
        List<Integer> spreadX = new ArrayList<>();
        List<Integer> spreadY = new ArrayList<>();
        int offset = 1;
        while (check(i + offset, j) && data[i + offset][j] == canSpreadId) {
            data[i + offset][j] = areaId;
            if (points != null) {
                points.add(new Point(j + this.x1, i + offset + this.y1));
            }
            spreadX.add(i + offset);
            spreadY.add(j);
            offset++;
            result++;
        }
        offset = 1;
        while (check(i - offset, j) && data[i - offset][j] == canSpreadId) {
            data[i - offset][j] = areaId;
            if (points != null) {
                points.add(new Point(j + this.x1, i - offset + this.y1));
            }
            spreadX.add(i - offset);
            spreadY.add(j);
            offset++;
            result++;
        }
        offset = 1;
        while (check(i, j + offset) && data[i][j + offset] == canSpreadId) {
            data[i][j + offset] = areaId;
            if (points != null) {
                points.add(new Point(j + offset + this.x1, i + this.y1));
            }
            spreadX.add(i);
            spreadY.add(j + offset);
            offset++;
            result++;
        }
        offset = 1;
        while (check(i, j - offset) && data[i][j - offset] == canSpreadId) {
            data[i][j - offset] = areaId;
            if (points != null) {
                points.add(new Point(j - offset + this.x1, i + this.y1));
            }
            spreadX.add(i);
            spreadY.add(j - offset);
            offset++;
            result++;
        }

        // 从扩散的点中寻找仍可扩散的点
        List<Integer> canSpreadX = new ArrayList<>();
        List<Integer> canSpreadY = new ArrayList<>();
        for (int index = 0; index < spreadX.size(); index++) {
            i = spreadX.get(index);
            j = spreadY.get(index);
            if (canSpread(i, j, canSpreadId)) {
                canSpreadX.add(i);
                canSpreadY.add(j);
            }
        }
        for (int index = 0; index < canSpreadX.size(); index++) {
            result += areaSpread(points, canSpreadX.get(index), canSpreadY.get(index), canSpreadId, areaId);
        }
        return result;
    }

    private boolean canSpread(int i, int j, int canSpreadId) {
        if (check(i - 1, j) && data[i - 1][j] == canSpreadId) {
            return true;
        } else if (check(i + 1, j) && data[i + 1][j] == canSpreadId) {
            return true;
        } else if (check(i, j + 1) && data[i][j + 1] == canSpreadId) {
            return true;
        } else return check(i, j - 1) && data[i][j - 1] == canSpreadId;
    }

    private void areaMerge(int range) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (isArea(i, j)) {
                    int[] anotherArea;
                    while ((anotherArea = getAroundFirstExistOtherArea(i, j, range)) != null) {
                        int anotheri = anotherArea[0];
                        int anohterj = anotherArea[1];
                        int areaId = data[i][j];
                        int anotherAreaId = data[anotheri][anohterj];
                        areaChange(anotherAreaId, areaId);
                        // 更新map
                        List<Point> remove = areasMap.remove(anotherAreaId);
                        if (remove != null) {
                            areasMap.get(areaId).addAll(remove);
                        }
                    }
                }
            }
        }
    }

    private void areaChange(int canSpreadId, int areaId) {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (data[i][j] == canSpreadId) {
                    data[i][j] = areaId;
                }
            }
        }
    }

    private int[] getAroundFirstExistOtherArea(int x, int y, int range) {
        int areaId = data[x][y];
        x = x - range;
        y = y - range;
        for (int i = 0; i < range * 2; i++, x++) {
            for (int j = 0; j < range * 2; j++, y++) {
                if (check(x, y)) {
                    if (data[x][y] != BLANK && data[x][y] != areaId) {
                        return new int[]{x, y};
                    }
                }
            }
            y = y - range * 2;
        }
        return null;
    }

    private boolean check(int x, int y) {
        return x >= 0 && x <= data.length - 1
                && y >= 0 && y <= data[x].length - 1;
    }

    public static double distanceToSegment(Point2D.Double p, Point2D.Double start, Point2D.Double end) {
        // 计算线段的向量
        double dx = end.x - start.x;
        double dy = end.y - start.y;

        // 计算线段的长度的平方
        double segmentLengthSquared = dx * dx + dy * dy;

        // 如果线段长度为0，则点到线段的距离为点到线段起点的距离
        if (segmentLengthSquared == 0) {
            return p.distance(start);
        }

        // 计算点到线段起点的向量在线段方向上的投影长度
        double t = ((p.x - start.x) * dx + (p.y - start.y) * dy) / segmentLengthSquared;
        t = Math.max(0, Math.min(1, t));

        // 计算点到线段起点的距离的平方
        double projectionX = start.x + t * dx;
        double projectionY = start.y + t * dy;

        return p.distance(projectionX, projectionY);
    }
}
