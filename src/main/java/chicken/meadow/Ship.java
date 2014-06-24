package chicken.meadow;

import chicken.Cell;
import chicken.Point;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by mley on 12.06.14.
 */
@Data
public class Ship {


    private Type type;
    private String shipString;
    private Point loc;
    private Boolean horizontal;
    private List<Cell> coords = new ArrayList<>();



    public Ship() {
    }

    public Ship(Type type) {
        this.type = type;
    }

    public boolean liesOn(Point location) {
        for (Cell c : coords) {
            if (c.getLocation().equals(location)) {
                return true;
            }
        }

        return false;
    }

    public void sink(Cell c) {
        hit(c);
        type = Type.fromLength(coords.size());
    }

    public void hit(Cell c) {
        coords.add(c);
        Collections.sort(coords);
        setOrientation();

    }

    private void setOrientation() {
        if(coords.size() >= 2) {
            Point p0 = coords.get(0).getLocation();
            Point p1 = coords.get(1).getLocation();

            if (p0.x == p1.x) {
                horizontal = Boolean.TRUE;
            } else {
                horizontal = Boolean.FALSE;
            }
        }
    }

    public static enum Type {
        Carrier(5),
        Cruiser(4),
        Destroya(3),
        Submarine(2);

        public final int length;

        private Type(int l) {
            length = l;
        }

        public static Type fromString(String s) {
            for (Type t : Type.values()) {
                if (t.name().toLowerCase().substring(0, 2).equals(s.toLowerCase().substring(0, 2))) {
                    return t;
                }
            }
            return null;

        }

        public static Type fromLength(int size) {
            for (Type t : Type.values()) {
                if (size == t.length) {
                    return t;
                }
            }
            return null;
        }
    }


}
