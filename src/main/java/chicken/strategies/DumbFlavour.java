package chicken.strategies;

import chicken.Field;
import chicken.Point;

import java.util.Random;

/**
 * Created by mley on 11.06.14.
 */
public class DumbFlavour extends AbstractFlavour {

    private Random r = new Random();

    @Override
    public void placeShips() {
        bws.send("defaultships");
    }

    @Override
    public void play() {
        f.fire(new Point(r.nextInt(10), r.nextInt(10)), Field.Special.None);

    }
}
