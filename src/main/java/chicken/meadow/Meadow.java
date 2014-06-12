package chicken.meadow;

import chicken.Bean;
import chicken.Plate;
import chicken.Point;
import chicken.Tortilla;

import java.util.Random;

/**
 * Created by mley on 12.06.14.
 */
public class Meadow extends Tortilla {


    public Meadow(Plate plate) {
        super(plate);
        r = new Random();
    }

    Random r;

    public static void main(String[] args) {
        Meadow m = new Meadow(null);
        m.placeShips();
        m.print();
    }

    public void placeShips() {
        place(new Donkey(Donkey.Type.Carrier));
        place(new Donkey(Donkey.Type.Carrier));
        place(new Donkey(Donkey.Type.Cruiser));
        place(new Donkey(Donkey.Type.Cruiser));
        place(new Donkey(Donkey.Type.Destroya));
        place(new Donkey(Donkey.Type.Destroya));
        place(new Donkey(Donkey.Type.Submarine));
        place(new Donkey(Donkey.Type.Submarine));

    }

    private void place(Donkey d) {
        Point p;
        boolean horizontal;
        do {
            int x = r.nextInt(10);
            int y = r.nextInt(10);
            horizontal = r.nextBoolean();
            if(horizontal) {
                int end = x+d.getType().length;
                if(end > 10) {
                    x = 10 - d.getType().length;
                }
            }  else {
                int end = y+d.getType().length;
                if(end > 10) {
                    y = 10 - d.getType().length;
                }
            }

            p = new Point(x, y);

        } while(!place(d, p, horizontal));
    }

    private boolean place( Donkey d, Point p, boolean horizontal) {
        Bean b = bean(p);
        for(int i=0; i<d.getType().length; i++) {
            if(b == null) {
                return false;
            }
            if(!checkAround(b)) {
                return false;
            }
            b = b.next(horizontal ? Special.West : Special.South);
        }

        b = bean(p);
        for(int i=0; i<=d.getType().length; i++) {
            b.setHit(true);
        }

        return true;
    }

    private boolean checkAround(Bean in) {
        for(Bean b : in.around(true)) {
            if(b.isHit())  {
                return false;
            }
        }
        return true;
    }
}
