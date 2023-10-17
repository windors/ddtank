package cn.windor.ddtank.util;

import cn.windor.ddtank.base.Library;
import cn.windor.ddtank.base.Point;

import java.awt.geom.Point2D;
import java.util.*;

public class BinaryPicProcess {
    private int[][] data;

    private int height;
    private int width;

    private int areaCount;

    private static final int BLANK = 0;

    private static final int PASS = 1;

    private final int x1;
    private final int y1;

    private final Map<Integer, List<Point>> areasMap = new HashMap<>();

    private int area;

    // 初始化，初始化完毕后图像是被裁剪过的数组对应原始坐标点为x1, y1
    public BinaryPicProcess(Library dm, int x1, int y1, int x2, int y2, String color, double sim) {
        this.width = x2 - x1;
        this.height = y2 - y1;
        data = new int[height][width];
        for (int i = 0; i < height; i++) {
            data[i] = new int[width];
        }
        for (Point point : dm.findColorEx(x1, y1, x2, y2, color, sim, 0)) {
            data[point.getY() - y1][point.getX() - x1] = PASS;
        }

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
        width = x2 - right - this.x1;
        height = y2 - down - this.y1;
        int[][] newData = new int[height][width];
        for (int i = 0; i < height; i++) {
            System.arraycopy(data[i + up], left, newData[i], 0, width);
        }
        data = newData;

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
    }
    public Point findRoundRole() {
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                if (isArea(i, j)) {
                    int area = data[i][j];
                    if (pass(i, area, 5, 10)
                            && pass(i + 1, area, 1, 4)
                            && pass(i + 2, area, 1, 3)
                            || (pass(i, area, 3, 5)
                            && pass(i + 1, area, 1, 3))) {
                        int offset = 3;
                        // 向下查找第一个其他区域的颜色
                        while (!isOtherArea(i + offset, j + 2, area)) {
                            offset++;
                        }
                        if (isOtherArea(i + offset, j + 2, area)) {
                            return new Point(j + x1, i + offset + y1);
                        }
                    }
                    break;
                }
            }
        }
        return null;
    }

    public List<Point> findLine() {
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
        if (!check(i, j)) return result;
        if (data[i][j] == canSpreadId) {
            data[i][j] = areaId;
            if (points != null) {
                points.add(new Point(j + this.x1, i + this.y1));
            }
            result += areaSpread(points, i + 1, j, canSpreadId, areaId);
            result += areaSpread(points, i - 1, j, canSpreadId, areaId);
            result += areaSpread(points, i, j + 1, canSpreadId, areaId);
            result += areaSpread(points, i, j - 1, canSpreadId, areaId);
        }
        return result;
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
                        areaSpread(null, anotheri, anohterj, anotherAreaId, areaId);
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

    private int[] getAroundFirstExistOtherArea(int x, int y, int range) {
        int areaId = data[x][y];
        int[] result = null;
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
        }
        return result;
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
