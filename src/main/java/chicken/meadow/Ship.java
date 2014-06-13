package chicken.meadow;

import lombok.Data;

/**
 * Created by mley on 12.06.14.
 */
@Data
public class Ship {

    public static enum Type {
        Carrier(5),
        Cruiser(4),
        Destroya(3),
        Submarine(2);

        public final int length;

        private Type(int l) {
            length = l;
        }

        public Type fromString(String s) {
            for (Type t : Type.values()) {
                if (t.name().toLowerCase().substring(0, 2).equals(s.toLowerCase().substring(0, 2))) {
                    return t;
                }
            }
            return null;

        }
    }

    private final Type type;
    private String shipString;

    public Ship(Type type) {
        this.type = type;
    }


}
