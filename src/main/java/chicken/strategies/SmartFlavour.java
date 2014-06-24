package chicken.strategies;

import chicken.Cell;
import chicken.Field;
import chicken.Point;
import chicken.meadow.OwnField;

import java.util.Random;

/**
 * Created by mley on 11.06.14.
 */
public class SmartFlavour extends AbstractFlavour {

    private Random r = new Random();

    @Override
    public void placeShips() {
        OwnField of = new OwnField(bws);
        of.placeShips();
    }

    @Override
    public void play() {
        f.fire(new Point(r.nextInt(10), r.nextInt(10)), Field.Special.None);

    }

    @Override
    public void hit(Cell shotAt, Cell hitAt, Field.Special lastSpecial) {

    }
}
