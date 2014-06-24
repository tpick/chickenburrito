package chicken.meadow;

import chicken.Cell;
import chicken.Field;
import chicken.Point;
import chicken.WSHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by mley on 12.06.14.
 */
public class OwnField extends Field {


    public OwnField(WSHandler WSHandler) {
        super(WSHandler, null);
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
        List<Ship> ships = new ArrayList<>();
        while (!placed) {
            try {
                Ship c = place(new Ship(Ship.Type.Carrier));
                ships.add(c);
                ships.add(place(new Ship(Ship.Type.Carrier), c.getHorizontal(), c.getLoc().x, c.getLoc().y));
                ships.add(place(new Ship(Ship.Type.Cruiser)));
                ships.add(place(new Ship(Ship.Type.Cruiser)));
                ships.add(place(new Ship(Ship.Type.Destroya)));
                ships.add(place(new Ship(Ship.Type.Destroya)));
                ships.add(place(new Ship(Ship.Type.Submarine)));
                ships.add(place(new Ship(Ship.Type.Submarine)));
                placed = true;
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage());
                for (Cell c : getCellList()) {
                    c.setObservedPieces(0);
                }
                ships.clear();
            }
        }

        print();

        for(Ship s : ships) {
           getBws().send(s.getShipString());
        }
    }

    /**
     * place ship with preferred orientation and x or y for maximum cluster bombs
     * @param d
     * @param horizontal
     * @param prefX
     * @param prefY
     * @return
     */
    private Ship place(Ship d, boolean horizontal, int prefX, int prefY) {
        int tried = 0;
        Point p;

        do {
            int x, y;
            if(horizontal) {
                x = prefX;
                y = r.nextInt(16);
            } else {
                x = r.nextInt(16);
                y = prefY;
            }

            p = new Point(x, y);
            tried++;
        } while (!place(d, p, horizontal) && (tried <= 10000));

        if (tried > 10000) {
            throw new IllegalStateException("failed to place ship : " + d.getType());
        }

        return d;

    }

    private Ship place(Ship d) {
        int tried = 0;
        Point p;
        boolean horizontal;
        do {
            int x = r.nextInt(16);
            int y = r.nextInt(16);

            if (x == 0) {
                x = 1;
            }
            if (y == 0) {
                y = 1;
            }

            horizontal = r.nextBoolean();
            if (horizontal) {
                int end = x + d.getType().length - 1;
                if (end > 16) {
                    x = 16 - d.getType().length - 1;
                }
            } else {
                int end = y + d.getType().length;
                if (end > 16) {
                    y = 16 - d.getType().length;
                }
            }

            p = new Point(x, y);
            tried++;
        } while (!place(d, p, horizontal) && (tried <= 10000));

        if (tried > 10000) {
            throw new IllegalStateException("failed to place ship : " + d.getType());
        }

        return d;

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
        String shipStr = "";
        for (int i = 0; i < d.getType().length; i++) {
            b.setObservedPieces(d.getType().length);
            shipStr += b.getLocation().toString();
            if (i < d.getType().length - 1) {
                shipStr += ",";
            }
            b = b.next(horizontal ? Special.West : Special.South);
        }
        d.setHorizontal(horizontal);
        d.setShipString(shipStr);
        d.setLoc(p);
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

    public void print() {
        System.out.println("Y\\X 0 1 2 3 4 5 6 7 8 9 A B C D E F");
        for (int y = 0; y < 16; y++) {
            System.out.print(Integer.toHexString(y)+"  |" );
            for (int x = 0; x < 16; x++) {
                Cell c = cells[x][y];
                String s = "  ";
                if (c.getObservedPieces() > 0) {
                    s = "▉▉";
                }
                System.out.print(s);
            }
            System.out.println("|");
        }

    }
}
