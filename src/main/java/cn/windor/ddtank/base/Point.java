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

    public void setOffset(int x, int y) {
        this.x += x;
        this.y += y;
    }
}
