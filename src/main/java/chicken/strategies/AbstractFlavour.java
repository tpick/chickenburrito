package chicken.strategies;

import chicken.Field;
import chicken.Flavour;
import chicken.WSHandler;

/**
 * Created by mley on 12.06.14.
 */
public abstract  class AbstractFlavour implements Flavour {

    protected WSHandler bws;
    protected Field f;

    @Override
    public void configure(WSHandler bws, Field field) {
        this.bws = bws;
        this.f = field;

    }
}
