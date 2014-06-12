package chicken.strategies;

import chicken.Flavour;
import chicken.Plate;
import chicken.Tortilla;

/**
 * Created by mley on 12.06.14.
 */
public abstract  class AbstractFlavour implements Flavour {

    protected Plate bws;
    protected Tortilla f;

    @Override
    public void configure(Plate bws, Tortilla tortilla) {
        this.bws = bws;
        this.f = tortilla;

    }
}
