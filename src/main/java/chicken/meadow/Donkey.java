package chicken.meadow;

import lombok.Data;

/**
 * Created by mley on 12.06.14.
 */
@Data
public class Donkey {

    public static enum Type {
        Carrier(5),
        Cruiser(4),
        Destroya(3),
        Submarine(2);

        public final int length;
        private Type(int l) {
            length = l;
        }
    }

    private final Type type;
    public Donkey(Type type) {
        this.type = type;
    }


}
