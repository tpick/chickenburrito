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
    private int calcHalf() {
        double d = ((double) length) / 2.0;
        double f =  ((r.nextInt(10)+5)/100.0);
        if (r.nextBoolean()) {
            d += ((double)length)*f;
        } else {
            d -= ((double)length)*f;
        }

        return (int) d;
    }

    public Point getMiddle() {
        if (horizontal) {
            return new Point(p.x + calcHalf(), p.y);
        }
        return new Point(p.x, p.y + calcHalf());
    }

    @Override
    public int compareTo(Line l) {
        return Integer.compare(l.length, length);
    }

}
