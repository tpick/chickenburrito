package chicken;

/**
 * Created by mley on 11.06.14.
 */
public interface Flavour {

    void configure(WSHandler bws, Field field);
    void placeShips();
    void play();
}
