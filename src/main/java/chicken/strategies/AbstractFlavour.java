package chicken.strategies;

import chicken.Cell;
import chicken.Field;
import chicken.Flavour;
import chicken.History;
import chicken.WSHandler;

/**
 * Created by mley on 12.06.14.
 */
public abstract class AbstractFlavour implements Flavour {

    protected WSHandler bws;
    protected Field f;

    @Override
    public void configure(WSHandler bws, Field field) {
        this.bws = bws;
        this.f = field;

    }

    @Override
    public void hit(Cell shotAt, Cell hitAt, Field.Special lastSpecial) {
    }

    @Override
    public void sunkShip(Cell c, String msg) {

    }

    @Override
    public History gameOver(boolean b) {
        return null;
    }

    @Override
    public void error(int errorCode) {


    }
}
