package chicken;

import lombok.EqualsAndHashCode;

/**
 * Created by mley on 11.06.14.
 */

@EqualsAndHashCode
public class Point {

    public final int x;
    public final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Point(String p) {
        x = Integer.parseInt(p.substring(0, 1), 16);
        y = Integer.parseInt(p.substring(1, 2), 16);
    }

    public Point neighbour(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }

    public String toString() {
        return Integer.toHexString(x)+Integer.toHexString(y);
    }
}
