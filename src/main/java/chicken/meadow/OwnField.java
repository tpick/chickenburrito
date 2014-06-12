package chicken.meadow;

import chicken.Cell;
import chicken.Field;
import chicken.Point;
import chicken.WSHandler;

import java.util.Random;

/**
 * Created by mley on 12.06.14.
 */
public class OwnField extends Field {


    public OwnField(WSHandler WSHandler) {
        super(WSHandler);
        r = new Random();
    }

    Random r;

    public static void main(String[] args) {
        OwnField m = new OwnField(null);
        m.placeShips();
        m.print();
    }

    public void placeShips() {
        boolean placed = false;
        while (!placed) {
            try {
                place(new Ship(Ship.Type.Carrier));
                place(new Ship(Ship.Type.Carrier));
                place(new Ship(Ship.Type.Cruiser));
                place(new Ship(Ship.Type.Cruiser));
                place(new Ship(Ship.Type.Destroya));
                place(new Ship(Ship.Type.Destroya));
                place(new Ship(Ship.Type.Submarine));
                place(new Ship(Ship.Type.Submarine));
                placed = true;
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                for(Cell c : getCellList()) {
                    c.setObservedPieces(0);
                }
            }

        }
    }

    private void place(Ship d) {
        int tried = 0;
        Point p;
        boolean horizontal;
        do {
            int x = r.nextInt(10);
            int y = r.nextInt(10);

            if (x == 0) {
                x = 1;
            }
            if (y == 0) {
                y = 1;
            }

            horizontal = r.nextBoolean();
            if (horizontal) {
                int end = x + d.getType().length - 1;
                if (end > 10) {
                    x = 10 - d.getType().length - 1;
                }
            } else {
                int end = y + d.getType().length;
                if (end > 10) {
                    y = 10 - d.getType().length;
                }
            }

            p = new Point(x, y);
            tried++;
        } while (!place(d, p, horizontal) && (tried <= 10000));
        System.out.println("Tried: "+tried+" for "+d.getType());
        if(tried > 10000) {
            throw new IllegalStateException("failed to place ship : "+d.getType());
        }
    }

    private boolean place(Ship d, Point p, boolean horizontal) {
        Cell b = bean(p);
        for (int i = 0; i < d.getType().length; i++) {
            if (b == null) {
                return false;
            }
            if (!checkAround(b)) {
                return false;
            }
            b = b.next(horizontal ? Special.West : Special.South);
        }

        b = bean(p);
        for (int i = 0; i < d.getType().length; i++) {
            b.setObservedPieces(d.getType().length);
            b = b.next(horizontal ? Special.West : Special.South);
        }

        return true;
    }

    private boolean checkAround(Cell in) {
        for (Cell b : in.around(true)) {
            if (b.getObservedPieces() != 0) {
                return false;
            }
        }
        return true;
    }
}
