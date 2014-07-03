package chicken.strategies;

import chicken.Point;
import lombok.Data;

import java.util.Random;

/**
 * Created by mley on 23.06.14.
 */
@Data
public class Line implements Comparable<Line> {

    Point p;
    boolean horizontal;
    int length;
    Random r = new Random();

    private int calcHalf(int offset) {
        double d = ((double) length) / 2.0;

        return ((int) d)+offset;
    }

    public Point getMiddle(int offset) {
        if (horizontal) {
            return new Point(p.x + calcHalf(offset), p.y);
        }
        return new Point(p.x, p.y + calcHalf(offset));
    }

    @Override
    public int compareTo(Line l) {
        return Integer.compare(l.length, length);
    }

    public boolean isBorder() {
        if(horizontal) {
            return p.y == 0 || p.y == 15;
        } else {
            return p.x == 0 || p.x == 15;
        }
    }
}
