package chicken.meadow;

import chicken.Burrito;
import chicken.Cell;
import chicken.Field;
import chicken.Point;
import chicken.WSHandler;

import java.util.ArrayList;
import java.util.Collections;
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
    }

    public void placeShips() {
        boolean placed = false;
        List<Ship> ships = new ArrayList<>();
        while (!placed) {
            try {

                ships.add(new Ship(Ship.Type.Carrier));

                ships.add(new Ship(Ship.Type.Carrier));
                ships.add(new Ship(Ship.Type.Destroya));
                ships.add(new Ship(Ship.Type.Destroya));
                ships.add(new Ship(Ship.Type.Cruiser));
                ships.add(new Ship(Ship.Type.Cruiser));
                ships.add(new Ship(Ship.Type.Submarine));
                ships.add(new Ship(Ship.Type.Submarine));
                Collections.shuffle(ships, r);

                Ship carrier = null;
                for (Ship s : ships) {
                    if (s.getType() == Ship.Type.Carrier) {
                        if (carrier == null) {
                            carrier = s;
                            place(s);
                        } else {
                            place(s, carrier.getHorizontal(), carrier.getLoc().x, carrier.getLoc().y);
                        }

                    } else {
                        place(s);
                    }
                }

                placed = true;
            } catch (IllegalStateException e) {
                Burrito.out.println(e.getMessage());
                for (Cell c : getCellList()) {
                    c.setObservedPieces(0);
                }
                ships.clear();
            }
        }

        print();

        if (getBws() != null) {
            for (Ship s : ships) {
                getBws().send(s.getShipString());
            }
        }
    }

    /**
     * place ship with preferred orientation and x or y for maximum cluster bombs
     *
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
            if (horizontal) {
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
        Burrito.out.printf("Needed %d tries to place %s%n", tried, d.getType());

        return d;

    }

    private Ship place(Ship d) {
        int tried = 0;
        Point p;
        boolean horizontal;
        do {
            int x = r.nextInt(16);
            int y = r.nextInt(16);

            horizontal = r.nextBoolean();
            if (horizontal) {
                int end = x + d.getType().length;
                if (end > 16) {
                    x = 16 - d.getType().length;
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
        Burrito.out.printf("Needed %d tries to place %s%n", tried, d.getType());
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
        Burrito.out.println("Y\\X 0 1 2 3 4 5 6 7 8 9 A B C D E F");
        for (int y = 0; y < 16; y++) {
            Burrito.out.print(Integer.toHexString(y) + "  |");
            for (int x = 0; x < 16; x++) {
                Cell c = cells[x][y];
                String s = "  ";
                if (c.getObservedPieces() > 0) {
                    s = "▉▉";
                }
                Burrito.out.print(s);
            }
            Burrito.out.println("|");
        }

    }
}
