package cn.windor.ddtank.base;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point {
    private int x;
    private int y;


    public Point setOffset(int x, int y) {
        this.x += x;
        this.y += y;
        return this;
    }

    public boolean isCloseTo(Point point, int offsetX, int offsetY) {
        return Math.abs(point.x - x) <= offsetX && Math.abs(point.y - y) <= offsetY;
    }
}
