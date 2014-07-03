package chicken.strategies;

import chicken.Field;
import chicken.Point;
import chicken.meadow.OwnField;

import java.util.Random;

/**
 * Created by mley on 11.06.14.
 */
public class DumbFlavour extends AbstractFlavour {

    private Random r = new Random();

    @Override
    public void placeShips() {
//        OwnField of = new OwnField(bws);
//        of.placeShips();
        pct85Border();

    }


    private void pct85Border() {
        bws.send("00,10,20,30,40");
        bws.send("60,70,80,90,a0");
        bws.send("02,03,04,05");
        bws.send("f2,f3,f4,f5");
        bws.send("07,08,09");
        bws.send("f7,f8,f9");
        bws.send("1b,1c");
        bws.send("eb,ec");

    }


    private void fullBorder() {
        bws.send("00,10,20,30,40");
        bws.send("60,70,80,90,a0");
        bws.send("02,03,04,05");
        bws.send("f2,f3,f4,f5");
        bws.send("07,08,09");
        bws.send("f7,f8,f9");
        bws.send("0b,0c");
        bws.send("fb,fc");

    }

    @Override
    public void play() {
        f.fire(new Point(r.nextInt(16), r.nextInt(16)), Field.Special.None);

    }


}
