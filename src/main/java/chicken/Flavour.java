package chicken;

/**
 * Created by mley on 11.06.14.
 */
public interface Flavour {

    void configure(WSHandler bws, Field field);
    void placeShips();
    void play();

    void hit(Cell shotAt, Cell hitAt, Field.Special lastSpecial);

    void sunkShip(Cell c, String msg);

    History gameOver(boolean b);

    void error(int errorCode);
}
